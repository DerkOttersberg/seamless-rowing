package io.github.derkottersberg.seamlessrowing.mixin;

import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;

@Mixin(AvatarRenderState.class)
public class AvatarStateMixin implements RowingState {
    @Unique private RowingPose rowing$pose;
    @Unique private Matrix4f rowing$base;
    @Unique private boolean rowing$handsOnly;
    public RowingPose rowingPose(){return rowing$pose;}
    public void rowingPose(RowingPose pose){rowing$pose=pose;}
    public Matrix4f rowingBase(){return rowing$base;}
    public void rowingBase(Matrix4f base){rowing$base=base;}
    public boolean rowingHandsOnly(){return rowing$handsOnly;}
    public void rowingHandsOnly(boolean only){rowing$handsOnly=only;}
}
