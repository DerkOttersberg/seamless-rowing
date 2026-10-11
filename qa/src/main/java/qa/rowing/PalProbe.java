package qa.rowing;

import net.minecraft.client.Minecraft;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;

/** A real optional PAL layer in the test client; no fixture code enters the release. */
public final class PalProbe implements IAnimation {
    private boolean active;
    private static PalProbe layer;
    public static void tick(Minecraft mc,boolean active) {
        if(layer==null) {layer=new PalProbe();PlayerAnimationAccess.getPlayerAnimManager(mc.player).addAnimLayer(9000,layer);}
        layer.active=active;
    }
    public boolean isActive(){return active;}
    public void get3DTransform(PlayerAnimBone bone){if(active&&bone.name.equals("left_arm"))bone.rotation.z+=.08f;}
    public boolean canRemove(){return false;}
}
