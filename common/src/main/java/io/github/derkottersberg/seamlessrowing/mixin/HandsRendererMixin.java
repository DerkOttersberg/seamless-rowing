package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public class HandsRendererMixin {
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void stow(AbstractClientPlayer player,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,float height,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){
        var row=RowingClient.capture(player,partial,true);
        boolean left=hand==InteractionHand.MAIN_HAND?player.getMainArm()==HumanoidArm.LEFT:player.getMainArm()!=HumanoidArm.LEFT;
        if(row!=null&&row.owns(left))ci.cancel();
    }
}
