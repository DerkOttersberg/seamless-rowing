package qa.rowing.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
@Mixin(SoundManager.class)
public class PaddleSoundProbe {
    @Inject(method="play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",at=@At("HEAD"))
    private void capture(SoundInstance sound,CallbackInfoReturnable<SoundEngine.PlayResult> ci){qa.rowing.SoundProbe.record(sound);}
}
