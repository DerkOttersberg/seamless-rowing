package qa.rowing.mixin;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.client.renderer.entity.BoatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BoatRenderer.class)
public class NativeYawProbe {
    @Inject(method="render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void observe(Boat boat,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci){qa.rowing.NativeYaw.observe(boat,yaw,partial);}
}
