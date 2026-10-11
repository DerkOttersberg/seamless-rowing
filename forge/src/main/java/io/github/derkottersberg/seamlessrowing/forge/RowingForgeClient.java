package io.github.derkottersberg.seamlessrowing.forge;
import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.integration.IntegrationServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
final class RowingForgeClient {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("seamlessrowing","controls")));
    private static final Services SERVICES=new Services();
    static void initialize(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context) {
        net.minecraftforge.client.event.RegisterKeyMappingsEvent.BUS.addListener(event->event.register(SETTINGS));
        RowingClient.initialize(SERVICES);
        net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event->{SERVICES.integrations();RowingClient.tick();});
        net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event->RowingClient.clear());
        net.minecraftforge.event.GameShuttingDownEvent.BUS.addListener(event->RowingClient.clear());
        net.minecraftforge.client.event.RegisterClientReloadListenersEvent.BUS.addListener(event->
            event.registerReloadListener(new StrokeReloadListener(SERVICES::resourcesReloaded)));
        context.getContainer().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
            ()->new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((client,parent)->new RowingSettingsScreen(parent)));
    }
    private static final class Services extends IntegrationServices {
        public Path configDirectory(){return FMLPaths.CONFIGDIR.get();}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
        public boolean isModLoaded(String id){return ModList.isLoaded(id);}
        public String modVersion(String id){return ModList.getModContainerById(id).map(m->m.getModInfo().getVersion().toString()).orElse("");}
    }
}
