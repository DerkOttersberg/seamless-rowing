package qa.rowing;
import net.minecraft.client.Minecraft;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
/** A real active public PlayerAnimator layer validates rig ownership handoffs. */
public final class PlayerAnimatorProbe implements IAnimation {
    private boolean active;private static PlayerAnimatorProbe layer;
    public static void tick(Minecraft mc,boolean active){
        if(layer==null){layer=new PlayerAnimatorProbe();PlayerAnimationAccess.getPlayerAnimLayer(mc.player).addAnimLayer(9000,layer);}
        layer.active=active;
    }
    public boolean isActive(){return active;}
    public void setupAnim(float partial){}
    public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f input){
        return active&&type==TransformType.ROTATION&&part.equals("leftArm")?new Vec3f(input.getX(),input.getY(),input.getZ()+.08f):input;
    }
}
