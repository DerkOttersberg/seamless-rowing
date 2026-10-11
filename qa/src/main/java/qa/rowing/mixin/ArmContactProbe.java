package qa.rowing.mixin;

import qa.rowing.RowingQaClient;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=RowingArmLayer.class,remap=false)
public class ArmContactProbe {
    @Inject(method="solve",at=@At("RETURN"))
    private static void measure(ModelPart part,RowingPose pose,boolean left,Matrix4f transform,boolean sleeve,
                                CallbackInfoReturnable<ArmRigModel.Bone> cir) {
        if(!pose.owns(left) || pose.weight(left)<.999) return;
        var bone=cir.getReturnValue(); var grip=pose.grip(left);
        qa.rowing.ArmAnatomy.measure(bone,left);
        Vector3f palm=new Vector3f(0,6,0).rotate(bone.lower()).add(0,6,0).rotate(bone.upper()).add(bone.shoulder());
        Vector3f target=transform.transformPosition(new Vector3f((float)grip.x,(float)grip.y,(float)grip.z)).mul(16);
        RowingQaClient.measure(palm.distance(target));
    }
}
