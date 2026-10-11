package qa.rowing.mixin;

import qa.rowing.FrameContact;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=RowingArmLayer.class,remap=false)
public class SubmittedArmProbe {
    @Unique private RowingPose probe$pose;
    @Inject(method="submit",at=@At("HEAD"))
    private void capture(PoseStack stack,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch,CallbackInfo ci) {
        probe$pose=((RowingState)state).rowingPose();
    }
    @Redirect(method="submit",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V"))
    private void measure(SubmitNodeCollector collector,Model<ArmRigModel.DrawState> model,Object data,PoseStack stack,Identifier texture,int light,int overlay,int outline) {
        var snapshot=(ArmRigModel.DrawState)data;
        model.setupAnim(snapshot);
        if(probe$pose!=null) for(boolean left:new boolean[]{true,false}) {
            if(!probe$pose.owns(left)||probe$pose.weight(left)<.999) continue;
            var upper=model.root().getChild(left?"left":"right");var lower=upper.getChild("lower");
            qa.rowing.ArmAnatomy.submitted(upper,lower);
            stack.pushPose();upper.translateAndRotate(stack);lower.translateAndRotate(stack);
            FrameContact.hand(left,stack.last().pose().transformPosition(new Vector3f(0,6f/16,0)));
            stack.popPose();
        }
        collector.submitModel(model,snapshot,stack,texture,light,overlay,outline);
    }
}
