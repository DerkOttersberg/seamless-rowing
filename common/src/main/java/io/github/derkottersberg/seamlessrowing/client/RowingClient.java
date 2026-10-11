package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.config.ClientSettings;
import io.github.derkottersberg.seamlessrowing.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessrowing.math.PaddlePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.*;
import java.lang.Math;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;
import org.slf4j.LoggerFactory;

public final class RowingClient {
    private static ClientPlatformServices platform;
    private static ClientSettings settings=ClientSettings.DEFAULT;
    private static Object levelIdentity;
    private static final Map<UUID,Track> TRACKS=new HashMap<>();
    private static final List<PaddleGeometry> GEOMETRY=List.of(new VanillaPaddleGeometry(false),new VanillaPaddleGeometry(true));
    private record Track(UUID driver,PaddleTrack left,PaddleTrack right) {}
    private RowingClient() {}
    public static void initialize(ClientPlatformServices services) {
        platform=services;
        try { settings=ClientSettings.load(configPath()); }
        catch(IOException malformed) { LoggerFactory.getLogger("Seamless Rowing").warn("Settings not loaded; file preserved",malformed); }
    }
    public static Path configPath() { return platform.configDirectory().resolve("seamless-rowing.json"); }
    public static ClientSettings settings() { return settings; }
    public static boolean scheduledOwnership() { return platform.animationSchedulerActive(); }
    public static boolean managesFirstPersonBoat() {
        if(platform==null)return false;
        var mc=Minecraft.getInstance();
        return mc!=null&&mc.player!=null&&settings.enabled()&&settings.firstPerson()&&!customRig()&&!skinLayers()
                &&mc.options.getCameraType().isFirstPerson()&&mc.getCameraEntity()==mc.player
                &&mc.player.getVehicle() instanceof AbstractBoat boat&&eligible(mc.player,boat);
    }
    public static boolean restoresPaddles() {return platform.paddleVisibilityHandoff();}
    public static void prepareModel(net.minecraft.client.model.player.PlayerModel model) {platform.prepareRowingModel(model);}
    public static void save(ClientSettings draft) throws IOException { draft.save(configPath()); settings=draft; clear(); }
    public static void clear() { TRACKS.clear(); levelIdentity=null; }
    public static String compatibilityStatus() {
        if(platform.customBoatGeometry())return "Custom boat resource geometry: existing boat and player rendering preserved.";
        if(platform.unsupportedAnimationIntegration()) return platform.integrationStatus();
        if (platform.isModLoaded("figura") || platform.isModLoaded("cpm") || platform.isModLoaded("epicfight"))
            return "Custom player rig detected: rowing visuals safely disabled.";
        if (skinLayers()) return "3D Skin Layers: rigid third-person arms; existing first-person rendering preserved.";
        if (platform.isModLoaded("playeranimator") || platform.isModLoaded("player-animation-lib"))
            return "Animation library detected: conservative fallback until its adapter is verified.";
        return "Vanilla rigs supported. "+platform.integrationStatus();
    }
    public static boolean customRig() {
        return platform.unsupportedAnimationIntegration() || platform.isModLoaded("figura") || platform.isModLoaded("cpm") || platform.isModLoaded("epicfight")
                || platform.isModLoaded("playeranimator") || platform.isModLoaded("player-animation-lib");
    }
    private static boolean skinLayers() { return platform.isModLoaded("skinlayers3d") || platform.isModLoaded("skinlayers"); }
    public static void tick() {
        Minecraft mc=Minecraft.getInstance();
        if (platform.consumeSettingsClick() && mc.gui.screen()==null) mc.setScreenAndShow(new RowingSettingsScreen(null));
        if (mc.level!=levelIdentity) { clear(); levelIdentity=mc.level; }
        if (mc.level==null || !settings.enabled() || customRig()) { TRACKS.clear(); return; }
        Set<UUID> seen=new HashSet<>();
        for (var entity:mc.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractBoat boat) || !(boat.getControllingPassenger() instanceof AbstractClientPlayer driver)
                    || !eligible(driver,boat)) continue;
            seen.add(boat.getUUID());
            Track track=TRACKS.get(boat.getUUID());
            if (track==null || !track.driver.equals(driver.getUUID())) {
                track=new Track(driver.getUUID(),new PaddleTrack(),new PaddleTrack()); TRACKS.put(boat.getUUID(),track);
            }
            track.left.tick(boat.getPaddleState(0),boat.getRowingTime(0,1));
            track.right.tick(boat.getPaddleState(1),boat.getRowingTime(1,1));
        }
        TRACKS.keySet().retainAll(seen);
    }
    public static boolean eligible(AbstractClientPlayer player,AbstractBoat boat) {
        var avatarRenderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if(!(avatarRenderer instanceof AvatarRenderer<?> avatar)
                || avatar.getModel().getClass()!=net.minecraft.client.model.player.PlayerModel.class
                || platform.customPlayerModel(avatar.getModel())
                || !platform.playerGeometrySupported(avatar.getModel())) return false;
        // A replacement renderer needs a verified geometry adapter, even if its entity subclasses a boat.
        var geometry=geometry(boat);
        if(platform.externalAnimationActive(player)) return false;
        return new ControllerEligibility(player.isAlive(),player.isSleeping(),player.isSpectator(),
                player.getVehicle()==boat,boat.getControllingPassenger()==player,
                geometry!=null).allowed();
    }
    public static boolean owns(AbstractClientPlayer player,boolean left) {
        if (player.isUsingItem()) return false;
        HumanoidArm side=left ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        if (player.isSwinging()) {
            HumanoidArm swing=player.getCurrentSwing().hand()==InteractionHand.OFF_HAND ? player.getMainArm().getOpposite() : player.getMainArm();
            if (side==swing) return false;
        }
        return true;
    }
    /** Model-independent admission for EMF's per-entity vanilla-pose condition. */
    public static boolean requestsRowingRig(AbstractClientPlayer player) {
        var mc=Minecraft.getInstance();
        if(!settings.enabled() || customRig() || !player.isAlive() || player.isSleeping() || player.isSpectator()
                || !(player.getVehicle() instanceof AbstractBoat boat) || boat.getControllingPassenger()!=player
                || geometry(boat)==null || platform.externalAnimationActive(player) || player.isUsingItem()) return false;
        boolean first=player==mc.player && mc.options.getCameraType().isFirstPerson();
        if(first ? !settings.firstPerson() || skinLayers() : !settings.thirdPerson()) return false;
        var track=TRACKS.get(boat.getUUID());
        return boat.getPaddleState(0) || boat.getPaddleState(1)
                || track!=null && track.driver.equals(player.getUUID())
                && (track.left.weight(1)>.001f || track.right.weight(1)>.001f);
    }
    public static RowingPose capture(AbstractClientPlayer player,float partial,boolean firstPerson) {
        if(firstPerson && Minecraft.getInstance().getCameraEntity()!=player) return null;
        if (!settings.enabled() || customRig() || !(player.getVehicle() instanceof AbstractBoat boat)
                || !eligible(player,boat) || (firstPerson ? !settings.firstPerson() : !settings.thirdPerson())) return null;
        if(firstPerson&&skinLayers()) return null;
        Track track=TRACKS.get(boat.getUUID());
        if (track==null || !track.driver.equals(player.getUUID())) return null;
        float lw=track.left.weight(partial),rw=track.right.weight(partial);
        if (lw<=0 && rw<=0) return null;
        PaddlePose l=track.left.sample(boat.getRowingTime(0,partial),partial);
        PaddlePose r=track.right.sample(boat.getRowingTime(1,partial),partial);
        boolean ol=lw>.001f && owns(player,true),or=rw>.001f && owns(player,false);
        if(!ol&&!or)return null;
        PaddleGeometry geometry=geometry(boat);
        if(geometry==null) return null;
        Vec3 relative=boat.getPosition(partial).subtract(player.getPosition(partial));
        Matrix4f boatMatrix=boatMatrix(boat,partial);
        Vec3 left=relative.add(geometry.grip(boatMatrix,l,false));
        Vec3 right=relative.add(geometry.grip(boatMatrix,r,true));
        float lean=settings.torso() && !player.isUsingItem() && !player.isSwinging()
                ? (track.left.lean(boat.getRowingTime(0,partial),partial)+track.right.lean(boat.getRowingTime(1,partial),partial))*.5f : 0;
        float twist=settings.torso() && !player.isUsingItem() && !player.isSwinging() ? (l.yaw()*lw-r.yaw()*rw)*.12f : 0;
        return new RowingPose(left,right,lw,rw,ol,or,settings.elbows()&&!skinLayers(),lean,twist,l,r,
                geometry.shaft(boatMatrix,l,false),geometry.shaft(boatMatrix,r,true));
    }
    private static PaddleGeometry geometry(AbstractBoat boat) {
        if(platform.customBoatGeometry())return null;
        var renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(boat);
        for(var adapter:GEOMETRY) if(adapter.matches(boat,renderer)) return adapter;
        return null;
    }
    public static Matrix4f boatMatrix(AbstractBoat boat,float partial) {
        PoseStack stack=new PoseStack(); stack.translate(0,.375f,0);
        stack.rotateDegrees(Axis.YP,180-boat.getYRot(partial));
        float hurt=boat.getHurtTime()-partial,damage=Math.max(0,boat.getDamage()-partial);
        if (hurt>0) stack.rotateDegrees(Axis.XP,(float)Math.sin(hurt)*hurt*damage/10*boat.getHurtDir());
        if (!boat.isUnderWater() && Math.abs(boat.getBubbleAngle(partial))>1e-5)
            stack.rotate(new Quaternionf().setAngleAxis(boat.getBubbleAngle(partial)*(float)Math.PI/180,1,0,1));
        stack.scale(-1,-1,1); stack.rotateDegrees(Axis.YP,90);
        return new Matrix4f(stack.last().pose());
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static AvatarRenderState firstPersonState(AbstractBoat boat,float partial) {
        Minecraft mc=Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson() || mc.player==null || mc.getCameraEntity()!=mc.player || mc.player.getVehicle()!=boat)
            return null;
        var row=capture(mc.player,partial,true);
        if (row==null || (!row.ownLeft()&&!row.ownRight())) return null;
        AvatarRenderer renderer=(AvatarRenderer)mc.getEntityRenderDispatcher().getRenderer(mc.player);
        AvatarRenderState state=renderer.createRenderState(); renderer.extractRenderState(mc.player,state,partial);
        ((RowingState)state).rowingPose(capture(mc.player,partial,true));
        ((RowingState)state).rowingHandsOnly(true);
        state.nameTag=null; state.scoreText=null; state.displayFireAnimation=false;
        state.showCape=false; state.parrotOnLeftShoulder=null; state.parrotOnRightShoulder=null;
        state.arrowCount=0; state.stingerCount=0;
        state.headEquipment=net.minecraft.world.item.ItemStack.EMPTY;
        state.heldOnHead.clear();
        state.legsEquipment=net.minecraft.world.item.ItemStack.EMPTY;
        state.feetEquipment=net.minecraft.world.item.ItemStack.EMPTY;
        return state;
    }
}
