package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.HandsState;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class HandsRendererMixin {
    @Inject(method="submitArmWithItem",at=@At("HEAD"),cancellable=true)
    private void rowing$stow(PlayerRenderState player,FirstPersonHandsAndItemsRenderState hands,float partial,float pitch,
            InteractionHand hand,float swing,ItemStack item,float height,PoseStack stack,SubmitNodeCollector collector,int light,CallbackInfo ci) {
        HandsState state=(HandsState)hands;
        if(hand==InteractionHand.MAIN_HAND ? state.rowingOwnMain() : state.rowingOwnOff()) ci.cancel();
    }
}
