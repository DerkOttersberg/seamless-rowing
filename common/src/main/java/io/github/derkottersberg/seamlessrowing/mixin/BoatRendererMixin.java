package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BoatRenderer.class)
public class BoatRendererMixin {
    @Inject(method="render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void arms(Boat boat,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){RowingClient.renderFirstPerson(boat,partial,stack,buffers,light);}
}
