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
                            }
                        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client->services.integrations());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->RowingClient.clear());
        ClientTickEvents.END_CLIENT_TICK.register(client->RowingClient.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->RowingClient.clear());
    }
    private static final class Services implements ClientPlatformServices {
        private boolean nea,pal,unsupported;
        private String status="Compatibility is local to this client.";
        private void integrations() {
            try {
                if(isModLoaded("firstperson")) {
                    if(!modVersion("firstperson").startsWith("2.7.3")) unsupported=true;
                    else io.github.derkottersberg.seamlessrowing.fabric.integration.FirstPersonAdapter.register();
                }
                if(isModLoaded("notenoughanimations")) {
                    if(!modVersion("notenoughanimations").startsWith("1.12.5")) unsupported=true;
                    else {io.github.derkottersberg.seamlessrowing.fabric.integration.NeaAdapter.register();nea=true;}
                }
                if(isModLoaded("player_animation_library")) {
                    if(!modVersion("player_animation_library").equals("1.2.8+mc.26.3")) unsupported=true;
                    else pal=true;
                }
            } catch(LinkageError incompatible) {
                unsupported=true;
                org.slf4j.LoggerFactory.getLogger("Seamless Rowing").warn("Optional animation API unavailable; preserving existing rendering",incompatible);
            }
            status=unsupported ? "Unsupported animation API: rowing disabled."
                    : (nea ? "NEA priority 1501. " : "")+(isModLoaded("firstperson") ? "FirstPersonModel. " : "")+(pal ? "PAL priority respected." : "");
        }
        public boolean animationSchedulerActive(){return nea;}
        public boolean unsupportedAnimationIntegration(){return unsupported;}
        public String integrationStatus(){return status;}
        public boolean externalAnimationActive(net.minecraft.client.player.AbstractClientPlayer player) {
            if(!pal) return false;
            try{return io.github.derkottersberg.seamlessrowing.fabric.integration.PalAdapter.active(player);}
            catch(RuntimeException|LinkageError invalid){unsupported=true;status="PAL rig unavailable: rowing safely disabled.";
                org.slf4j.LoggerFactory.getLogger("Seamless Rowing").warn(status,invalid);return true;}
        }
        public Path configDirectory(){return FabricLoader.getInstance().getConfigDir();}
        public boolean isModLoaded(String id){return FabricLoader.getInstance().isModLoaded(id);}
        public String modVersion(String id){return FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getVersion().getFriendlyString()).orElse("");}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
        public boolean customPlayerModel(net.minecraft.client.model.player.PlayerModel model) {
            if(!isModLoaded("entity_model_features")) return false;
            if(!modVersion("entity_model_features").startsWith("3.3.11")) {status="Unsupported EMF model API: existing visuals preserved.";return true;}
            try{
                boolean custom=io.github.derkottersberg.seamlessrowing.fabric.integration.EmfAdapter.custom(model);
                if(custom) status="Custom EMF rigs keep their existing animations.";
                return custom;
            }
            catch(RuntimeException|LinkageError invalid){status="EMF model unavailable: existing visuals preserved.";return true;}
        }
    }
}
