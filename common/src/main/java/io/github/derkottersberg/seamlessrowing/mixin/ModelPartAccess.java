package io.github.derkottersberg.seamlessrowing.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.*;

@Mixin(ModelPart.class)
public interface ModelPartAccess {
    @Accessor("cubes") List<ModelPart.Cube> rowingCubes();
    @Mutable @Accessor("cubes") void rowingCubes(List<ModelPart.Cube> cubes);
    @Accessor("children") Map<String,ModelPart> rowingChildren();
}
