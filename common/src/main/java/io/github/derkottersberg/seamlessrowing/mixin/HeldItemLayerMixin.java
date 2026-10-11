package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandLayer.class)
public class HeldItemLayerMixin {
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void stow(LivingEntity player,ItemStack item,ItemDisplayContext display,HumanoidArm arm,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){
        var context=RenderContext.current();if(context!=null&&context.player==player&&context.rowingPose()!=null&&context.rowingPose().owns(arm==HumanoidArm.LEFT))ci.cancel();
    }
}
