package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.RowingState;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public class HeldItemLayerMixin {
    @Inject(method="submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",at=@At("HEAD"),cancellable=true)
    private void rowing$stow(AvatarRenderState state,ItemStackRenderState itemState,ItemStack item,HumanoidArm arm,
            PoseStack stack,SubmitNodeCollector collector,int light,CallbackInfo ci) {
        RowingState row=(RowingState)state;
        if(row.rowingHandsOnly() || (row.rowingPose()!=null && row.rowingPose().owns(arm==HumanoidArm.LEFT))) ci.cancel();
    }
}
