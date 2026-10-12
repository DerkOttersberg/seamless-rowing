package io.github.derkottersberg.seamlessrowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.*;
@Mixin(HumanoidArmorLayer.class)
public class ArmorLayerMixin {
    @Unique private final Map<HumanoidModel<?>,RowingArmorModel> rowing$models=new IdentityHashMap<>();
    @Inject(method="getArmorModel",at=@At("RETURN"),cancellable=true)
    private void articulated(EquipmentSlot slot,CallbackInfoReturnable<HumanoidModel<?>> cir){
        var context=RenderContext.current();var source=cir.getReturnValue();
        if(context==null||context.rowingPose()==null
                ||(source.getClass()!=HumanoidModel.class&&source.getClass()!=HumanoidArmorModel.class)
                ||!ArmMesh.supported(source.leftArm)||!ArmMesh.supported(source.rightArm))return;
        cir.setReturnValue(rowing$models.computeIfAbsent(source,RowingArmorModel::new));
    }
}
