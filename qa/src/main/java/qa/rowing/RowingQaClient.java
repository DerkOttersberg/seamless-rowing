package qa.rowing;

import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RowingQaClient {
    private final AtomicBoolean fixture=new AtomicBoolean();
    private int tick;
    private int missingVehicle;
    private int startup;
    private static double maxError;
    private static int samples;
    public static void measure(double error) { maxError=Math.max(maxError,error); samples++; }
    public void initialize() {
        if(!System.getenv().getOrDefault("DISPLAY","").matches(":[1-9][0-9]*") || System.getenv("WAYLAND_DISPLAY")!=null)
            throw new IllegalStateException("QA requires an isolated nonzero X display");
    }
    public void tick(Minecraft mc) {
        mc.options.pauseOnLostFocus=false;
        if(++startup%100==0&&tick==0)System.out.println("ROWING_QA_STARTUP screen="+(mc.screen==null?"none":mc.screen.getClass().getName())+" player="+(mc.player!=null)+" vehicle="+(mc.player==null?null:mc.player.getVehicle()));
        if(mc.player!=null&&!mc.options.hideGui) mc.options.hideGui=true;
        if(System.getProperty("qa.rowing.scenario","").equals("settings")) {SettingsScenario.tick(mc);return;}
        if(System.getProperty("qa.rowing.scenario","").equals("edge")&&EdgeScenario.started()){EdgeScenario.tick(mc);return;}
        if(mc.player==null || mc.level==null || mc.screen!=null) return;
        if(System.getProperty("qa.rowing.scenario","").equals("source")&&qa.rowing.SkinSourceProbe.written){mc.stop();return;}
        if(!System.getProperty("qa.rowing.role","").isEmpty()) { RemoteScenario.tick(mc);return; }
        var server=mc.getSingleplayerServer(); if(server==null) return;
        if(fixture.compareAndSet(false,true)) server.execute(()->{
            var level=server.overworld();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"kill @e[type=!minecraft:player]");
            for(int x=-64;x<=64;x++) for(int z=-64;z<=64;z++) {
                level.setBlock(new BlockPos(x,-61,z),Blocks.SAND.defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-60,z),Blocks.WATER.defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-59,z),Blocks.WATER.defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-58,z),Blocks.AIR.defaultBlockState(),2);
            }
            var player=server.getPlayerList().getPlayerByName("RowQA");
            player.setGameMode(GameType.CREATIVE);player.getInventory().clearContent();player.teleportTo(level,0,-57,0,Set.of(),0,55);
            var boat=new net.minecraft.world.entity.vehicle.Boat(level,0,-58.1,0);
            boat.setPos(0,-58.1,0);level.addFreshEntity(boat);player.startRiding(boat,true);
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"time set 6000");
        });
        if(System.getProperty("qa.rowing.scenario","").matches("fresh|control")) {FreshScenario.tick(mc);return;}
        if(mc.player.getVehicle()!=null&&System.getProperty("qa.rowing.scenario","").equals("lifecycle")){LifecycleScenario.tick(mc);return;}
        if(mc.player.getVehicle()!=null&&System.getProperty("qa.rowing.scenario","").equals("fallback")){FallbackScenario.tick(mc);return;}
        if(System.getProperty("qa.rowing.scenario","").equals("edge")){EdgeScenario.tick(mc);return;}
        if(System.getProperty("qa.rowing.scenario","").equals("boat-fallback")&&mc.player.getVehicle()!=null){BoatFallbackScenario.tick(mc);return;}
        if(System.getProperty("qa.rowing.scenario","").equals("suite")&&mc.player.getVehicle()!=null){SuiteScenario.tick(mc);return;}
        if(mc.player.getVehicle()==null) {
            if(++missingVehicle==60) {
                System.out.println("ROWING_QA_FAILED missing vehicle position="+mc.player.position());mc.stop();
            }
            return;
        }
        missingVehicle=0;
        if(System.getProperty("qa.rowing.scenario","").equals("matrix")) { MatrixScenario.tick(mc);return; }
        tick++;
        if(tick%20==0) System.out.println("ROWING_QA_TICK "+tick+" position="+mc.player.position()+" boat="+mc.player.getVehicle().getType()+" samples="+samples+" error="+maxError);
        mc.player.setYRot(0);mc.player.setXRot(tick<100 ? 45 : tick<200 ? 20 : -15);
        mc.options.setCameraType(tick<100 ? CameraType.FIRST_PERSON : tick<200 ? CameraType.THIRD_PERSON_BACK : CameraType.THIRD_PERSON_FRONT);
        int local=tick%100;
        mc.options.keyUp.setDown(local>=12 && local<72);
        mc.options.keyLeft.setDown(local>=44 && local<56);
        if(tick%1==0 && local>=1 && local<96) {
            String name=String.format("rowing-%03d.png",tick);
            Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),message->{});
        }
        if(tick>=292) {
            mc.options.keyUp.setDown(false);mc.options.keyLeft.setDown(false);
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("rowing-contact.json"),
                    "{\"samples\":"+samples+",\"maximumErrorPixels\":"+maxError+",\"nativeSamples\":"+FrameContact.samples()+
                    ",\"nativeMaximumErrorPixels\":"+FrameContact.maximum()+",\"views\":"+new com.google.gson.Gson().toJson(FrameContact.views())+
                    ",\"pass\":"+(samples>0&&maxError<=1&&FrameContact.samples()>0&&FrameContact.maximum()<=1)+"}\n");
            } catch(Exception failure) {throw new RuntimeException(failure);}
            System.out.println("ROWING_QA_CAPTURE_COMPLETE samples="+samples+" error="+maxError);
            mc.stop();
        }
    }
}
