package io.github.derkottersberg.seamlessrowing.forge;
import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.integration.IntegrationServices;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.event.TickEvent;
import java.nio.file.Path;
final class RowingForgeClient {
    private static final KeyMapping SETTINGS=new KeyMapping("key.seamlessrowing.settings",InputConstants.UNKNOWN.getValue(),"key.categories.seamlessrowing");
    private static final Services SERVICES=new Services();
    static void initialize(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context){
        context.getModEventBus().addListener((net.minecraftforge.client.event.RegisterKeyMappingsEvent event)->event.register(SETTINGS));
        context.getModEventBus().addListener((net.minecraftforge.client.event.RegisterClientReloadListenersEvent event)->event.registerReloadListener(new StrokeReloadListener(SERVICES::resourcesReloaded)));
        RowingClient.initialize(SERVICES);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event)->{if(event.phase==TickEvent.Phase.END){SERVICES.integrations();RowingClient.tick();}});
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event)->RowingClient.clear());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.GameShuttingDownEvent event)->RowingClient.clear());
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
            ()->new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((client,parent)->new RowingSettingsScreen(parent)));
    }
    private static final class Services extends IntegrationServices {
        public Path configDirectory(){return FMLPaths.CONFIGDIR.get();}
        public boolean consumeSettingsClick(){return SETTINGS.consumeClick();}
        public boolean isModLoaded(String id){return ModList.get().isLoaded(id);}
        public String modVersion(String id){return ModList.get().getModContainerById(id).map(m->m.getModInfo().getVersion().toString()).orElse("");}
    }
}
