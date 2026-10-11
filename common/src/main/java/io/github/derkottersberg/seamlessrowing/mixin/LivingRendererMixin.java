package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.RowingState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingRendererMixin {
    // The separately submitted hands own this pass. Do not submit a hidden
    // full player model: custom model draw hooks may restore its head/body.
    @Inject(method="getRenderType",at=@At("HEAD"),cancellable=true)
    private void rowing$handsBase(LivingEntityRenderState state,boolean visible,boolean translucent,boolean glowing,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.client.renderer.rendertype.RenderType> cir) {
        if(state instanceof RowingState row && row.rowingHandsOnly() && row.rowingPose()!=null)cir.setReturnValue(null);
    }
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"))
    private void rowing$base(LivingEntityRenderState state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci) {
        if(state instanceof RowingState rowing && rowing.rowingPose()!=null) rowing.rowingBase(new Matrix4f(stack.last().pose()));
    }
}
