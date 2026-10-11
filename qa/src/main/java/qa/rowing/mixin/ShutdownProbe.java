package qa.rowing.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ShutdownProbe {
    @Inject(method="stop",at=@At("HEAD"))
    private void anatomy(CallbackInfo ci) {
        qa.rowing.HeadCompileProbe.write((Minecraft)(Object)this);
        qa.rowing.LocalHeadDraws.write((Minecraft)(Object)this);
        qa.rowing.SurfaceSubmission.write((Minecraft)(Object)this);
        qa.rowing.FirstPersonOwnership.write((Minecraft)(Object)this);
        qa.rowing.PaddleVisibility.write((Minecraft)(Object)this);
        qa.rowing.ArmAnatomy.write((Minecraft)(Object)this);
        qa.rowing.SurfaceGeometry.write((Minecraft)(Object)this);
        if(qa.rowing.QaPlatform.isModLoaded("entity_model_features"))
            qa.rowing.FreshProbe.write((Minecraft)(Object)this);
    }
}



