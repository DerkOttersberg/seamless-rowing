package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatRenderer.class)
public class BoatRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;Lnet/minecraft/client/renderer/entity/state/BoatRenderState;F)V",at=@At("RETURN"))
    private void rowing$capture(AbstractBoat boat,BoatRenderState state,float partial,CallbackInfo ci) {
        BoatState row=(BoatState)state; row.rowingBoatPose(null); row.rowingLocalPlayer(null);row.rowingRestorePaddles(false);
        if(boat.getControllingPassenger() instanceof AbstractClientPlayer driver) {
            var mc=Minecraft.getInstance(); boolean first=driver==mc.player && mc.options.getCameraType().isFirstPerson();
            row.rowingRestorePaddles(first&&RowingClient.restoresPaddles()&&RowingClient.managesFirstPersonBoat());
            row.rowingBoatPose(RowingClient.capture(driver,partial,first));
            row.rowingLocalPlayer(RowingClient.firstPersonState(boat,partial));
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"))
    private void rowing$local(BoatRenderState state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci) {
        var player=((BoatState)state).rowingLocalPlayer(); if(player==null || !camera.isFirstPerson) return;
        var local=Minecraft.getInstance().player; if(local==null) return;
        AvatarRenderer renderer=(AvatarRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(local);
        stack.pushPose();
        stack.translate(player.x-state.x,player.y-state.y,player.z-state.z);
        renderer.submit(player,stack,collector,camera); stack.popPose();
    }
}
