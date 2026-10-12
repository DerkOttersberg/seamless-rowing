package qa.rowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.SurfaceRenderer;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=SurfaceRenderer.class,remap=false)
public class ArmorSurfaceProbe {
    @Inject(method="render",at=@At("HEAD"))
    private static void draw(ModelPart part,PoseStack stack,VertexConsumer consumer,int light,int overlay,int color,CallbackInfo ci){qa.rowing.ArmorSubmission.part(part);}
}
