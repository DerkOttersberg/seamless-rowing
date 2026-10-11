package qa.rowing.mixin;

import qa.rowing.MatrixScenario;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class SkinProbe {
    @Inject(method="getSkin",at=@At("RETURN"),cancellable=true)
    private void nativeSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if(!System.getProperty("qa.rowing.scenario","").equals("matrix")) return;
        if(!((AbstractClientPlayer)(Object)this).getName().getString().equals("RowQA")) return;
        var skin=MatrixScenario.skin();if(skin!=null) cir.setReturnValue(skin);
    }
}
