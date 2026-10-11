package io.github.derkottersberg.seamlessrowing.internal;

public interface ClientPlatformServices extends PlatformServices {
    default boolean customPlayerModel(net.minecraft.client.model.player.PlayerModel model){return false;}
    boolean consumeSettingsClick();
    default boolean animationSchedulerActive() { return false; }
    default boolean externalAnimationActive(net.minecraft.client.player.AbstractClientPlayer player) { return false; }
    default boolean unsupportedAnimationIntegration() { return false; }
    default String integrationStatus() { return ""; }
}
