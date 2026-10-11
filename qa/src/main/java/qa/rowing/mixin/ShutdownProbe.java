package qa.rowing.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ShutdownProbe {
    @Inject(method="stop",at=@At("HEAD"))
    private void anatomy(CallbackInfo ci) {
        qa.rowing.ArmAnatomy.write((Minecraft)(Object)this);
        qa.rowing.SurfaceGeometry.write((Minecraft)(Object)this);
    }
}
