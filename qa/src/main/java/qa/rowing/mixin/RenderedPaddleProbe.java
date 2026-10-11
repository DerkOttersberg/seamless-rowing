package qa.rowing.mixin;

import qa.rowing.RenderedPaddles;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class RenderedPaddleProbe {
    @Inject(method="translateAndRotate",at=@At("RETURN"))
    private void measure(PoseStack stack,CallbackInfo ci) {
        RenderedPaddles.inspect((ModelPart)(Object)this,stack);
    }
}
