package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import java.util.*;

/** Continuous shared rings, original UVs, no internal end caps. Updated only from a draw snapshot. */
final class TexturedSurface {
    interface Warp { Vector3f point(float x,float y,float z); }
    private record Face(ModelPart.Cube cube,int index,ModelPart.Vertex[] rest,float winding,Vector3f normal) {}
    private final List<ModelPart.Cube> cubes=new ArrayList<>();
    private final List<Face> faces=new ArrayList<>();
    TexturedSurface(ModelPart source,float center,float yOffset,float step,float cutoff) {
        for(var original:((ModelPartAccess)(Object)source).rowingCubes()) {
            float lo=Float.POSITIVE_INFINITY,hi=Float.NEGATIVE_INFINITY;
            for(var face:original.polygons) for(var v:face.vertices()) { lo=Math.min(lo,v.y());hi=Math.max(hi,v.y()); }
            lo=Math.max(lo,cutoff);
            for(float a=lo;a<hi-.0001f;a+=step) {
                float b=Math.min(hi,a+step);var band=emptyCube();
                for(int i=0;i<original.polygons.length;i++) {
                    var polygon=original.polygons[i];
                    if(Math.abs(polygon.normal().y())>.9) continue;
                    var clipped=clip(clip(Arrays.asList(polygon.vertices()),a,false),b,true);
                    if(clipped.size()!=4) throw new IllegalArgumentException("Unsupported surface topology");
                    bind(band,i,clipped,polygon,center,yOffset);
                }
            }
            var ends=emptyCube();
            final float start=lo;
            for(int i=0;i<original.polygons.length;i++) {
                var polygon=original.polygons[i];if(Math.abs(polygon.normal().y())<.9) continue;
                var vertices=Arrays.stream(polygon.vertices()).map(v->new ModelPart.Vertex(v.x(),Math.max(start,v.y()),v.z(),v.u(),v.v())).toList();
                bind(ends,i,vertices,polygon,center,yOffset);
            }
        }
        deform((x,y,z)->new Vector3f(x,y,z));
    }
    private ModelPart.Cube emptyCube() {
        var cube=new ModelPart.Cube(0,0,-2,0,-2,4,12,4,0,0,0,false,64,64,EnumSet.allOf(Direction.class));
        Arrays.fill(cube.polygons,new ModelPart.Polygon(new ModelPart.Vertex[0],new Vector3f(0,1,0)));
        cubes.add(cube);return cube;
    }
    private void bind(ModelPart.Cube cube,int index,List<ModelPart.Vertex> vertices,ModelPart.Polygon polygon,float center,float offset) {
        var rest=vertices.stream().map(v->new ModelPart.Vertex(v.x()-center,v.y()+offset,v.z(),v.u(),v.v())).toArray(ModelPart.Vertex[]::new);
        Vector3f cross=cross(rest);
        faces.add(new Face(cube,index,rest,cross.dot(polygon.normal())<0?-1:1,new Vector3f(polygon.normal())));
    }
    List<ModelPart.Cube> cubes() { return List.copyOf(cubes); }
    void deform(Warp warp) {
        for(var face:faces) {
            var vertices=Arrays.stream(face.rest).map(v->{var p=warp.point(v.x(),v.y(),v.z());return new ModelPart.Vertex(p.x,p.y,p.z,v.u(),v.v());}).toArray(ModelPart.Vertex[]::new);
            Vector3f normal=cross(vertices).mul(face.winding);
            if(normal.lengthSquared()<1e-12) normal.set(face.normal);else normal.normalize();
            face.cube.polygons[face.index]=new ModelPart.Polygon(vertices,normal);
        }
    }
    private static Vector3f cross(ModelPart.Vertex[] v) {
        return new Vector3f(v[1].x()-v[0].x(),v[1].y()-v[0].y(),v[1].z()-v[0].z())
                .cross(v[2].x()-v[0].x(),v[2].y()-v[0].y(),v[2].z()-v[0].z());
    }
    private static List<ModelPart.Vertex> clip(List<ModelPart.Vertex> input,float plane,boolean below) {
        List<ModelPart.Vertex> output=new ArrayList<>();
        for(int i=0;i<input.size();i++) {
            var a=input.get(i);var b=input.get((i+1)%input.size());
            boolean ia=below?a.y()<=plane:a.y()>=plane,ib=below?b.y()<=plane:b.y()>=plane;
            if(ia)output.add(a);
            if(ia!=ib) { float t=(plane-a.y())/(b.y()-a.y());output.add(new ModelPart.Vertex(a.x()+(b.x()-a.x())*t,plane,a.z()+(b.z()-a.z())*t,a.u()+(b.u()-a.u())*t,a.v()+(b.v()-a.v())*t)); }
        }
        return output;
    }
}
