package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.model.geom.ModelPart;
import java.util.IdentityHashMap;
import java.util.Map;
import java.lang.Math;

@Mixin(value=PlayerModel.class,priority=500)
public class PlayerModelMixin implements AnimationOwnership {
    @Unique private boolean rowing$leftGranted,rowing$rightGranted;
    public void rowingGrant(boolean left) { if(left) rowing$leftGranted=true; else rowing$rightGranted=true; }
    public boolean rowingGranted(boolean left) { return left ? rowing$leftGranted : rowing$rightGranted; }
    @Unique private final Map<ModelPart,Boolean> rowing$hidden=new IdentityHashMap<>();
    // Restore before other animation adapters read the model; compose after their RETURN hooks.
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("HEAD"),order=900)
    private void rowing$restore(AvatarRenderState state,CallbackInfo ci) {
        if(((RowingState)state).rowingPose()!=null)RowingClient.prepareModel((PlayerModel)(Object)this);
        TorsoSurface.restore(((PlayerModel)(Object)this).body);
        rowing$hidden.forEach((part,visible)->part.visible=visible);
        rowing$hidden.clear();
        rowing$leftGranted=rowing$rightGranted=!RowingClient.scheduledOwnership();
    }
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("RETURN"),order=1100)
    private void rowing$pose(AvatarRenderState state,CallbackInfo ci) {
        RowingState carrier=(RowingState)state; RowingPose row=carrier.rowingPose();
        if(row==null) return;
        if(!rowing$leftGranted || !rowing$rightGranted) {
            row=new RowingPose(row.leftGrip(),row.rightGrip(),row.leftWeight(),row.rightWeight(),
                    row.ownLeft()&&rowing$leftGranted,row.ownRight()&&rowing$rightGranted,
                    row.articulated(),row.lean(),row.twist(),row.leftPaddle(),row.rightPaddle(),row.leftShaft(),row.rightShaft());
            carrier.rowingPose(row);
        }
        PlayerModel m=(PlayerModel)(Object)this;
        if(!TorsoSurface.supported(m.body)||!ArmMesh.supported(m.leftArm)||!ArmMesh.supported(m.rightArm)) {
            carrier.rowingPose(null);return;
        }
        TorsoSurface.apply(m.body,row.lean(),row.twist());
        // FirstPersonModel may deliberately move the head thousands of pixels away to hide it.
        // Preserve that decision; a periodic spine curve must never wrap a hidden head back in view.
        if(!carrier.rowingHandsOnly() && m.head.visible
                && io.github.derkottersberg.seamlessrowing.math.TorsoMotion.attachedHead(m.head.x-m.body.x,m.head.y-m.body.y,m.head.z-m.body.z)) {
            var head=io.github.derkottersberg.seamlessrowing.math.TorsoMotion.point(m.head.x,m.head.y,m.head.z,row.lean(),row.twist());
            m.head.setPos(head.x,head.y,head.z);
        }
        for(var arm:new ModelPart[]{m.leftArm,m.rightArm}) {
            var cube=((ModelPartAccess)(Object)arm).rowingCubes().getFirst();float center=(cube.minX+cube.maxX)*.5f;
            var shoulder=io.github.derkottersberg.seamlessrowing.math.TorsoMotion.point(arm.x+center,arm.y-2,arm.z,row.lean(),row.twist());
            arm.setPos(shoulder.x-center,shoulder.y+2,shoulder.z);
        }
        boolean advanced=row.articulated() && ArmMesh.supported(m.leftArm) && ArmMesh.supported(m.rightArm);
        if(carrier.rowingHandsOnly()) {
            m.allParts().forEach(part->{rowing$hidden.put(part,part.visible);part.visible=false;});
            m.root().visible=true;
            m.leftArm.visible=row.ownLeft()&&!advanced; m.rightArm.visible=row.ownRight()&&!advanced;
            m.leftSleeve.visible=state.showLeftSleeve; m.rightSleeve.visible=state.showRightSleeve;
        }
        if(advanced) {
            if(row.ownLeft()) { rowing$hidden.putIfAbsent(m.leftArm,m.leftArm.visible);m.leftArm.visible=false; }
            if(row.ownRight()) { rowing$hidden.putIfAbsent(m.rightArm,m.rightArm.visible);m.rightArm.visible=false; }
        } else {
            if(row.ownLeft()) { m.leftArm.xRot=-1.15f-row.leftPaddle().pitch()*.2f; m.leftArm.zRot=-.2f; }
            if(row.ownRight()) { m.rightArm.xRot=-1.15f-row.rightPaddle().pitch()*.2f; m.rightArm.zRot=.2f; }
        }
    }
}
