package io.github.derkottersberg.seamlessrowing.client;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import java.util.*;

/** Native armor/trim/glint passes use the same copied, articulated model. */
public final class RowingArmorModel extends HumanoidModel<AbstractClientPlayer> {
    public RowingArmorModel(HumanoidModel<?> source){super(root(source));}
    private static ModelPart root(HumanoidModel<?> source){
        var parts=new HashMap<String,ModelPart>();
        parts.put("head",copy(source.head));parts.put("hat",copy(source.hat));parts.put("body",copy(source.body));
        parts.put("left_leg",copy(source.leftLeg));parts.put("right_leg",copy(source.rightLeg));
        parts.put("left_arm",ArmRigModel.arm(source.leftArm,new ModelPart(List.of(),Map.of())));
        parts.put("right_arm",ArmRigModel.arm(source.rightArm,new ModelPart(List.of(),Map.of())));
        return new ModelPart(List.of(),parts);
    }
    private static ModelPart copy(ModelPart source){
        var children=new HashMap<String,ModelPart>();((ModelPartAccess)(Object)source).rowingChildren().forEach((name,part)->children.put(name,copy(part)));
        return new ModelPart(((ModelPartAccess)(Object)source).rowingCubes(),children);
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha){
        var context=RenderContext.current();
        if(context!=null&&context.rowingPose()!=null){
            var renderer=net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(context.player);
            if(renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer){
                PlayerModel<?> player=playerRenderer.getModel();var row=context.rowingPose();
                body.copyFrom(player.body);head.copyFrom(player.head);hat.copyFrom(player.hat);
                TorsoSurface.restore(body);TorsoSurface.apply(body,row.lean(),row.twist());
                var transform=new Matrix4f(stack.last().pose()).invert().mul(context.rowingBase());
                boolean visibleLeft=leftArm.visible,visibleRight=rightArm.visible;
                var l=bone(player.leftArm,row,true,transform,context.rowingHandsOnly());var r=bone(player.rightArm,row,false,transform,context.rowingHandsOnly());
                ArmRigModel.apply(leftArm,leftArm.getChild("lower"),new ArmRigModel.Bone(l.shoulder(),l.upper(),l.lower(),visibleLeft&&l.visible(),false),context.rowingHandsOnly());
                ArmRigModel.apply(rightArm,rightArm.getChild("lower"),new ArmRigModel.Bone(r.shoulder(),r.upper(),r.lower(),visibleRight&&r.visible(),false),context.rowingHandsOnly());
                if(context.rowingHandsOnly()){head.visible=hat.visible=body.visible=leftLeg.visible=rightLeg.visible=false;}
            }
        }
        for(var part:List.of(head,hat,body,leftArm,rightArm,leftLeg,rightLeg)) SurfaceRenderer.render(part,stack,consumer,light,overlay,red,green,blue,alpha);
    }
    private static ArmRigModel.Bone bone(ModelPart arm,RowingPose row,boolean left,Matrix4f transform,boolean hands) {
        if(row.owns(left))return RowingArmLayer.solve(arm,row,left,transform,false);
        var cube=((ModelPartAccess)(Object)arm).rowingCubes().get(0);
        var rotation=new org.joml.Quaternionf().rotationZYX(arm.zRot,arm.yRot,arm.xRot);
        var shoulder=rotation.transform(new org.joml.Vector3f((cube.minX+cube.maxX)*.5f,-2,0)).add(arm.x,arm.y,arm.z);
        return new ArmRigModel.Bone(shoulder,rotation,new org.joml.Quaternionf(),!hands,false);
    }
}

