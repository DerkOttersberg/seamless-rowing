package qa.rowing;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
public final class QaFabricClient implements ClientModInitializer {
    public void onInitializeClient() {
        QaPlatform.initialize(FabricLoader.getInstance()::isModLoaded);
        var program=new RowingQaClient();program.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(program::tick);
    }
}
