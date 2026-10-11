package io.github.derkottersberg.seamlessrowing.client;
import io.github.derkottersberg.seamlessrowing.math.*;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.PlayerModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.*;
public final class RowingArmLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private ArmRigModel rig;
    public RowingArmLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
    @Override public void render(PoseStack stack,MultiBufferSource buffers,int light,AbstractClientPlayer player,float limb,float amount,float partial,float age,float yaw,float pitch){
        var context=RenderContext.current();if(context==null||context.player!=player||context.rowingPose()==null||player.isInvisible())return;
        var pose=context.rowingPose();if(!pose.articulated()&&!context.rowingHandsOnly())return;var model=getParentModel();
        if(!ArmMesh.supported(model.leftArm)||!ArmMesh.supported(model.rightArm))return;
        if(rig==null)rig=new ArmRigModel(model);
        var transform=new Matrix4f(stack.last().pose()).invert().mul(context.rowingBase());
        var left=solve(model.leftArm,pose,true,transform,player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        var right=solve(model.rightArm,pose,false,transform,player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        rig.setupAnim(new ArmRigModel.DrawState(left,right,context.rowingHandsOnly()));
        rig.renderToBuffer(stack,buffers.getBuffer(rig.renderType(player.getSkin().texture())),light,LivingEntityRenderer.getOverlayCoords(player,0),-1);
    }
    public static ArmRigModel.Bone solve(ModelPart part,RowingPose pose,boolean left,Matrix4f transform,boolean sleeve){
        var cube=((ModelPartAccess)(Object)part).rowingCubes().get(0);float center=(cube.minX+cube.maxX)/2;
        var original=new Quaternionf().rotationZYX(part.zRot,part.yRot,part.xRot);
        var start=original.transform(new Vector3f(center,-2,0)).add(part.x,part.y,part.z);
        if(!pose.articulated())return new ArmRigModel.Bone(start,original,new Quaternionf(),pose.owns(left),sleeve);
        var shoulder=new Vector3f(part.x+center,part.y-2,part.z);float weight=pose.weight(left);start.lerp(shoulder,weight);shoulder=start;
        var grip=pose.grip(left);var target=transform.transformPosition(new Vector3f((float)grip.x,(float)grip.y,(float)grip.z)).mul(16);
        shoulder=ShoulderReach.adjust(shoulder,target,weight);
        var shaft=pose.shaft(left);var paddle=transform.transformDirection(new Vector3f((float)shaft.x,(float)shaft.y,(float)shaft.z));
        var solution=RowingArmKinematics.solve(point(shoulder),point(target),left,point(paddle));
        return new ArmRigModel.Bone(shoulder,new Quaternionf(original).slerp(solution.upper(),weight),new Quaternionf().slerp(solution.lower(),weight),pose.owns(left),sleeve);
    }
    private static Point3 point(Vector3f v){return new Point3(v.x,v.y,v.z);}
}
