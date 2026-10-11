package qa.rowing.mixin;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Observes the final native base-model admission decision after mod hooks. */
@Mixin(value=LivingEntityRenderer.class,priority=400)
public class FirstPersonBodyProbe {
    @ModifyVariable(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("STORE"),ordinal=0)
    private net.minecraft.client.renderer.RenderType body(net.minecraft.client.renderer.RenderType type,net.minecraft.world.entity.LivingEntity entity,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack stack,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
        var context=io.github.derkottersberg.seamlessrowing.client.RenderContext.current();
        if(context!=null && context.player==entity && context.rowingHandsOnly()){
            qa.rowing.FirstPersonOwnership.nativeBodyQueries++;
            if(context.rowingPose()==null&&qa.rowing.FirstPersonOwnership.nativeBodyQueries<5)System.out.println("HANDS_POSE_CLEARED bodyType="+type);
            if(type!=null)qa.rowing.FirstPersonOwnership.invalidBodyTypes++;
        }
        return type;
    }
}
