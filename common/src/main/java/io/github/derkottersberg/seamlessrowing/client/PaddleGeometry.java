package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.PaddlePose;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Explicit geometry adapter. An unrecognized renderer never acquires rowing visuals. */
public interface PaddleGeometry {
    boolean matches(net.minecraft.world.entity.vehicle.boat.AbstractBoat boat,EntityRenderer<?,?> renderer);
    Vec3 grip(Matrix4f renderedBoatTransform,PaddlePose pose,boolean right);
    Vec3 shaft(Matrix4f renderedBoatTransform,PaddlePose pose,boolean right);
}
