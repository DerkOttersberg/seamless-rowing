package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;
@Mixin(value=PlayerModel.class,priority=500)
public class PlayerModelMixin implements AnimationOwnership {
    @Unique private boolean rowing$left,rowing$right;
    @Unique private final Map<ModelPart,Boolean> rowing$hidden=new IdentityHashMap<>();
    @Unique private final Map<ModelPart,float[]> rowing$moved=new IdentityHashMap<>();
    public void rowingRestorePose(){
        rowing$moved.forEach((part,pos)->{
            // Preserve a position installed by another renderer or custom-model adapter.
            if(part.x==pos[3]&&part.y==pos[4]&&part.z==pos[5])part.setPos(pos[0],pos[1],pos[2]);
        });rowing$moved.clear();
        rowing$hidden.forEach((part,visible)->part.visible=visible);rowing$hidden.clear();
        PlayerModel<?> model=(PlayerModel<?>)(Object)this;TorsoSurface.restore(model.body);TorsoSurface.restore(model.jacket);
    }
    public void rowingGrant(boolean left){if(left)rowing$left=true;else rowing$right=true;}
    public boolean rowingGranted(boolean left){return left?rowing$left:rowing$right;}
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("HEAD"),order=900)
    private void restore(LivingEntity entity,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci){
        var context=RenderContext.current();PlayerModel<?> m=(PlayerModel<?>)(Object)this;
        if(context!=null&&context.player==entity&&context.rowingPose()!=null)RowingClient.prepareModel(m);
        rowingRestorePose();
        rowing$left=rowing$right=!RowingClient.scheduledOwnership();
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"),order=1100)
    private void pose(LivingEntity entity,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci){
        var context=RenderContext.current();if(context==null||context.player!=entity||context.rowingPose()==null)return;
        var row=context.rowingPose();PlayerModel<?> m=(PlayerModel<?>)(Object)this;
        if(!TorsoSurface.supported(m.body)||!ArmMesh.supported(m.leftArm)||!ArmMesh.supported(m.rightArm)){context.rowingPose(null);return;}
        if(!rowing$left||!rowing$right){row=new RowingPose(row.leftGrip(),row.rightGrip(),row.leftWeight(),row.rightWeight(),row.ownLeft()&&rowing$left,row.ownRight()&&rowing$right,row.articulated(),row.lean(),row.twist(),row.leftPaddle(),row.rightPaddle(),row.leftShaft(),row.rightShaft());context.rowingPose(row);}
        TorsoSurface.apply(m.body,row.lean(),row.twist());TorsoSurface.apply(m.jacket,row.lean(),row.twist());
        if(!context.rowingHandsOnly()&&m.head.visible&&io.github.derkottersberg.seamlessrowing.math.TorsoMotion.attachedHead(m.head.x-m.body.x,m.head.y-m.body.y,m.head.z-m.body.z)){
            var h=io.github.derkottersberg.seamlessrowing.math.TorsoMotion.point(m.head.x,m.head.y,m.head.z,row.lean(),row.twist());move(m.head,h);move(m.hat,h);m.hat.copyFrom(m.head);
        }
        for(var arm:new ModelPart[]{m.leftArm,m.rightArm}){
            var cube=((ModelPartAccess)(Object)arm).rowingCubes().get(0);float center=(cube.minX+cube.maxX)*.5f;
            var shoulder=io.github.derkottersberg.seamlessrowing.math.TorsoMotion.point(arm.x+center,arm.y-2,arm.z,row.lean(),row.twist());move(arm,new org.joml.Vector3f(shoulder.x-center,shoulder.y+2,shoulder.z));
        }
        m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);
        if(context.rowingHandsOnly())for(var part:new ModelPart[]{m.head,m.hat,m.body,m.jacket,m.leftLeg,m.rightLeg,m.leftPants,m.rightPants})hide(part);
        if(row.articulated()){
            if(row.ownLeft()){hide(m.leftArm);hide(m.leftSleeve);}if(row.ownRight()){hide(m.rightArm);hide(m.rightSleeve);}
        }else{
            if(row.ownLeft()){m.leftArm.xRot=-1.15f-row.leftPaddle().pitch()*.2f;m.leftArm.zRot=-.2f;m.leftSleeve.copyFrom(m.leftArm);}
            if(row.ownRight()){m.rightArm.xRot=-1.15f-row.rightPaddle().pitch()*.2f;m.rightArm.zRot=.2f;m.rightSleeve.copyFrom(m.rightArm);}
        }
    }
    @Unique private void hide(ModelPart part){rowing$hidden.putIfAbsent(part,part.visible);part.visible=false;}
    @Unique private void move(ModelPart part,org.joml.Vector3f point){
        rowing$moved.put(part,new float[]{part.x,part.y,part.z,point.x,point.y,point.z});part.setPos(point.x,point.y,point.z);
    }
}
