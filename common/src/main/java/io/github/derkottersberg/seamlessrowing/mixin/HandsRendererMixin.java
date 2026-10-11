package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HandsRendererMixin {
    @Inject(method="submitArmWithItem",at=@At("HEAD"),cancellable=true)
    private void rowing$stow(AbstractClientPlayer player,float partial,float pitch,
            InteractionHand hand,float swing,ItemStack item,float height,PoseStack stack,SubmitNodeCollector collector,int light,CallbackInfo ci) {
        var pose=RowingClient.capture(player,partial,true);
        boolean left=hand==InteractionHand.MAIN_HAND ? player.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT
                : player.getMainArm()!=net.minecraft.world.entity.HumanoidArm.LEFT;
        if(pose!=null&&pose.owns(left))ci.cancel();
    }
}
