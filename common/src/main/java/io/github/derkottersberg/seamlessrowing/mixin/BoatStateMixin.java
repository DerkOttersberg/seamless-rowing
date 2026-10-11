package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.renderer.entity.state.*;
import org.spongepowered.asm.mixin.*;

@Mixin(BoatRenderState.class)
public class BoatStateMixin implements BoatState {
    @Unique private boolean rowing$restorePaddles;
    public boolean rowingRestorePaddles(){return rowing$restorePaddles;}
    public void rowingRestorePaddles(boolean restore){rowing$restorePaddles=restore;}
    @Unique private RowingPose rowing$pose;
    @Unique private AvatarRenderState rowing$local;
    public RowingPose rowingBoatPose(){return rowing$pose;}
    public void rowingBoatPose(RowingPose pose){rowing$pose=pose;}
    public AvatarRenderState rowingLocalPlayer(){return rowing$local;}
    public void rowingLocalPlayer(AvatarRenderState player){rowing$local=player;}
}
