package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import io.github.derkottersberg.seamlessrowing.mixin.PolygonAccess;

/** Preserve the source UVs while using the legacy native quad layout. */
final class NativeMesh {
    static ModelPart.Polygon face(ModelPart.Vertex[] vertices,Vector3f normal) {
        var zero=new ModelPart.Vertex(0,0,0,0,0);
        var polygon=new ModelPart.Polygon(new ModelPart.Vertex[]{zero,zero,zero,zero},0,0,1,1,64,64,false,Direction.UP);
        ((PolygonAccess)(Object)polygon).rowingVertices(vertices);
        polygon.normal.set(normal);
        return polygon;
    }
}
