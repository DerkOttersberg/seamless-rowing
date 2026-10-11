package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import java.util.EnumSet;

/** Marker for our mutable polygon surfaces; cuboid caches cannot represent these faces. */
public final class SurfaceCube extends ModelPart.Cube {
    public SurfaceCube() {
        super(0,0,-2,0,-2,4,12,4,0,0,0,false,64,64,EnumSet.allOf(Direction.class));
    }
    @Override public void compile(com.mojang.blaze3d.vertex.PoseStack.Pose pose,
            com.mojang.blaze3d.vertex.VertexConsumer consumer,int light,int overlay,int color) {
        SurfaceRenderer.emit(this,pose,consumer,light,overlay,color);
    }
}
