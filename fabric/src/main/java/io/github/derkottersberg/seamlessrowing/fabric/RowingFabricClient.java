package io.github.derkottersberg.seamlessrowing.fabric;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import io.github.derkottersberg.seamlessrowing.internal.ClientPlatformServices;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;

public final class RowingFabricClient implements ClientModInitializer {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("seamlessrowing","controls")));
    @Override public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(SETTINGS);
        Services services=new Services();RowingClient.initialize(services);
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                .registerReloadListener(Identifier.fromNamespaceAndPath("seamlessrowing","pose_clip"),
                        new net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener<float[]>() {
                            protected float[] prepare(net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state) {
                                return io.github.derkottersberg.seamlessrowing.client.StrokeProfile.prepare(state.resourceManager());
                            }
                            protected void apply(float[] clip,net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state) {
                                io.github.derkottersberg.seamlessrowing.client.StrokeProfile.apply(clip);RowingClient.clear();
                                services.resourcesReloaded(state.resourceManager());
                            }
                        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client->services.integrations());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->RowingClient.clear());
        ClientTickEvents.END_CLIENT_TICK.register(client->RowingClient.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->RowingClient.clear());
    }
    private static final class Services extends io.github.derkottersberg.seamlessrowing.integration.IntegrationServices {
        public Path configDirectory(){return FabricLoader.getInstance().getConfigDir();}
        public boolean isModLoaded(String id){return FabricLoader.getInstance().isModLoaded(id);}
        public String modVersion(String id){return FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getVersion().getFriendlyString()).orElse("");}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
    }
}
