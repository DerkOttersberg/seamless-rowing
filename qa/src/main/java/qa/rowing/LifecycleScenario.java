package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.*;
import net.minecraft.world.entity.vehicle.Boat;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public final class LifecycleScenario {
    private static int tick,assertions;private static CompletableFuture<Void> reload;
    public static void tick(Minecraft mc) {
        if(mc.isPaused()) return;
        tick++;
        mc.options.keyUp.setDown(tick<95);
        mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(-15);
        if(tick==30) {check(RowingClient.capture(mc.player,1,false)!=null,"No initial track");assertions++;RowingClient.clear();check(RowingClient.capture(mc.player,1,false)==null,"Stale track after clear");assertions++;}
        if(tick==35) {check(RowingClient.capture(mc.player,1,false)!=null,"No recovery after clear");assertions++;reload=mc.reloadResourcePacks();}
        if(tick>=60&&reload!=null&&!reload.isDone()) {tick=59;return;}
        if(tick==65) {reload.join();check(RowingClient.capture(mc.player,1,false)!=null,"No recovery after resource reload");assertions++;}
        if(tick==72) {
            var boat=(Boat)mc.player.getVehicle();boat.setHurtTime(9);boat.setDamage(5);boat.setHurtDir(1);
        }
        if(tick%2==0&&tick>66&&tick<86) Screenshot.grab(mc.gameDirectory,"wobble-"+tick+".png",mc.getMainRenderTarget(),m->{});
        if(tick==110) {
            var boat=(Boat)mc.player.getVehicle();
            System.out.println("ROWING_STOP_NATIVE left="+boat.getPaddleState(0)+" right="+boat.getPaddleState(1)+" up="+mc.options.keyUp.isDown());
            check(RowingClient.capture(mc.player,1,false)==null,"Stopped boat retained pose");assertions++;
        }
        if(tick==112) {
            boolean pass=FrameContact.samples()>10&&FrameContact.maximum()<=1;
            try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-lifecycle.json"),"{\"pass\":"+pass+",\"assertions\":"+assertions+",\"nativeSamples\":"+FrameContact.samples()+",\"maximumErrorPixels\":"+FrameContact.maximum()+"}");}
            catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_LIFECYCLE_COMPLETE pass="+pass);mc.stop();
        }
    }
    private static void check(boolean value,String error){if(!value)throw new IllegalStateException(error);}
}
