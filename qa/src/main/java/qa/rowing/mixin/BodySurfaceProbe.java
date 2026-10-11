package qa.rowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=PlayerModel.class,priority=400)
public class BodySurfaceProbe {
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"),order=1000)
    private void source(LivingEntity entity,float limb,float speed,float age,float yaw,float pitch,CallbackInfo ci){
        qa.rowing.SurfaceGeometry.source((PlayerModel)(Object)this);
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"),order=1200)
    private void geometry(LivingEntity entity,float limb,float speed,float age,float yaw,float pitch,CallbackInfo ci){
        var context=RenderContext.current();
        var row=context!=null&&context.player==entity?context.rowingPose():null;
        qa.rowing.SurfaceGeometry.body((PlayerModel)(Object)this,row);
        if(qa.rowing.QaPlatform.isModLoaded("entity_model_features"))
            qa.rowing.FreshProbe.inspect((PlayerModel)(Object)this,context);
    }
}
