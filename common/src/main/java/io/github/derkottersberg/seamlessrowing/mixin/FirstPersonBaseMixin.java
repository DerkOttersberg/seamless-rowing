package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only the scoped world-projected hands pass omits the full player base model. */
@Mixin(LivingEntityRenderer.class)
public class FirstPersonBaseMixin {
    @Inject(method="getRenderType",at=@At("HEAD"),cancellable=true)
    private void rowing$handsBase(LivingEntity entity,boolean visible,boolean translucent,boolean glowing,
            CallbackInfoReturnable<RenderType> cir){
        var context=RenderContext.current();
        if(context!=null&&context.player==entity&&context.rowingHandsOnly()&&context.rowingPose()!=null)cir.setReturnValue(null);
    }
}
