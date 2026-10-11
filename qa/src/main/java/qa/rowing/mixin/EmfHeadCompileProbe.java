package qa.rowing.mixin;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets="traben.entity_model_features.models.parts.EMFModelPart",remap=false)
public class EmfHeadCompileProbe {
    @Inject(method={"compile","method_22702","m_104290_"},at=@At("HEAD"))
    private void draw(PoseStack.Pose pose,VertexConsumer consumer,int light,int overlay,int color,CallbackInfo ci){qa.rowing.HeadCompileProbe.inspect((ModelPart)(Object)this);}
}
