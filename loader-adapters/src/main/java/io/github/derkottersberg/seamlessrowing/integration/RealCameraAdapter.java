package io.github.derkottersberg.seamlessrowing.integration;

import com.xtracr.realcamera.compat.DisableHelper;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.player.AbstractClientPlayer;

/** Keeps Real Camera's camera binding, with one world-space arm submission during rowing. */
public final class RealCameraAdapter {
    public static void register() {
        DisableHelper.RENDER_MODEL.registerOr(player -> player instanceof AbstractClientPlayer client
                && RowingClient.requestsRowingRig(client));
    }
}
