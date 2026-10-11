package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import io.github.derkottersberg.swordthrow.client.SwordThrowClient;
import net.minecraft.client.*;
import java.nio.file.*;
import java.util.Arrays;

/** Tests existing Throw Weapons input and server action while all available siblings are installed. */
public final class SuiteScenario {
    private static int tick,yielded,recovered,owned;
    private static KeyMapping throwKey;
    public static void tick(Minecraft mc){
        tick++;
        if(throwKey==null)throwKey=Arrays.stream(mc.options.keyMappings).filter(k->k.getName().equals("key.swordthrow.throw")).findFirst().orElseThrow();
        mc.options.keyUp.setDown(true);mc.options.setCameraType(tick<90?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(tick<90?40:-15);
        if(tick==1||tick==90){var server=mc.getSingleplayerServer();server.execute(()->server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"item replace entity RowQA weapon.mainhand with minecraft:diamond_axe"));}
        throwKey.setDown(tick>=20&&tick<45||tick>=110&&tick<135);
        var pose=RowingClient.capture(mc.player,1,mc.options.getCameraType().isFirstPerson());
        if(SwordThrowClient.poseStateFor(mc.player.getId())!=null){if(pose!=null)throw new IllegalStateException("Rowing stole an active Throw Weapons pose");yielded++;}
        else if(pose!=null){owned++;if(tick>=60&&tick<85||tick>=150)recovered++;}
        if(tick==175){
            boolean pass=yielded>20&&owned>20&&recovered>15;
            try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-suite.json"),"{\"pass\":"+pass+",\"throwPoseReleasedTicks\":"+yielded+",\"ownedTicks\":"+owned+",\"recoveredTicks\":"+recovered+"}");}catch(Exception failure){throw new RuntimeException(failure);}
            throwKey.setDown(false);System.out.println("ROWING_SUITE_COMPLETE pass="+pass);mc.stop();
        }
    }
}
