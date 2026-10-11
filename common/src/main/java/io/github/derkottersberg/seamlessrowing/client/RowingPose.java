package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.PaddlePose;
import net.minecraft.world.phys.Vec3;

/** Immutable geometry/ownership captured during entity extraction. Distances are world units. */
public record RowingPose(Vec3 leftGrip, Vec3 rightGrip, float leftWeight, float rightWeight,
                        boolean ownLeft, boolean ownRight, boolean articulated, float lean, float twist,
                        PaddlePose leftPaddle, PaddlePose rightPaddle, Vec3 leftShaft, Vec3 rightShaft) {
    public boolean owns(boolean left) { return left ? ownLeft : ownRight; }
    public float weight(boolean left) { return left ? leftWeight : rightWeight; }
    public Vec3 grip(boolean left) { return left ? leftGrip : rightGrip; }
    public Vec3 shaft(boolean left) { return left ? leftShaft : rightShaft; }
    public RowingPose rigid() { return new RowingPose(leftGrip,rightGrip,leftWeight,rightWeight,ownLeft,ownRight,false,lean,twist,leftPaddle,rightPaddle,leftShaft,rightShaft); }
}
