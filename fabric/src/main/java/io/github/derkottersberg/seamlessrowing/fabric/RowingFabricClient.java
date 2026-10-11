package io.github.derkottersberg.seamlessrowing.fabric;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import io.github.derkottersberg.seamlessrowing.internal.ClientPlatformServices;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;

public final class RowingFabricClient implements ClientModInitializer {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),
            "key.categories.seamlessrowing");
    @Override public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SETTINGS);
        Services services=new Services();RowingClient.initialize(services);
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                .registerReloadListener(new Reload(services));
        ClientLifecycleEvents.CLIENT_STARTED.register(client->services.integrations());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->RowingClient.clear());
        ClientTickEvents.END_CLIENT_TICK.register(client->RowingClient.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->RowingClient.clear());
    }
    private static final class Reload extends net.minecraft.server.packs.resources.SimplePreparableReloadListener<float[]>
            implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
        private final Services services;Reload(Services services){this.services=services;}
        public ResourceLocation getFabricId(){return new ResourceLocation("seamlessrowing","pose_clip");}
        protected float[] prepare(net.minecraft.server.packs.resources.ResourceManager resources,net.minecraft.util.profiling.ProfilerFiller profiler){return io.github.derkottersberg.seamlessrowing.client.StrokeProfile.prepare(resources);}
        protected void apply(float[] clip,net.minecraft.server.packs.resources.ResourceManager resources,net.minecraft.util.profiling.ProfilerFiller profiler){io.github.derkottersberg.seamlessrowing.client.StrokeProfile.apply(clip);RowingClient.clear();services.resourcesReloaded(resources);}
    }
    private static final class Services extends io.github.derkottersberg.seamlessrowing.integration.IntegrationServices {
        public Path configDirectory(){return FabricLoader.getInstance().getConfigDir();}
        public boolean isModLoaded(String id){return FabricLoader.getInstance().isModLoaded(id);}
        public String modVersion(String id){return FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getVersion().getFriendlyString()).orElse("");}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
    }
}
