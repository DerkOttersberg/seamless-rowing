package qa.rowing.platform.forge;
import qa.rowing.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModList;
@Mod("seamlessrowing_qa")
public final class QaForge {
    public QaForge(FMLJavaModLoadingContext context) {
        if(FMLEnvironment.dist.isClient()) {
            QaPlatform.initialize(ModList::isLoaded);
            var program=new RowingQaClient();program.initialize();
            net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event->program.tick(net.minecraft.client.Minecraft.getInstance()));
        }
    }
}
