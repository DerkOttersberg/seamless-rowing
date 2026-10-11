package qa.rowing.mixin;

import qa.rowing.RenderedPaddles;
import io.github.derkottersberg.seamlessrowing.client.BoatState;
import net.minecraft.client.model.object.boat.AbstractBoatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe the model after all pose hooks, without owning a renderer call. */
@Mixin(value=AbstractBoatModel.class,priority=1200)
public class NativePaddleProbe {
    @Shadow @Final private ModelPart leftPaddle;
    @Shadow @Final private ModelPart rightPaddle;
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;)V",at=@At("RETURN"))
    private void measure(BoatRenderState state,CallbackInfo ci) {
        var pose=((BoatState)state).rowingBoatPose();
        qa.rowing.PaddleVisibility.inspect(((BoatState)state).rowingRestorePaddles(),leftPaddle,rightPaddle);
        RenderedPaddles.register(leftPaddle,true,state.rowingTimeLeft,pose);
        RenderedPaddles.register(rightPaddle,false,state.rowingTimeRight,pose);
    }
}
