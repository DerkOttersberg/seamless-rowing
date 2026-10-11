package qa.rowing.mixin;

import qa.rowing.MatrixScenario;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class SkinProbe {
    @Inject(method="getSkinTextureLocation",at=@At("RETURN"),cancellable=true)
    private void nativeSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if(!System.getProperty("qa.rowing.scenario","").equals("matrix")) return;
        if(!((AbstractClientPlayer)(Object)this).getName().getString().equals("RowQA")) return;
        var skin=MatrixScenario.skin();if(skin!=null) cir.setReturnValue(skin.texture());
    }
    @Inject(method="getModelName",at=@At("RETURN"),cancellable=true)
    private void nativeModel(CallbackInfoReturnable<String> cir){
        if(!System.getProperty("qa.rowing.scenario","").equals("matrix")||!((AbstractClientPlayer)(Object)this).getName().getString().equals("RowQA"))return;
        var skin=MatrixScenario.skin();if(skin!=null)cir.setReturnValue(skin.model()==MatrixScenario.SkinModel.SLIM?"slim":"default");
    }
}
