package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.Minecraft;
import java.nio.file.Files;

/** Resource reload must preserve an unsupported native boat rig. */
public final class BoatFallbackScenario {
    private static int tick;
    public static void tick(Minecraft mc) {
        tick++;mc.options.keyUp.setDown(true);
        if(tick>15) {
            if(!RowingClient.compatibilityStatus().contains("Custom boat resource geometry"))
                throw new IllegalStateException("Replacement boat pack was not detected");
            if(RowingClient.capture(mc.player,1,false)!=null||RowingClient.capture(mc.player,1,true)!=null)
                throw new IllegalStateException("Rowing replaced unsupported boat rig");
        }
        if(tick==55) {
            try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-boat-fallback.json"),"{\"pass\":true,\"replacementResourceDetected\":true,\"preservedPlayerAndBoatRig\":true}");}
            catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_BOAT_FALLBACK_COMPLETE");mc.stop();
        }
    }
}
