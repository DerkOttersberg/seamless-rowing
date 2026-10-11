package qa.rowing.platform.neoforge;
import qa.rowing.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
@Mod(value="seamlessrowing_qa",dist=Dist.CLIENT)
public final class QaNeoForge {
    public QaNeoForge(ModContainer container) {
        QaPlatform.initialize(ModList.get()::isLoaded);
        var program=new RowingQaClient();program.initialize();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event)->program.tick(net.minecraft.client.Minecraft.getInstance()));
    }
}
