package io.github.derkottersberg.seamlessrowing.integration.mixin;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Runtime ownership of the overlapping feature; Punchy's saved preferences remain untouched. */
@Pseudo
@Mixin(targets="punchy.config.PunchyConfig",remap=false)
public class PunchyBoatConfigMixin {
    @Inject(method="disableBoatFirstPersonAnimations",at=@At("RETURN"),cancellable=true)
    private static void rowing$handoff(CallbackInfoReturnable<Boolean> result) {
        if(RowingClient.managesFirstPersonBoat())result.setReturnValue(true);
    }
}
