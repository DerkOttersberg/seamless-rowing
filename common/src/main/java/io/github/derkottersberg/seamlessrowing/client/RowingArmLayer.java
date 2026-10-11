package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.*;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.*;
import java.lang.Math;

public final class RowingArmLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    private ArmRigModel rig;
    public RowingArmLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent) { super(parent); }
    @Override public void submit(PoseStack stack,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch) {
        RowingState carrier=(RowingState)state; RowingPose pose=carrier.rowingPose();
        if(pose==null || !pose.articulated()&&!carrier.rowingHandsOnly() || carrier.rowingBase()==null || state.isInvisible) return;
        PlayerModel model=getParentModel();
        if(!ArmMesh.supported(model.leftArm) || !ArmMesh.supported(model.rightArm)) return;
        if(rig==null) rig=new ArmRigModel(model);
        Matrix4f transform=new Matrix4f(stack.last().pose()).invert().mul(carrier.rowingBase());
        ArmRigModel.Bone left=solve(model.leftArm,pose,true,transform,state.showLeftSleeve);
        ArmRigModel.Bone right=solve(model.rightArm,pose,false,transform,state.showRightSleeve);
        collector.submitModel(rig,new ArmRigModel.DrawState(left,right,carrier.rowingHandsOnly()),stack,state.skin.body().texturePath(),light,
                LivingEntityRenderer.getOverlayCoords(state,0),state.outlineColor,null);
    }
    public static ArmRigModel.Bone solve(ModelPart part,RowingPose pose,boolean left,Matrix4f transform,boolean sleeve) {
        var cube=((ModelPartAccess)(Object)part).rowingCubes().getFirst(); float center=(cube.minX+cube.maxX)/2;
        Quaternionf original=new Quaternionf().rotationZYX(part.zRot,part.yRot,part.xRot);
        Vector3f start=original.transform(new Vector3f(center,-2,0)).add(part.x,part.y,part.z);
        if(!pose.articulated())return new ArmRigModel.Bone(start,original,new Quaternionf(),pose.owns(left),sleeve);
        Vector3f shoulder=new Vector3f(part.x+center,part.y-2,part.z);
        float weight=pose.weight(left); start.lerp(shoulder,weight); shoulder=start;
        var grip=pose.grip(left);
        Vector3f target=transform.transformPosition(new Vector3f((float)grip.x,(float)grip.y,(float)grip.z)).mul(16);
        shoulder=ShoulderReach.adjust(shoulder,target,weight);
        var shaft=pose.shaft(left);
        Vector3f paddle=transform.transformDirection(new Vector3f((float)shaft.x,(float)shaft.y,(float)shaft.z));
        var solution=RowingArmKinematics.solve(point(shoulder),point(target),left,point(paddle));
        Quaternionf blendUpper=new Quaternionf(original).slerp(solution.upper(),weight);
        Quaternionf blendLower=new Quaternionf().slerp(solution.lower(),weight);
        return new ArmRigModel.Bone(shoulder,blendUpper,blendLower,pose.owns(left),sleeve);
    }
    private static Point3 point(Vector3f v) { return new Point3(v.x,v.y,v.z); }
}
