package qa.rowing.mixin;

import qa.rowing.RenderedPaddles;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** EMF overrides the native transform, sometimes without invoking its superclass. */
@Pseudo
@Mixin(targets="traben.entity_model_features.models.parts.EMFModelPartVanilla",remap=false)
public class EmfPaddleTransformProbe {
    @Inject(method={"translateAndRotate","method_22703","m_104299_"},at=@At("RETURN"))
    private void measure(PoseStack stack,CallbackInfo ci) {RenderedPaddles.inspect((ModelPart)(Object)this,stack);}
}
