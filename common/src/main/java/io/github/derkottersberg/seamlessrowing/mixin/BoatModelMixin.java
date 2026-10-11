package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BoatModel.class)
public class BoatModelMixin {
    @Shadow @Final private ModelPart leftPaddle;
    @Shadow @Final private ModelPart rightPaddle;
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/vehicle/Boat;FFFFF)V",at=@At("RETURN"))
    private void pose(Boat boat,float partial,float speed,float age,float yaw,float pitch,CallbackInfo ci){
        if(!(boat.getControllingPassenger() instanceof AbstractClientPlayer player))return;
        var mc=net.minecraft.client.Minecraft.getInstance();var row=RowingClient.capture(player,partial,player==mc.player&&mc.options.getCameraType().isFirstPerson());
        if(row==null)return;
        if(row.ownLeft()){leftPaddle.xRot=row.leftPaddle().pitch();leftPaddle.yRot=row.leftPaddle().yaw();}
        if(row.ownRight()){rightPaddle.xRot=row.rightPaddle().pitch();rightPaddle.yRot=(float)Math.PI-row.rightPaddle().yaw();}
        if(RowingClient.restoresPaddles()&&player==mc.player&&mc.options.getCameraType().isFirstPerson()){leftPaddle.visible=true;rightPaddle.visible=true;}
    }
}
