package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.HandsState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.*;

@Mixin(FirstPersonHandsAndItemsRenderState.class)
public class HandsStateMixin implements HandsState {
    @Unique private boolean rowing$main,rowing$off;
    public boolean rowingOwnMain(){return rowing$main;}
    public boolean rowingOwnOff(){return rowing$off;}
    public void rowingOwnership(boolean main,boolean off){rowing$main=main;rowing$off=off;}
}
