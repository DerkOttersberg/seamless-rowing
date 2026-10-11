package qa.rowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observes the actual model-part draw after visibility decisions from every mod. */
@Mixin(ModelPart.class)
public class LocalHeadProbe {

    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",at=@At("HEAD"))
    private void inspect(CallbackInfo ci){
        var mc=Minecraft.getInstance();var context=RenderContext.current();
        if(context==null||context.player!=mc.player||!mc.options.getCameraType().isFirstPerson())return;
        var renderer=mc.getEntityRenderDispatcher().getRenderer(mc.player);
        if(!(renderer instanceof PlayerRenderer player))return;
        var part=(ModelPart)(Object)this;var model=player.getModel();
        if(part!=model.head&&part!=model.hat||!part.visible||part.skipDraw||Math.abs(part.y)>100)return;
        if(context.rowingPose()!=null){qa.rowing.LocalHeadDraws.ownedHeads++;if(context.rowingHandsOnly())qa.rowing.LocalHeadDraws.handsHeads++;}
        else qa.rowing.LocalHeadDraws.unownedHeads++;
    }
}

