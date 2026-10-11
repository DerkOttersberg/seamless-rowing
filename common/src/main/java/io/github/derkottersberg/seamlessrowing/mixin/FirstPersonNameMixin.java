package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityRenderer.class)
public class FirstPersonNameMixin {
    @Inject(method="shouldShowName",at=@At("RETURN"),cancellable=true)
    private void name(Entity entity,CallbackInfoReturnable<Boolean> ci){
        var context=RenderContext.current();
        if(context!=null&&context.player==entity&&context.rowingHandsOnly())ci.setReturnValue(false);
    }
}
