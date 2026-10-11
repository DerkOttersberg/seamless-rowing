package qa.rowing.mixin;

import qa.rowing.FrameContact;
import io.github.derkottersberg.seamlessrowing.client.BoatState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(AbstractBoatRenderer.class)
public class NativePaddleProbe {
    @Redirect(method="submit(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V"))
    private void measure(SubmitNodeCollector collector,Model<BoatRenderState> model,Object data,PoseStack stack,Identifier texture,int light,int overlay,int outline) {
        var state=(BoatRenderState)data;var pose=((BoatState)state).rowingBoatPose();
        model.setupAnim(state);
        if(pose!=null) for(boolean left:new boolean[]{true,false}) {
            if(!pose.owns(left)||pose.weight(left)<.999) continue;
            var paddle=model.root().getChild(left?"left_paddle":"right_paddle");
            FrameContact.phase(left,left?state.rowingTimeLeft:state.rowingTimeRight);
            stack.pushPose();paddle.translateAndRotate(stack);
            FrameContact.handle(left,stack.last().pose().transformPosition(new Vector3f(0,1f/16,-4f/16)));
            stack.popPose();
        }
        collector.submitModel(model,state,stack,texture,light,overlay,outline);
    }
}
