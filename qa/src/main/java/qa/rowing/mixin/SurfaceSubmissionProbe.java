package qa.rowing.mixin;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.derkottersberg.seamlessrowing.client.SurfaceRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Measures real native vertex-consumer submissions, after bypassing stale cuboid caches. */
@Mixin(value=SurfaceRenderer.class,remap=false)
public class SurfaceSubmissionProbe {
    @Inject(method="emit",at=@At("HEAD"))
    private static void classify(net.minecraft.client.model.geom.ModelPart.Cube cube,com.mojang.blaze3d.vertex.PoseStack.Pose pose,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){
        // Native skin/armor torso UVs occupy the atlas columns preceding both arm strips.
        qa.rowing.SurfaceSubmission.torso=java.util.Arrays.stream(cube.polygons).filter(f->f!=null)
                .flatMap(f->java.util.Arrays.stream(f.vertices)).anyMatch(v->v.u<.5f&&v.v>.25f);
    }
    @Inject(method="emit",at=@At("RETURN"))
    private static void end(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){qa.rowing.SurfaceSubmission.torso=false;}
    @Redirect(method="emit",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(FFFFFFFFFIIFFF)V",remap=true))
    private static void nativeVertex(VertexConsumer consumer,float x,float y,float z,float red,float green,float blue,float alpha,float u,float v,int overlay,int light,float nx,float ny,float nz){
        qa.rowing.SurfaceSubmission.vertex(x,y,z,u,v,nx,ny,nz);
        consumer.vertex(x,y,z,red,green,blue,alpha,u,v,overlay,light,nx,ny,nz);
    }
}


