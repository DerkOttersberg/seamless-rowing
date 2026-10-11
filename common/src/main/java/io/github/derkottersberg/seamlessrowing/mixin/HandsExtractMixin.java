package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public class HandsExtractMixin {
    @Inject(method="extractRenderState",at=@At("RETURN"))
    private void rowing$own(LocalPlayer player,float partial,FirstPersonHandsAndItemsRenderState state,CallbackInfo ci) {
        var pose=RowingClient.capture(player,partial,true);
        boolean main=pose!=null && pose.owns(player.getMainArm()==HumanoidArm.LEFT);
        boolean off=pose!=null && pose.owns(player.getMainArm()!=HumanoidArm.LEFT);
        ((HandsState)state).rowingOwnership(main,off);
    }
}
