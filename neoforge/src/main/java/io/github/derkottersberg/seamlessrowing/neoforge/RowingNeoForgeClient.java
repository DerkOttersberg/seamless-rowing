package io.github.derkottersberg.seamlessrowing.neoforge;
import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.integration.IntegrationServices;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;
import java.nio.file.Path;
final class RowingNeoForgeClient {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),"key.categories.seamlessrowing");
    private static final Services SERVICES=new Services();
    static void initialize(net.neoforged.fml.ModContainer container){
        var bus=container.getEventBus();
        bus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event)->event.register(SETTINGS));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event)->event.registerReloadListener(new StrokeReloadListener(SERVICES::resourcesReloaded)));
        RowingClient.initialize(SERVICES);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event)->{SERVICES.integrations();RowingClient.tick();});
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event)->RowingClient.clear());
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,(mod,parent)->new RowingSettingsScreen(parent));
    }
    private static final class Services extends IntegrationServices {
        public Path configDirectory(){return FMLPaths.CONFIGDIR.get();}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
        public boolean isModLoaded(String id){return ModList.get().isLoaded(id);}
        public String modVersion(String id){return ModList.get().getModContainerById(id).map(m->m.getModInfo().getVersion().toString()).orElse("");}
    }
}
