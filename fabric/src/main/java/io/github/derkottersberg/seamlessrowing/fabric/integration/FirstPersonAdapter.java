package io.github.derkottersberg.seamlessrowing.fabric.integration;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.Minecraft;

public final class FirstPersonAdapter {
    private FirstPersonAdapter() {}
    public static void register() {
        FirstPersonAPI.registerPlayerHandler((ActivationHandler)()->{
            var mc=Minecraft.getInstance();
            var pose=mc.player==null ? null : RowingClient.capture(mc.player,1,true);
            return pose!=null&&(pose.ownLeft()||pose.ownRight());
        });
    }
}
