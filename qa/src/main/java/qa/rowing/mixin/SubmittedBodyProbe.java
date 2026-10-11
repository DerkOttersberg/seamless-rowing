package qa.rowing.mixin;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Measure the native skin draw after every pose hook, independent of Mixin injection order. */
@Mixin(AgeableListModel.class)
public class SubmittedBodyProbe {
    @Inject(method="renderToBuffer",at=@At("HEAD"))
    private void body(PoseStack stack,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha,CallbackInfo ci){
        if(!((Object)this instanceof PlayerModel<?> model))return;
        var context=RenderContext.current();
        qa.rowing.SurfaceGeometry.body(model,context==null?null:context.rowingPose());
        if(qa.rowing.QaPlatform.isModLoaded("entity_model_features"))qa.rowing.FreshProbe.inspect(model,context);
    }
}
