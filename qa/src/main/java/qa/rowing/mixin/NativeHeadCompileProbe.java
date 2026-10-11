package qa.rowing.mixin;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ModelPart.class)
public class NativeHeadCompileProbe {
    @Inject(method="compile",at=@At("HEAD"))
    private void draw(PoseStack.Pose pose,VertexConsumer consumer,int light,int overlay,int color,CallbackInfo ci){qa.rowing.HeadCompileProbe.inspect((ModelPart)(Object)this);}
}
