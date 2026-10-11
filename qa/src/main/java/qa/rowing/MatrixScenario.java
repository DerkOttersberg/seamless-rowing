package qa.rowing;

import net.minecraft.client.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.player.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.GsonBuilder;

public final class MatrixScenario {
    public record Case(String type,PlayerModelType skin,HumanoidArm hand,CameraType view,int fov,boolean armor) {
        public String label() { return type+"/"+skin+"/"+hand+"/"+view+"/"+fov+"/"+armor; }
    }
    private static final List<Case> CASES=new ArrayList<>();
    private static int index=-1,tick;
    private static volatile UUID expectedBoat;
    private static String current;
    private static boolean done;
    static {
        for(String wood:List.of("oak","spruce","birch","jungle","acacia","dark_oak","mangrove","cherry","pale_oak"))
            for(String type:List.of(wood+"_boat",wood+"_chest_boat")) CASES.add(new Case(type,PlayerModelType.WIDE,HumanoidArm.RIGHT,CameraType.THIRD_PERSON_FRONT,70,false));
        CASES.add(new Case("bamboo_raft",PlayerModelType.WIDE,HumanoidArm.RIGHT,CameraType.THIRD_PERSON_FRONT,70,false));
        CASES.add(new Case("bamboo_chest_raft",PlayerModelType.WIDE,HumanoidArm.RIGHT,CameraType.THIRD_PERSON_FRONT,70,false));
        for(String type:List.of("oak_boat","oak_chest_boat","bamboo_raft","bamboo_chest_raft"))
            for(var skin:PlayerModelType.values()) for(var hand:HumanoidArm.values()) for(var view:CameraType.values())
                CASES.add(new Case(type,skin,hand,view,view==CameraType.FIRST_PERSON?110:view==CameraType.THIRD_PERSON_BACK?30:70,true));
        for(String type:List.of("oak_boat","bamboo_raft")) for(var skin:PlayerModelType.values())
            for(var hand:HumanoidArm.values()) CASES.add(new Case(type,skin,hand,CameraType.FIRST_PERSON,30,false));
    }
    public static String current() { return current; }
    public static PlayerSkin skin() {
        if(index<0) return null;
        var target=CASES.get(index).skin;
        for(int seed=0;seed<256;seed++) {
            var candidate=DefaultPlayerSkin.get(new UUID(seed,seed*31L));
            if(candidate.model()==target) return candidate;
        }
        throw new IllegalStateException("No native skin for "+target);
    }
    public static void tick(Minecraft mc) {
        if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null) return;
        if(index<0) { index=0;spawn(mc);return; }
        if(mc.player.getVehicle()==null||!mc.player.getVehicle().getUUID().equals(expectedBoat)) return;
        Case test=CASES.get(index);current=test.label();tick++;
        if(tick==1) {
            mc.options.mainHand().set(test.hand);mc.options.fov().set(test.fov);mc.options.setCameraType(test.view);
            System.out.println("ROWING_MATRIX_BEGIN "+current);
        }
        mc.player.setYRot(mc.player.getVehicle().getYRot());mc.player.setXRot(test.view==CameraType.FIRST_PERSON?50:test.view==CameraType.THIRD_PERSON_FRONT?-15:20);
        mc.options.keyUp.setDown(tick>=8&&tick<32);mc.options.keyLeft.setDown(tick>=20&&tick<26);
        mc.options.keyDown.setDown(tick>=38&&tick<44);
        if(tick==22) Screenshot.grab(mc.gameDirectory,String.format("matrix-%02d.png",index),mc.gameRenderer.mainRenderTarget(),1,m->{});
        if(tick<48) return;
        System.out.println("ROWING_MATRIX_END "+current+" nativeSamples="+FrameContact.cases().getOrDefault(current,0));
        mc.options.keyUp.setDown(false);mc.options.keyLeft.setDown(false);mc.options.keyDown.setDown(false);
        current=null;tick=0;index++;
        if(index<CASES.size()) {spawn(mc);return;}
        done=true;index=CASES.size()-1;
        boolean pass=FrameContact.maximum()<=1&&CASES.stream().allMatch(c->FrameContact.cases().getOrDefault(c.label(),0)>0);
        try { Files.writeString(mc.gameDirectory.toPath().resolve("rowing-matrix.json"),new GsonBuilder().setPrettyPrinting().create().toJson(
                Map.of("plannedCases",CASES.size(),"sampleCounts",FrameContact.cases(),"maximumErrorPixels",FrameContact.maximum(),"strokePhaseBins",FrameContact.phaseBins(),"nativePaddleSounds",SoundProbe.count(),"duplicateArmSubmissions",FrameContact.duplicates(),"pass",pass))); }
        catch(Exception failure){throw new RuntimeException(failure);}
        System.out.println("ROWING_MATRIX_COMPLETE pass="+pass+" cases="+CASES.size());mc.stop();
    }
    private static void spawn(Minecraft mc) {
        var server=mc.getSingleplayerServer();Case test=CASES.get(index);expectedBoat=null;
        server.execute(()->{
            var player=server.getPlayerList().getPlayerByName("RowQA");var old=player.getVehicle();
            player.stopRiding();if(old!=null) old.discard();
            player.teleportTo(server.overworld(),0,-57,0,Set.of(),0,50,true);
            var type=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(test.type));
            var boat=(AbstractBoat)type.create(server.overworld(),EntitySpawnReason.COMMAND);
            boat.setPos(0,-58.1,0);server.overworld().addFreshEntity(boat);player.startRiding(boat,true,true);expectedBoat=boat.getUUID();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),test.armor
                    ? "item replace entity RowQA armor.chest with minecraft:diamond_chestplate[minecraft:enchantments={\"minecraft:protection\":1},minecraft:trim={pattern:\"minecraft:spire\",material:\"minecraft:gold\"}]"
                    : "item replace entity RowQA armor.chest with minecraft:air");
        });
    }
}
