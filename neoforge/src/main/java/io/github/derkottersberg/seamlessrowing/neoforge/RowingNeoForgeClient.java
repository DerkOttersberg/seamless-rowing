package io.github.derkottersberg.seamlessrowing.neoforge;
import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.integration.IntegrationServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;
final class RowingNeoForgeClient {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("seamlessrowing","controls")));
    private static final Services SERVICES=new Services();
    static void initialize(net.neoforged.fml.ModContainer container) {
        var bus=container.getEventBus();
        bus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event)->{event.registerCategory(SETTINGS.getCategory());event.register(SETTINGS);});
        RowingClient.initialize(SERVICES);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event)->{SERVICES.integrations();RowingClient.tick();});
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event)->RowingClient.clear());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.GameShuttingDownEvent event)->RowingClient.clear());
        bus.addListener((net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event)->
            event.addListener(Identifier.fromNamespaceAndPath("seamlessrowing","pose_clip"),new StrokeReloadListener(SERVICES::resourcesReloaded)));
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,(mod,parent)->new RowingSettingsScreen(parent));
    }
    private static final class Services extends IntegrationServices {
        public Path configDirectory(){return FMLPaths.CONFIGDIR.get();}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
        public boolean isModLoaded(String id){return ModList.get().isLoaded(id);}
        public String modVersion(String id){return ModList.get().getModContainerById(id).map(m->m.getModInfo().getVersion().toString()).orElse("");}
    }
}
