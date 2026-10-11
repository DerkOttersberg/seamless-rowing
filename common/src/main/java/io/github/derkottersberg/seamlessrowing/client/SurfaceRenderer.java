package io.github.derkottersberg.seamlessrowing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

/** Submit current polygons through the native vertex consumer, including shader attributes. */
public final class SurfaceRenderer {
    private SurfaceRenderer() {}
    public static boolean owns(ModelPart part) {
        var access=(ModelPartAccess)(Object)part;
        return access.rowingCubes().stream().anyMatch(SurfaceCube.class::isInstance)
                ||access.rowingChildren().values().stream().anyMatch(SurfaceRenderer::owns);
    }
    public static void render(ModelPart part,PoseStack stack,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha) {
        if(!part.visible)return;
        stack.pushPose();
        try {
            part.translateAndRotate(stack);
            if(!part.skipDraw) {
                for(var cube:((ModelPartAccess)(Object)part).rowingCubes())cube.compile(stack.last(),consumer,light,overlay,red,green,blue,alpha);
            }
            for(var child:((ModelPartAccess)(Object)part).rowingChildren().values()) {
                if(owns(child))render(child,stack,consumer,light,overlay,red,green,blue,alpha);
                else child.render(stack,consumer,light,overlay,red,green,blue,alpha);
            }
        } finally {stack.popPose();}
    }
    public static void emit(ModelPart.Cube cube,PoseStack.Pose pose,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha) {
        var normal=new Vector3f();var position=new Vector3f();
        for(var face:cube.polygons) {
            if(face==null||face.vertices.length==0)continue;
            pose.normal().transform(normal.set(face.normal));
            for(var vertex:face.vertices) {
                pose.pose().transformPosition(vertex.pos.x/16,vertex.pos.y/16,vertex.pos.z/16,position);
                consumer.vertex(position.x,position.y,position.z,red,green,blue,alpha,vertex.u,vertex.v,overlay,light,normal.x,normal.y,normal.z);
            }
        }
    }
}

