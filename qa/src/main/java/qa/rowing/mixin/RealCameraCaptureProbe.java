package qa.rowing.mixin;

import qa.rowing.CameraCapturePass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="com.xtracr.realcamera.RealCameraCore",remap=false)
public class RealCameraCaptureProbe {
    @Inject(method="computeCamera",at=@At("HEAD"))
    private static void begin(Minecraft mc,float partial,CallbackInfo ci) {CameraCapturePass.active=true;}
    @Inject(method="computeCamera",at=@At("RETURN"))
    private static void end(Minecraft mc,float partial,CallbackInfo ci) {CameraCapturePass.active=false;}
}
