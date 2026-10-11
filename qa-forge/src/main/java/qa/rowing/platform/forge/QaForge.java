package qa.rowing.platform.forge;
import qa.rowing.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModList;
@Mod("seamlessrowing_qa")
public final class QaForge {
    public QaForge() {
        if(FMLEnvironment.dist.isClient()) {
            QaPlatform.initialize(ModList.get()::isLoaded);
            var program=new RowingQaClient();program.initialize();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent event)->{
                if(event.phase==net.minecraftforge.event.TickEvent.Phase.END)program.tick(net.minecraft.client.Minecraft.getInstance());
            });
        }
    }
}
