package qa.rowing.mixin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SoundManager.class)
public class PaddleSoundProbe {
    @Inject(method="play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",at=@At("HEAD"))
    private void capture(SoundInstance sound,CallbackInfo ci){qa.rowing.SoundProbe.record(sound);}
}
