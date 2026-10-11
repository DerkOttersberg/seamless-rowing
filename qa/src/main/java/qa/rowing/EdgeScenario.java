package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.nio.file.*;

/** Genuine native terrain, mount, dimension, death and disconnect transitions. */
public final class EdgeScenario {
    private static int phase,age,assertions,dismounted,remounted;
    private static boolean started;
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> original;
    private static float bubbleAngle;
    public static boolean started(){return started;}
    public static void renderCleanup(Minecraft mc){if(started&&(phase==7||phase==6&&mc.player!=null&&!mc.player.isAlive()))tick(mc);}
    private static void command(Minecraft mc,String command){
        var server=mc.getSingleplayerServer();
        server.execute(()->server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),command));
    }
    private static void ground(Minecraft mc,int kind){
        var server=mc.getSingleplayerServer();server.execute(()->{
            var level=server.overworld();var player=server.getPlayerList().getPlayerByName("RowQA");var boat=player.getVehicle();
            for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){
                level.setBlock(new BlockPos(x,-61,z),(kind==2?Blocks.SOUL_SAND:Blocks.SAND).defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-60,z),(kind==2?Blocks.BUBBLE_COLUMN:Blocks.SAND).defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-59,z),(kind==0?Blocks.ICE:kind==1?Blocks.DIRT:Blocks.BUBBLE_COLUMN).defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-58,z),Blocks.AIR.defaultBlockState(),2);
            }
            boat.setPos(0,kind==2?-58.1:-58,0);boat.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        });
    }
    public static void tick(Minecraft mc){
        if(!started){if(mc.player==null||mc.player.getVehicle()==null)return;started=true;original=mc.level.dimension();}
        if(++age>400)throw new IllegalStateException("Native edge phase timed out: "+phase);
        mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.keyUp.setDown(phase<4);mc.options.keyLeft.setDown(phase<4);
        if(mc.player!=null)mc.player.setXRot(-15);
        if(phase<4){
            if(age==1&&phase>0)ground(mc,phase-1);
            if(age>15&&mc.player.getVehicle()!=null){
                if(RowingClient.capture(mc.player,1,false)!=null)assertions++;
                if(phase==3)bubbleAngle=Math.max(bubbleAngle,Math.abs(((net.minecraft.world.entity.vehicle.boat.AbstractBoat)mc.player.getVehicle()).getBubbleAngle(1)));
            }
            if(age==45){phase++;age=0;}
        }else if(phase==4){
            if(age%16==1)command(mc,"ride RowQA dismount");
            if(age%16==9)command(mc,"ride RowQA mount @e[type=minecraft:oak_boat,limit=1,sort=nearest]");
            if(mc.player.getVehicle()==null){if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dismounted pose retained");dismounted++;}
            else remounted++;
            if(age==96){if(dismounted<3||remounted<3)throw new IllegalStateException("Rapid mounts did not traverse native states");phase++;age=0;command(mc,"execute in minecraft:the_nether run tp RowQA 0 100 0");}
        }else if(phase==5){
            if(mc.level!=null&&!mc.level.dimension().equals(original)&&age>10){
                if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dimension transition retained boat pose");assertions++;phase++;age=0;command(mc,"gamemode survival RowQA");
            }
        }else if(phase==6){
            if(age==100)command(mc,"kill RowQA");
            if(age==120)System.out.println("EDGE_DEATH_STATE health="+(mc.player==null?-1:mc.player.getHealth())+" alive="+(mc.player!=null&&mc.player.isAlive()));
            if(mc.player!=null&&!mc.player.isAlive()){
                if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dead player retained pose");assertions++;
                phase++;age=0;mc.disconnect(new TitleScreen(),false);
            }
        }else if(phase==7&&mc.level==null&&mc.player==null){
            boolean pass=assertions>20&&bubbleAngle>0&&FrameContact.samples()>10&&FrameContact.maximum()<=1;
            try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-edge.json"),"{\"pass\":"+pass+",\"assertions\":"+assertions+",\"nativeDismountTicks\":"+dismounted+",\"nativeRemountTicks\":"+remounted+",\"nativeBubbleAngle\":"+bubbleAngle+",\"dimensionDeathDisconnect\":true}");}catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_EDGE_COMPLETE pass="+pass);mc.stop();
        }
    }
}


