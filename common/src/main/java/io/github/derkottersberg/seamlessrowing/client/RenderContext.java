package io.github.derkottersberg.seamlessrowing.client;
import net.minecraft.client.player.AbstractClientPlayer;
import org.joml.Matrix4f;
import java.util.*;

/** A scoped snapshot for legacy immediate rendering; render calls never advance stroke tracks. */
public final class RenderContext implements RowingState {
    private static final ThreadLocal<Deque<RenderContext>> STACK=ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Boolean> HANDS=ThreadLocal.withInitial(()->false);
    public final AbstractClientPlayer player;
    private RowingPose pose;private Matrix4f base;private boolean hands;
    private RenderContext(AbstractClientPlayer player,RowingPose pose,Matrix4f base,boolean hands) {
        this.player=player;this.pose=pose;this.base=new Matrix4f(base);this.hands=hands;
    }
    public static void begin(AbstractClientPlayer player,float partial,Matrix4f base) {
        boolean first=player==net.minecraft.client.Minecraft.getInstance().player&&net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson();
        STACK.get().push(new RenderContext(player,RowingClient.capture(player,partial,first),base,HANDS.get()));
    }
    public static RenderContext current(){return STACK.get().peek();}
    public static void end(){if(!STACK.get().isEmpty())STACK.get().pop();}
    public static void clear(){STACK.remove();HANDS.remove();}
    public static void hands(Runnable draw){boolean old=HANDS.get();HANDS.set(true);try{draw.run();}finally{HANDS.set(old);}}
    public RowingPose rowingPose(){return pose;}public void rowingPose(RowingPose value){pose=value;}
    public Matrix4f rowingBase(){return new Matrix4f(base);}public void rowingBase(Matrix4f value){base=new Matrix4f(value);}
    public boolean rowingHandsOnly(){return hands;}public void rowingHandsOnly(boolean value){hands=value;}
}
