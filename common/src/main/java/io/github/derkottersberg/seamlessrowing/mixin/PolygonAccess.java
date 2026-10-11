package io.github.derkottersberg.seamlessrowing.mixin;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ModelPart.Polygon.class)
public interface PolygonAccess {
    @Mutable @Accessor("vertices") void rowingVertices(ModelPart.Vertex[] vertices);
}
