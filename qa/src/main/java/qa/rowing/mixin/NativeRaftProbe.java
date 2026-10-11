package qa.rowing.mixin;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RaftModel.class)
public class NativeRaftProbe {
    @Shadow @Final private ModelPart leftPaddle;
    @Shadow @Final private ModelPart rightPaddle;
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/vehicle/Boat;FFFFF)V",at=@At("RETURN"))
    private void paddles(Boat boat,float partial,float speed,float age,float yaw,float pitch,CallbackInfo ci){
        var mc=net.minecraft.client.Minecraft.getInstance();
        var row=boat.getControllingPassenger() instanceof AbstractClientPlayer p
            ?io.github.derkottersberg.seamlessrowing.client.RowingClient.capture(p,partial,p==mc.player&&mc.options.getCameraType().isFirstPerson()):null;
        qa.rowing.RenderedPaddles.register(leftPaddle,true,boat.getRowingTime(0,partial),row);
        qa.rowing.RenderedPaddles.register(rightPaddle,false,boat.getRowingTime(1,partial),row);
    }
}
