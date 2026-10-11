package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import java.util.*;

/** Cuts existing textured quads at the elbow; UVs remain in the source skin/armor layout. */
public final class ArmMesh {
    private ArmMesh() {}
    public static boolean supported(ModelPart part) {
        var cubes=((ModelPartAccess)(Object)part).rowingCubes();
        if(cubes.size()!=1) return false;
        var cube=cubes.getFirst();
        return cube.polygons.length==6 && Arrays.stream(cube.polygons).allMatch(p->p!=null&&p.vertices().length==4)
                && Math.abs((cube.maxY-cube.minY)-12)<.01
                && Math.abs((cube.maxZ-cube.minZ)-4)<.01
                && Math.abs(cube.minY+2)<.01 && Math.abs(cube.maxY-10)<.01
                && ((cube.maxX-cube.minX)==3 || (cube.maxX-cube.minX)==4)
                && ((ModelPartAccess)(Object)part).rowingChildren().keySet().stream().allMatch(n->n.endsWith("sleeve"));
    }
    public static List<ModelPart.Cube> slice(ModelPart source,boolean upper,float center) {
        List<ModelPart.Cube> result=new ArrayList<>();
        for(var cube:((ModelPartAccess)(Object)source).rowingCubes()) {
            ModelPart.Cube copy=new ModelPart.Cube(0,0,-2,-2,-2,4,12,4,0,0,0,false,64,64,EnumSet.allOf(Direction.class));
            for(int i=0;i<cube.polygons.length;i++) {
                var polygon=cube.polygons[i];
                var input=Arrays.asList(polygon.vertices());
                List<ModelPart.Vertex> clipped=clip(input,upper);
                // Fill the newly exposed end with the original end-face UVs.
                if(clipped.isEmpty()) clipped=input.stream().map(v->new ModelPart.Vertex(v.x(),4,v.z(),v.u(),v.v())).toList();
                if(clipped.size()!=4) throw new IllegalArgumentException("Unexpected arm polygon topology");
                ModelPart.Vertex[] vertices=clipped.stream().map(v->new ModelPart.Vertex(v.x()-center,
                        v.y()+(upper ? 2 : -4),v.z(),v.u(),v.v())).toArray(ModelPart.Vertex[]::new);
                copy.polygons[i]=new ModelPart.Polygon(vertices,polygon.normal());
            }
            result.add(copy);
        }
        return List.copyOf(result);
    }
    /** Remove the shoulder end near the first-person camera without moving the elbow or hand. */
    public static List<ModelPart.Cube> firstPersonUpper(ModelPart source,float center) {
        List<ModelPart.Cube> result=new ArrayList<>();
        for(var cube:((ModelPartAccess)(Object)source).rowingCubes()) {
            ModelPart.Cube copy=new ModelPart.Cube(0,0,-2,-2,-2,4,12,4,0,0,0,false,64,64,EnumSet.allOf(Direction.class));
            for(int i=0;i<cube.polygons.length;i++) {
                var polygon=cube.polygons[i];var input=Arrays.asList(polygon.vertices());
                var clipped=clip(clip(input,true,4),false,2);
                if(clipped.isEmpty()) {
                    float plane=input.getFirst().y()<2 ? 2 : 4;
                    clipped=input.stream().map(v->new ModelPart.Vertex(v.x(),plane,v.z(),v.u(),v.v())).toList();
                }
                var vertices=clipped.stream().map(v->new ModelPart.Vertex(v.x()-center,v.y()+2,v.z(),v.u(),v.v())).toArray(ModelPart.Vertex[]::new);
                copy.polygons[i]=new ModelPart.Polygon(vertices,polygon.normal());
            }
            result.add(copy);
        }
        return List.copyOf(result);
    }
    private static List<ModelPart.Vertex> clip(List<ModelPart.Vertex> input,boolean upper) {
        return clip(input,upper,4);
    }
    private static List<ModelPart.Vertex> clip(List<ModelPart.Vertex> input,boolean upper,float plane) {
        List<ModelPart.Vertex> output=new ArrayList<>();
        for(int i=0;i<input.size();i++) {
            var a=input.get(i); var b=input.get((i+1)%input.size());
            boolean insideA=upper ? a.y()<=plane : a.y()>=plane;
            boolean insideB=upper ? b.y()<=plane : b.y()>=plane;
            if(insideA) output.add(a);
            if(insideA!=insideB) {
                float t=(plane-a.y())/(b.y()-a.y());
                output.add(new ModelPart.Vertex(a.x()+(b.x()-a.x())*t,plane,a.z()+(b.z()-a.z())*t,
                        a.u()+(b.u()-a.u())*t,a.v()+(b.v()-a.v())*t));
            }
        }
        return output;
    }
}
