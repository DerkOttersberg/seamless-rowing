package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import java.util.*;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorLayerMixin extends RenderLayer<HumanoidRenderState,HumanoidModel<HumanoidRenderState>> {
    @Unique private final Map<Model<?>,RowingArmorModel[]> rowing$models=new IdentityHashMap<>();
    protected ArmorLayerMixin(RenderLayerParent<HumanoidRenderState,HumanoidModel<HumanoidRenderState>> parent) { super(parent); }
    @Redirect(method="renderArmorPiece",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V"))
    private void rowing$equipment(EquipmentLayerRenderer renderer,EquipmentClientInfo.LayerType type,ResourceKey<EquipmentAsset> asset,
            Model<HumanoidRenderState> original,Object renderState,ItemStack item,PoseStack stack,SubmitNodeCollector collector,int light,int outline) {
        HumanoidRenderState state=(HumanoidRenderState)renderState;
        if(!(state instanceof RowingState carrier) || carrier.rowingPose()==null || carrier.rowingBase()==null
                || !((Object)getParentModel() instanceof PlayerModel player) || !(original instanceof HumanoidModel<?> armor)) {
            renderer.renderLayers(type,asset,original,state,item,stack,collector,light,outline);return;
        }
        RowingPose row=carrier.rowingPose();
        boolean articulated=row.articulated() && ArmMesh.supported(armor.leftArm)&&ArmMesh.supported(armor.rightArm);
        if(row.articulated()&&!articulated) {
            // Keep skin and equipment in the same rigid pose when a joint layout cannot be sliced.
            carrier.rowingPose(row.rigid());
            player.setupAnim((net.minecraft.client.renderer.entity.state.AvatarRenderState)state);
            row=carrier.rowingPose();
        }
        var variants=rowing$models.computeIfAbsent(original,m->new RowingArmorModel[2]);int index=articulated?1:0;
        if(variants[index]==null) variants[index]=new RowingArmorModel(armor,articulated);
        original.setupAnim(state);
        Matrix4f transform=new Matrix4f(stack.last().pose()).invert().mul(carrier.rowingBase());
        var snapshot=variants[index].capture(armor,player,row,transform,carrier.rowingHandsOnly());
        renderer.renderLayers(type,asset,variants[index],snapshot,item,stack,collector,light,outline);
    }
}
