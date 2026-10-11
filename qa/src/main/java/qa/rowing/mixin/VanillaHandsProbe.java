package qa.rowing.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Counts actual vanilla hand draws when the same hand is owned by rowing. */
@Mixin(PlayerRenderer.class)
public class VanillaHandsProbe {
    @Inject(method="renderHand",at=@At("HEAD"))
    private void draw(PoseStack stack,MultiBufferSource buffers,int light,AbstractClientPlayer player,ModelPart arm,ModelPart sleeve,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(player!=mc.player||!mc.options.getCameraType().isFirstPerson())return;
        var pose=io.github.derkottersberg.seamlessrowing.client.RowingClient.capture(player,1,true);
        var model=((PlayerRenderer)(Object)this).getModel();
        if(pose!=null&&pose.owns(arm==model.leftArm))qa.rowing.FirstPersonOwnership.extraOwnedVanillaArms++;
    }
}
