package io.github.derkottersberg.seamlessrowing.mixin;
import com.mojang.blaze3d.vertex.*;
import io.github.derkottersberg.seamlessrowing.client.SurfaceRenderer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only our mutable surfaces bypass static cuboid caches; all other parts retain their renderer. */
@Mixin(value=ModelPart.class,priority=500)
public class SurfaceRenderMixin {
    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",at=@At("HEAD"),cancellable=true)
    private void rowing$surface(PoseStack stack,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha,CallbackInfo ci) {
        var part=(ModelPart)(Object)this;
        if(SurfaceRenderer.owns(part)){SurfaceRenderer.render(part,stack,consumer,light,overlay,red,green,blue,alpha);ci.cancel();}
    }
}


