package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.PaddlePose;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;

public record VanillaPaddleGeometry(boolean raft) implements PaddleGeometry {
    public boolean matches(net.minecraft.world.entity.vehicle.Boat boat,EntityRenderer<?> renderer) {
        // A modded entity may change its seat while reusing a native renderer. It needs a verified adapter.
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(boat.getType()).getNamespace().equals("minecraft")
                && renderer.getClass()==BoatRenderer.class && (boat.getVariant()==net.minecraft.world.entity.vehicle.Boat.Type.BAMBOO)==raft;
    }
    public Vec3 grip(Matrix4f boat,PaddlePose pose,boolean right) {
        Quaternionf paddle=new Quaternionf().rotationZYX(.19634955f,right ? (float)Math.PI-pose.yaw() : pose.yaw(),pose.pitch());
        Vector3f point=paddle.transform(new Vector3f(0,1,-4));
        point.add(3,raft ? -4 : -5,right ? -9 : 9).div(16);
        boat.transformPosition(point);
        return new Vec3(point.x,point.y,point.z);
    }
    public Vec3 shaft(Matrix4f boat,PaddlePose pose,boolean right) {
        Quaternionf paddle=new Quaternionf().rotationZYX(.19634955f,right ? (float)Math.PI-pose.yaw() : pose.yaw(),pose.pitch());
        Vector3f direction=paddle.transform(new Vector3f(0,0,1));boat.transformDirection(direction).normalize();
        return new Vec3(direction.x,direction.y,direction.z);
    }
}
