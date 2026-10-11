package qa.rowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.RowingState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=PlayerModel.class,priority=400)
public class BodySurfaceProbe {
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("RETURN"),order=1000)
    private void source(AvatarRenderState state,CallbackInfo ci) {
        qa.rowing.SurfaceGeometry.source((PlayerModel)(Object)this);
    }
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("RETURN"),order=1200)
    private void geometry(AvatarRenderState state,CallbackInfo ci) {
        qa.rowing.SurfaceGeometry.body((PlayerModel)(Object)this,((RowingState)state).rowingPose());
        if(qa.rowing.QaPlatform.isModLoaded("entity_model_features"))
            qa.rowing.FreshProbe.inspect((PlayerModel)(Object)this,state);
    }
}
