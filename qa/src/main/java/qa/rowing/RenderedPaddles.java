package qa.rowing;

import net.minecraft.client.model.geom.ModelPart;
import io.github.derkottersberg.seamlessrowing.client.RowingPose;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.WeakHashMap;
import org.joml.Vector3f;

public final class RenderedPaddles {
    private record Paddle(boolean left,float phase) {}
    private static final WeakHashMap<ModelPart,Paddle> ACTIVE=new WeakHashMap<>();
    public static void register(ModelPart part,boolean left,float phase,RowingPose pose) {
        if(pose!=null&&pose.owns(left)&&pose.weight(left)>=.999f)ACTIVE.put(part,new Paddle(left,phase));
        else ACTIVE.remove(part);
    }
    public static void inspect(ModelPart part,PoseStack stack) {
        var paddle=ACTIVE.get(part);if(paddle==null)return;
        FrameContact.phase(paddle.left,paddle.phase);
        FrameContact.handle(paddle.left,stack.last().pose().transformPosition(new Vector3f(0,1f/16,-4f/16)));
    }
}
