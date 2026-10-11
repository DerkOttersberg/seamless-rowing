package qa.rowing.mixin;

import qa.rowing.FrameContact;
import qa.rowing.CameraCapturePass;
import io.github.derkottersberg.seamlessrowing.client.BoatState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.boat.AbstractBoatModel;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe the collector boundary, including callers wrapped by other mods. */
@Mixin(SubmitNodeCollection.class)
public class SubmittedBoatProbe {
    @Unique private static int firstLogs;
    @Inject(method="submitModel",at=@At("HEAD"))
    private void submitted(Model<?> generic,Object data,PoseStack stack,RenderType type,int light,int overlay,int tint,TextureAtlasSprite uv,int outline,CrumblingOverlay crumbling,CallbackInfo ci) {
        if(CameraCapturePass.active||!(generic instanceof AbstractBoatModel model)||!(data instanceof BoatRenderState state))return;
        var pose=((BoatState)state).rowingBoatPose();if(pose==null)return;
        model.setupAnim(state);
        for(boolean left:new boolean[]{true,false}) {
            if(!pose.owns(left)||pose.weight(left)<.999f)continue;
            var paddle=model.root().getChild(left?"left_paddle":"right_paddle");
            if(net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson()&&firstLogs++<4)
                System.out.println("NATIVE_FIRST_PADDLE visible="+paddle.visible+" class="+paddle.getClass().getName()+" root="+model.root().visible);
            FrameContact.phase(left,left?state.rowingTimeLeft:state.rowingTimeRight);
            stack.pushPose();paddle.translateAndRotate(stack);
            FrameContact.handle(left,stack.last().pose().transformPosition(new Vector3f(0,1f/16,-4f/16)));
            stack.popPose();
        }
    }
}
