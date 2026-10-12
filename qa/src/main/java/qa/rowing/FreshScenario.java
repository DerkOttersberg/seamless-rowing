package qa.rowing;

import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import java.nio.file.Files;
import java.util.Set;

public final class FreshScenario {
    private static int shaderCoverageWaitTicks;
    private static int tick,owned,useReleased,stopped,walking,realCameraTicks;private static boolean reloadDone;
    private static java.util.concurrent.CompletableFuture<Void> reload;
    public static void tick(Minecraft mc) {
        if(mc.isPaused()||mc.gui.screen()!=null||mc.player==null||mc.level==null)return;
        if(tick==0&&mc.player.getVehicle()==null)return;
        // Shader compilation can let many client ticks pass between rendered
        // frames. Preserve real controls while waiting for native model samples.
        if(Boolean.getBoolean("qa.rowing.shaderCoverage")&&
                (tick==180&&!FreshProbe.rowingCoverageReady()||tick==212&&!FreshProbe.restoredCoverageReady())){
            if(++shaderCoverageWaitTicks>400)throw new IllegalStateException("Shader native model coverage timed out");
            return;
        }
        tick++;
        if(tick==1&&Boolean.getBoolean("qa.rowing.realcamera"))com.xtracr.realcamera.config.ConfigFile.config().enabled=true;
        if(Boolean.getBoolean("qa.rowing.realcamera")&&com.xtracr.realcamera.RealCameraCore.isActive())realCameraTicks++;
        mc.options.setCameraType(tick<85?CameraType.FIRST_PERSON:tick<155?CameraType.THIRD_PERSON_BACK:CameraType.THIRD_PERSON_FRONT);
        // Sweep the real camera toward each grip instead of measuring only off-screen hands.
        float yaw=tick<85?(tick<30?-55:tick<54?0:55):0;
        mc.player.setYRot(mc.player.getVehicle()==null?yaw:mc.player.getVehicle().getYRot()+yaw);
        mc.player.setXRot(tick<85?60:tick<155?20:-15);
        if(qa.rowing.QaPlatform.isModLoaded("punchy")&&(tick==1||tick==11)) {
            try{RowingClient.save(new io.github.derkottersberg.seamlessrowing.config.ClientSettings(tick==11,true,true,true,true));}
            catch(java.io.IOException error){throw new IllegalStateException(error);}
        }
        mc.options.keyUp.setDown(tick>=12&&tick<70||tick>=100&&tick<140||tick>=160&&tick<200||tick>=230&&tick<250);
        mc.options.keyLeft.setDown(tick>=45&&tick<55);
        mc.options.keyUse.setDown(tick>=38&&tick<48||tick>=114&&tick<124);
        if(tick==28||tick==105)mc.getSingleplayerServer().execute(()-> {
            var server=mc.getSingleplayerServer();var source=server.createCommandSourceStack();
            server.getCommands().performPrefixedCommand(source,"item replace entity RowQA weapon.mainhand with minecraft:bow");
            server.getCommands().performPrefixedCommand(source,"give RowQA minecraft:arrow 64");
        });
        if(tick==38||tick==114)mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
        var pose=RowingClient.capture(mc.player,1,mc.options.getCameraType().isFirstPerson());
        if(pose!=null&&(pose.ownLeft()||pose.ownRight()))owned++;
        if(mc.player.isUsingItem()&&pose==null)useReleased++;
        if((tick>=78&&tick<85||tick>=148&&tick<155||tick>=208&&tick<215)&&pose==null)stopped++;
        if(tick==90)reload=mc.reloadResourcePacks();
        if(tick==96&&reload!=null&&!reload.isDone()){tick=95;return;}
        if(tick==97){reload.join();reloadDone=true;}
        if(tick==218)mc.getSingleplayerServer().execute(()-> {
            var server=mc.getSingleplayerServer();var level=server.overworld();
            for(int x=-4;x<=8;x++)for(int z=-3;z<=8;z++)level.setBlock(new BlockPos(x,-58,z),Blocks.OAK_PLANKS.defaultBlockState(),2);
            var player=server.getPlayerList().getPlayerByName("RowQA");player.stopRiding();
            player.teleportTo(level,-3,-57,1,Set.of(),0,0,true);
        });
        if(tick>225&&mc.player.getVehicle()==null&&pose==null)walking++;
        if(tick%2==0&&!Boolean.getBoolean("qa.rowing.cadence"))Screenshot.grab(mc.gameDirectory,String.format("fresh-%03d.png",tick),mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==275) {
            mc.options.keyUp.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyLeft.setDown(false);
            boolean pass=owned>40&&useReleased>3&&stopped>5&&walking>20&&reloadDone&&FrameContact.samples()>10&&FrameContact.maximum()<=1
                    &&FrameContact.duplicates()==0&&FrameContact.views().size()==3
                    &&(!Boolean.getBoolean("qa.rowing.realcamera")||realCameraTicks>30);
            var result=new java.util.LinkedHashMap<String,Object>();
            result.putAll(java.util.Map.of("ownedTicks",owned,"itemUseReleaseTicks",useReleased,"stoppedTicks",stopped,"walkingAfterDismountTicks",walking,"resourceReload",reloadDone,"nativeSamples",FrameContact.samples(),"maximumErrorPixels",FrameContact.maximum(),"pass",pass));
            result.put("realCameraActiveTicks",realCameraTicks);result.put("duplicateHandSubmissions",FrameContact.duplicates());result.put("views",FrameContact.views());
            try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-fresh-behavior.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}
            catch(java.io.IOException failed){throw new IllegalStateException(failed);}
            System.out.println("ROWING_FRESH_COMPLETE "+result);mc.stop();
        }
    }
}
