package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.object.boat.AbstractBoatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatModel.class)
public class BoatModelMixin {
    @Shadow @Final private ModelPart leftPaddle;
    @Shadow @Final private ModelPart rightPaddle;
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;)V",at=@At("TAIL"))
    private void rowing$ease(BoatRenderState state,CallbackInfo ci) {
        var pose=((BoatState)state).rowingBoatPose(); if(pose==null) return;
        leftPaddle.xRot=pose.leftPaddle().pitch(); leftPaddle.yRot=pose.leftPaddle().yaw();
        rightPaddle.xRot=pose.rightPaddle().pitch(); rightPaddle.yRot=(float)Math.PI-pose.rightPaddle().yaw();
    }
}
