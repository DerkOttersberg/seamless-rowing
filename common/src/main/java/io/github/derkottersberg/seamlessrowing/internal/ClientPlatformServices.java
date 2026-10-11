package io.github.derkottersberg.seamlessrowing.internal;

public interface ClientPlatformServices extends PlatformServices {
    default boolean customPlayerModel(net.minecraft.client.model.player.PlayerModel model){return false;}
    default void prepareRowingModel(net.minecraft.client.model.player.PlayerModel model) {}
    default boolean playerGeometrySupported(net.minecraft.client.model.player.PlayerModel model) {
        return io.github.derkottersberg.seamlessrowing.client.TorsoSurface.supported(model.body)
                && io.github.derkottersberg.seamlessrowing.client.ArmMesh.supported(model.leftArm)
                && io.github.derkottersberg.seamlessrowing.client.ArmMesh.supported(model.rightArm);
    }
    boolean consumeSettingsClick();
    default boolean paddleVisibilityHandoff() {return false;}
    default boolean customBoatGeometry() {return false;}
    default boolean animationSchedulerActive() { return false; }
    default boolean externalAnimationActive(net.minecraft.client.player.AbstractClientPlayer player) { return false; }
    default boolean unsupportedAnimationIntegration() { return false; }
    default String integrationStatus() { return ""; }
}
