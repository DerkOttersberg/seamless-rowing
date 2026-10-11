package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @SuppressWarnings({"rawtypes","unchecked"})
    @Inject(method="<init>",at=@At("RETURN"))
    private void rowing$layer(CallbackInfo ci) { ((LivingRendererAccess)this).rowingAddLayer(new RowingArmLayer((RenderLayerParent)this)); }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("RETURN"))
    private void rowing$capture(Avatar avatar,AvatarRenderState state,float partial,CallbackInfo ci) {
        RowingState row=(RowingState)state; row.rowingPose(null); row.rowingBase(null); row.rowingHandsOnly(false);
        if(avatar instanceof AbstractClientPlayer player) {
            Minecraft mc=Minecraft.getInstance();
            boolean first=player==mc.player && mc.options.getCameraType().isFirstPerson();
            row.rowingPose(RowingClient.capture(player,partial,first));
            var pose=row.rowingPose();
            if(pose!=null && player.getVehicle() instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat boat) {
                float weight=Math.max(pose.ownLeft() ? pose.leftWeight() : 0,pose.ownRight() ? pose.rightWeight() : 0);
                float delta=net.minecraft.util.Mth.wrapDegrees(boat.getYRot(partial)-state.bodyRot);
                state.bodyRot+=delta*weight;
                state.yRot=net.minecraft.util.Mth.wrapDegrees(player.getYRot(partial)-state.bodyRot);
            }
        }
    }
}
