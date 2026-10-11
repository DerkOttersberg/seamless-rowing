package qa.rowing;
import net.minecraft.client.*;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import java.nio.file.Files;
public final class PassengerScenario {
    private static int ticks;
    public static void tick(Minecraft mc,String role,int tick){
        if(role.equals("rower")&&tick>=425){
            mc.options.keyUp.setDown(true);mc.options.keyDown.setDown(false);mc.options.keyLeft.setDown(false);
            mc.options.keyUse.setDown(false);mc.options.keyAttack.setDown(false);
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(-15);
            if(tick==440)try{Files.writeString(mc.gameDirectory.toPath().getParent().resolve("mp-passenger-request"),"native-stroke");}
            catch(java.io.IOException error){throw new IllegalStateException(error);}
        }
        if(role.equals("observer")&&mc.player.getVehicle()!=null){
            if(RowingClient.capture(mc.player,1,true)!=null)throw new IllegalStateException("Passenger took controller animation");
            ticks++;
        }
    }
    public static int ticks(){return ticks;}
}
