package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public interface BoatState {
    RowingPose rowingBoatPose();
    void rowingBoatPose(RowingPose pose);
    AvatarRenderState rowingLocalPlayer();
    void rowingLocalPlayer(AvatarRenderState state);
}
