package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.*;
import net.minecraft.world.InteractionHand;
import java.nio.file.*;
import java.util.*;

/** Two actual packaged clients; all input goes through native client key mappings. */
public final class RemoteScenario {
    private static int tick,warmup,owned,useReleased,swingsReleased,postStop,backwards,palReleased;
    private static boolean first=true;
    private static boolean announced;
    public static void tick(Minecraft mc) {
        String role=System.getProperty("qa.rowing.role");
        Path ready=mc.gameDirectory.toPath().getParent().resolve("mp-ready");
        if(!announced) {
            try {Files.writeString(mc.gameDirectory.toPath().resolve("mp-client-ready"),role);announced=true;}
            catch(Exception failure){throw new RuntimeException(failure);}
        }
        if(!Files.exists(ready)) return;
        if(first) {first=false;System.out.println("ROWING_MP_READY "+role);}
        if(role.equals("rower")&&mc.player.getVehicle()==null) return;
        // Let native camera interpolation settle after the fixture's server teleport/mount.
        if(warmup<40) {
            if(role.equals("rower")) {
                float yaw=mc.player.getVehicle().getYRot();
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(yaw);mc.player.setYHeadRot(yaw);mc.player.setXRot(40);
                mc.options.keyUp.setDown(false);mc.options.keyLeft.setDown(false);mc.options.keyDown.setDown(false);
            } else {
                var target=mc.level.players().stream().filter(p->p.getName().getString().equals("RowQA")).findFirst();
                if(target.isEmpty()||target.get().getVehicle()==null) return;
                observerCamera(mc,target.get());
            }
            warmup++;return;
        }
        tick++;
        if(role.equals("rower")) {
            int local=(tick-1)%140;
            mc.options.setCameraType(tick<=140?CameraType.FIRST_PERSON:tick<=280?CameraType.THIRD_PERSON_BACK:CameraType.THIRD_PERSON_FRONT);
            float yaw=mc.player.getVehicle().getYRot()+(local>=80&&local<100 ? (float)Math.sin((local-80)*.3)*20 : 0);
            mc.player.setYRot(yaw);mc.player.setYHeadRot(yaw);mc.player.setXRot(tick<=140?40:tick<=280?20:-15);
            mc.options.keyUp.setDown(local>=10&&local<90);
            mc.options.keyLeft.setDown(local>=35&&local<48);
            mc.options.keyDown.setDown(local>=115&&local<128);
            mc.options.keyUse.setDown(local>=62&&local<74);
            mc.options.keyAttack.setDown(local==50);
            if(local==50) net.minecraft.client.KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(0));
            if(local==62) net.minecraft.client.KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(1));
            if(local==62) System.out.println("ROWING_USE_RESULT "+mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND)+" held="+mc.player.getMainHandItem()+" using="+mc.player.isUsingItem());
            if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("player_animation_library")) {
                PalProbe.tick(mc,local>=24&&local<30);
                if(local>=25&&local<30&&RowingClient.capture(mc.player,1,mc.options.getCameraType().isFirstPerson())==null) palReleased++;
            }
            var pose=RowingClient.capture(mc.player,1,mc.options.getCameraType().isFirstPerson());
            if(pose!=null&&(pose.ownLeft()||pose.ownRight())) owned++;
            if(mc.player.isUsingItem()&&(pose==null||(!pose.ownLeft()&&!pose.ownRight()))) useReleased++;
            if(mc.player.isSwinging()&&!RowingClient.owns(mc.player,mc.player.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT)) swingsReleased++;
            if(local>=102&&local<110&&pose==null) postStop++;
            if(local>=122&&local<128&&pose==null) backwards++;
        } else {
            var rower=mc.level.players().stream().filter(p->p.getName().getString().equals("RowQA")).findFirst();
            if(rower.isPresent()) {
                observerCamera(mc,rower.get());
                var pose=RowingClient.capture(rower.get(),1,false);
                if(pose!=null&&(pose.ownLeft()||pose.ownRight())) owned++;
            }
        }
        if(tick<(role.equals("observer")?900:420)) Screenshot.grab(mc.gameDirectory,String.format("mp-%04d.png",tick),mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(role.equals("rower")&&tick==425) {
            try {Files.writeString(mc.gameDirectory.toPath().getParent().resolve("mp-rower-done"),"done");}
            catch(Exception failure){throw new RuntimeException(failure);}
        }
        if((role.equals("rower")&&tick==425)||(role.equals("observer")&&Files.exists(mc.gameDirectory.toPath().getParent().resolve("mp-rower-done")))) {
            boolean pass=owned>40&&FrameContact.samples()>10&&FrameContact.maximum()<=1;
            if(role.equals("rower")) pass&=useReleased>0&&swingsReleased>0&&postStop>0&&backwards>0;
            if(role.equals("rower")&&net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("player_animation_library")) pass&=palReleased>0;
            Map<String,Object> result=new LinkedHashMap<>();result.put("pass",pass);result.put("role",role);
            result.put("ownedTicks",owned);result.put("itemUseReleasedTicks",useReleased);result.put("swingReleasedTicks",swingsReleased);
            result.put("stoppedTicks",postStop);result.put("backwardsVanillaTicks",backwards);
            result.put("activePalReleasedTicks",palReleased);
            result.put("nativeSamples",FrameContact.samples());result.put("nativeMaximumErrorPixels",FrameContact.maximum());result.put("views",FrameContact.views());
            result.put("strokePhaseBins",FrameContact.phaseBins());result.put("duplicateArmSubmissions",FrameContact.duplicates());result.put("nativePaddleSounds",SoundProbe.count());
            try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-multiplayer.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}
            catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_MP_COMPLETE "+role+" pass="+pass);mc.stop();
        }
    }
    private static void observerCamera(Minecraft mc,net.minecraft.client.player.AbstractClientPlayer rower) {
        var origin=rower.position();mc.player.setPos(origin.x+3,origin.y+1.8,origin.z+4);
        var target=rower.position().add(0,.5,0).subtract(mc.player.getEyePosition());
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-target.x,target.z)));
        mc.player.setXRot((float)-Math.toDegrees(Math.atan2(target.y,Math.hypot(target.x,target.z))));
    }
}
