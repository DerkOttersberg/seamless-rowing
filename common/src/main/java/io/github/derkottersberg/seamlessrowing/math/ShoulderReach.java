package io.github.derkottersberg.seamlessrowing.math;

import org.joml.Vector3f;

/** Small scapular protraction keeps the elbow from locking at the end of a fixed-seat reach. */
public final class ShoulderReach {
    private ShoulderReach() {}
    public static Vector3f adjust(Vector3f shoulder,Vector3f target,float weight) {
        var direction=new Vector3f(target).sub(shoulder);float distance=direction.length();
        float excess=distance-11.5f;
        if(excess<=0)return new Vector3f(shoulder);
        float shift=excess<.6f?excess*excess/1.2f:excess-.3f;
        return direction.mul(Math.min(1.5f,shift)*Math.clamp(weight,0,1)/distance).add(shoulder);
    }
}
