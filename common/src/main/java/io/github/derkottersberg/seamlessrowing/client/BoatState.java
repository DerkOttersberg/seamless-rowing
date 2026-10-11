package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public interface BoatState {
    boolean rowingRestorePaddles();
    void rowingRestorePaddles(boolean restore);
    RowingPose rowingBoatPose();
    void rowingBoatPose(RowingPose pose);
    AvatarRenderState rowingLocalPlayer();
    void rowingLocalPlayer(AvatarRenderState state);
}
