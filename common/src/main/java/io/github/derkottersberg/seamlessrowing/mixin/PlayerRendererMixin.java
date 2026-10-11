package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @SuppressWarnings({"rawtypes","unchecked"})
    @Inject(method="<init>",at=@At("RETURN"))
    private void layer(CallbackInfo ci){((LivingRendererAccess)this).rowingAddLayer(new RowingArmLayer((RenderLayerParent)this));}
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void begin(AbstractClientPlayer player,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){RenderContext.begin(player,partial,new Matrix4f(stack.last().pose()));}
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("RETURN"))
    private void end(AbstractClientPlayer player,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){RenderContext.end();}
    @ModifyVariable(method="setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float bodyYaw(float yaw,AbstractClientPlayer player,PoseStack stack,float age,float originalYaw,float partial,float scale){
        var context=RenderContext.current();
        if(context==null||context.rowingPose()==null||!(player.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat boat))return yaw;
        float weight=Math.max(context.rowingPose().leftWeight(),context.rowingPose().rightWeight());
        return yaw+net.minecraft.util.Mth.wrapDegrees(net.minecraft.util.Mth.rotLerp(partial,boat.yRotO,boat.getYRot())-yaw)*weight;
    }
}
