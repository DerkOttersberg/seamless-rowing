package qa.rowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import com.mojang.blaze3d.vertex.*;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ArmRigModel.class,remap=false)
public class SubmittedArmProbe {
    @Unique private ArmRigModel.DrawState probe$state;
    @Inject(method="setupAnim",at=@At("RETURN"))
    private void state(ArmRigModel.DrawState state,CallbackInfo ci){probe$state=state;}
    @Inject(method="renderToBuffer",at=@At("HEAD"))
    private void mesh(PoseStack stack,VertexConsumer consumer,int light,int overlay,int color,CallbackInfo ci){
        var context=RenderContext.current();
        if(context==null||context.rowingPose()==null||probe$state==null||qa.rowing.CameraCapturePass.active)return;
        var pose=context.rowingPose();var model=(ArmRigModel)(Object)this;
        for(boolean left:new boolean[]{true,false}){
            if(!pose.owns(left)||pose.weight(left)<.999f)continue;
            var upper=model.root().getChild(left?"left":"right");var lower=upper.getChild("lower");
            qa.rowing.ArmAnatomy.submitted(upper,lower);
            qa.rowing.SurfaceGeometry.measure(upper,lower,probe$state.firstPerson());
            stack.pushPose();upper.translateAndRotate(stack);lower.translateAndRotate(stack);
            qa.rowing.FrameContact.hand(left,stack.last().pose().transformPosition(new Vector3f(0,6f/16,0)));
            stack.popPose();
        }
    }
}
