package qa.rowing.mixin;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Observes the final native base-model admission decision after mod hooks. */
@Mixin(value=LivingEntityRenderer.class,priority=400)
public class FirstPersonBodyProbe {
    @ModifyVariable(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("STORE"),ordinal=0)
    private net.minecraft.client.renderer.rendertype.RenderType body(net.minecraft.client.renderer.rendertype.RenderType type,net.minecraft.client.renderer.entity.state.LivingEntityRenderState state,com.mojang.blaze3d.vertex.PoseStack stack,net.minecraft.client.renderer.SubmitNodeCollector collector,net.minecraft.client.renderer.state.level.CameraRenderState camera){
        
        if(state instanceof io.github.derkottersberg.seamlessrowing.client.RowingState row && row.rowingHandsOnly() && row.rowingPose()!=null){
            qa.rowing.FirstPersonOwnership.nativeBodyQueries++;
            if(type!=null)qa.rowing.FirstPersonOwnership.invalidBodyTypes++;
        }
        return type;
    }
}
