package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** World-projected first-person hands omit cape, head, wings and third-party body overlays. */
@Mixin(LivingEntityRenderer.class)
public class FirstPersonLayersMixin {
    @Redirect(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    @SuppressWarnings({"rawtypes","unchecked"})
    private void hands(RenderLayer layer,PoseStack stack,MultiBufferSource buffers,int light,Entity entity,float limb,float amount,float partial,float age,float yaw,float pitch){
        var context=RenderContext.current();
        if(context==null||context.player!=entity||!context.rowingHandsOnly()
            ||layer instanceof RowingArmLayer||layer instanceof HumanoidArmorLayer)
            layer.render(stack,buffers,light,entity,limb,amount,partial,age,yaw,pitch);
    }
}
