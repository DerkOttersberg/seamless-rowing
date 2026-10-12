package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.nio.file.*;

/** Genuine native terrain, mount, dimension, death and disconnect transitions. */
public final class EdgeScenario {
    private static int phase,age,assertions,dismounted,remounted,bubbleOwned,underwaterBubbleOwned;
    private static boolean started,disconnecting;
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> original;
    private static float bubbleAngle;
    private static java.util.concurrent.CompletableFuture<Void> terrainReady;
    private static java.util.UUID boatId;
    public static boolean started(){return started;}
    public static void renderCleanup(Minecraft mc){if(started&&!disconnecting&&(phase==7||phase==6&&mc.player!=null&&!mc.player.isAlive()))tick(mc);}
    private static void command(Minecraft mc,String command){
        var server=mc.getSingleplayerServer();
        server.execute(()->server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),command));
    }
    private static void ground(Minecraft mc,int kind){
        terrainReady=new java.util.concurrent.CompletableFuture<>();
        var server=mc.getSingleplayerServer();server.execute(()->{
            var level=server.overworld();var player=server.getPlayerList().getPlayerByName("RowQA");var boat=player.getVehicle();
            if(boat==null)boat=level.getEntity(boatId);
            if(boat==null)throw new IllegalStateException("Native fixture boat disappeared");
            int cx=boat.blockPosition().getX(),cz=boat.blockPosition().getZ();
            for(int x=cx-24;x<=cx+24;x++)for(int z=cz-24;z<=cz+24;z++){
                level.setBlock(new BlockPos(x,-61,z),(kind==2?Blocks.SOUL_SAND:Blocks.SAND).defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-60,z),(kind==2?Blocks.BUBBLE_COLUMN:kind==3?Blocks.WATER:kind==0?Blocks.ICE:Blocks.DIRT).defaultBlockState(),2);
                // Keep air above the surface. Raising the water through the boat
                // would invoke vanilla underwater passenger ejection immediately.
                level.setBlock(new BlockPos(x,-59,z),Blocks.AIR.defaultBlockState(),2);
                level.setBlock(new BlockPos(x,-58,z),Blocks.AIR.defaultBlockState(),2);
            }
            // Terrain changes beneath the moving boat; preserve native vehicle/passenger synchronization.
            terrainReady.complete(null);
        });
    }
    public static void tick(Minecraft mc){
        if(!started){if(mc.player==null||mc.player.getVehicle()==null)return;started=true;original=mc.level.dimension();boatId=mc.player.getVehicle().getUUID();}
        if(disconnecting)return;
        if(++age>400)throw new IllegalStateException("Native edge phase timed out: "+phase);
        mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.keyUp.setDown(phase<3);mc.options.keyLeft.setDown(phase<4);
        if(mc.player!=null)mc.player.setXRot(-15);
        if(phase<4){
            if(age==1&&phase>0){ground(mc,phase-1);return;}
            if(terrainReady!=null&&!terrainReady.isDone()){age=1;return;}
            if(age>15&&mc.player.getVehicle()!=null){
                if(RowingClient.capture(mc.player,1,false)!=null)assertions++;
                if(phase==3){
                    float angle=Math.abs(((net.minecraft.world.entity.vehicle.Boat)mc.player.getVehicle()).getBubbleAngle(1));
                    bubbleAngle=Math.max(bubbleAngle,angle);
                    if(angle>0&&RowingClient.capture(mc.player,1,false)!=null)bubbleOwned++;
                    if(angle>0&&mc.player.getVehicle().isUnderWater()&&RowingClient.capture(mc.player,1,false)!=null)underwaterBubbleOwned++;
                }
            }
            if(age==(phase==3?140:45)){
                if(phase==3)System.out.println("EDGE_BUBBLE angle="+bubbleAngle+" owned="+bubbleOwned+" nativeContact="+FrameContact.maximum());
                phase++;age=0;terrainReady=null;
            }
        }else if(phase==4){
            if(age==1){ground(mc,3);return;}
            if(terrainReady!=null&&!terrainReady.isDone()){age=1;return;}
            if(age<=30)return;
            int cycleAge=age-30;
            if(cycleAge%16==1)command(mc,"ride RowQA dismount");
            if(cycleAge%16==9)command(mc,"ride RowQA mount @e[type=minecraft:boat,limit=1,sort=nearest]");
            if(mc.player.getVehicle()==null){if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dismounted pose retained");dismounted++;}
            else remounted++;
            if(cycleAge==96){if(dismounted<3||remounted<3)throw new IllegalStateException("Rapid mounts did not traverse native states");phase++;age=0;command(mc,"execute in minecraft:the_nether run tp RowQA 0 100 0");}
        }else if(phase==5){
            if(mc.level!=null&&!mc.level.dimension().equals(original)&&age>10){
                if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dimension transition retained boat pose");assertions++;phase++;age=0;command(mc,"gamemode survival RowQA");
            }
        }else if(phase==6){
            if(age==100)command(mc,"kill RowQA");
            if(age==120)System.out.println("EDGE_DEATH_STATE health="+(mc.player==null?-1:mc.player.getHealth())+" alive="+(mc.player!=null&&mc.player.isAlive()));
            if(mc.player!=null&&!mc.player.isAlive()){
                if(RowingClient.capture(mc.player,1,false)!=null)throw new IllegalStateException("Dead player retained pose");assertions++;
                phase++;age=0;disconnecting=true;try{mc.level.disconnect();mc.disconnect(new TitleScreen());}finally{disconnecting=false;}
            }
        }else if(phase==7&&mc.level==null&&mc.player==null){
            boolean pass=assertions>20&&bubbleAngle>0&&bubbleOwned>5&&FrameContact.samples()>10&&FrameContact.maximum()<=1;
            try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-edge.json"),"{\"pass\":"+pass+",\"assertions\":"+assertions+",\"nativeDismountTicks\":"+dismounted+",\"nativeRemountTicks\":"+remounted+",\"nativeBubbleAngle\":"+bubbleAngle+",\"nativeBubbleOwnedTicks\":"+bubbleOwned+",\"underwaterBubbleOwnedTicks\":"+underwaterBubbleOwned+",\"maximumNativeContactPixels\":"+FrameContact.maximum()+",\"dimensionDeathDisconnect\":true}");}catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_EDGE_COMPLETE pass="+pass);mc.stop();
        }
    }
}


