package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.TorsoMotion;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.geom.ModelPart;
import java.util.*;

/** Retains the native body part, texture and renderer, and restores its mesh before ordinary poses. */
public final class TorsoSurface {
    private record Binding(List<ModelPart.Cube> original,TexturedSurface surface) {}
    private static final Map<ModelPart,Binding> BINDINGS=new WeakHashMap<>();
    private TorsoSurface() {}
    public static boolean supported(ModelPart part) {
        var access=(ModelPartAccess)(Object)part;var binding=BINDINGS.get(part);
        var cubes=binding==null?access.rowingCubes():binding.original;
        if(cubes.size()!=1)return false;var cube=cubes.getFirst();
        return Math.abs(cube.minY)<.01 && Math.abs(cube.maxY-12)<.01
                && Math.abs(cube.maxX-cube.minX-8)<.01 && Math.abs(cube.maxZ-cube.minZ-4)<.01
                && cube.polygons.length==6 && Arrays.stream(cube.polygons).allMatch(p->p!=null&&p.vertices().length==4);
    }
    public static void restore(ModelPart part) {
        var binding=BINDINGS.get(part);
        var access=(ModelPartAccess)(Object)part;
        // A custom-model adapter may have selected another mesh since our last draw.
        // Restore only the mesh we installed; never insert vanilla cubes into that variant.
        if(binding!=null&&access.rowingCubes()==binding.surface.cubes())access.rowingCubes(binding.original);
        ((ModelPartAccess)(Object)part).rowingChildren().values().forEach(TorsoSurface::restore);
    }
    public static void apply(ModelPart part,float lean,float twist) {
        if(Math.abs(lean)+Math.abs(twist)<1e-6f) return;
        var access=(ModelPartAccess)(Object)part;
        var binding=BINDINGS.get(part);
        if(binding==null) {
            if(!supported(part))return;var cubes=access.rowingCubes();
            binding=new Binding(cubes,new TexturedSurface(part,0,0,.5f,Float.NEGATIVE_INFINITY));
            BINDINGS.put(part,binding);
        }
        binding.surface.deform((x,y,z)->TorsoMotion.point(x,y,z,lean,twist));
        access.rowingCubes(binding.surface.cubes());
        access.rowingChildren().values().forEach(child->apply(child,lean,twist));
    }
}
