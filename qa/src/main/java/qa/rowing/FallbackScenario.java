package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.*;
import java.nio.file.*;

public final class FallbackScenario {
    private static int tick,rigid;
    public static void tick(Minecraft mc) {
        tick++;mc.options.keyUp.setDown(tick<80);
        mc.options.setCameraType(tick<50?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);
        mc.player.setXRot(tick<50?40:-15);
        if(tick>10&&tick<80) {
            if(RowingClient.capture(mc.player,1,true)!=null) throw new IllegalStateException("Fallback replaced native first-person overlay");
            var third=RowingClient.capture(mc.player,1,false);
            if(third!=null&&!third.articulated())rigid++;
        }
        if(tick==35||tick==65) Screenshot.grab(mc.gameDirectory,"fallback-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,m->{});
        if(tick==100) {
            try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-fallback.json"),"{\"pass\":"+(rigid>0)+",\"rigidTicks\":"+rigid+",\"preservedFirstPersonOverlay\":true}");}
            catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_FALLBACK_COMPLETE rigidTicks="+rigid);mc.stop();
        }
    }
}
