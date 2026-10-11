package io.github.derkottersberg.seamlessrowing.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A rounded skin envelope around two rigid bones. The wrist and bone lengths do not move. */
public final class ArmSurface {
    private final Quaternionf lower;
    private final Vector3f direction;
    public ArmSurface(Quaternionf lower) {
        this.lower=new Quaternionf(lower);
        direction=new Vector3f(0,1,0).rotate(lower);
    }
    public Vector3f point(float x,float y,float z) {
        if(y<=2) return new Vector3f(x,y,z);
        if(y>=10) return new Vector3f(x,y-6,z).rotate(lower).add(0,6,0);
        float t=(y-2)/8,one=1-t;
        // Quadratic corner rounding, tangent to both bones, with identical shared cross-sections.
        Vector3f center=new Vector3f(0,2*one*one+12*one*t+6*t*t,0)
                .add(new Vector3f(direction).mul(4*t*t));
        Vector3f tangent=new Vector3f(0,one,0).add(new Vector3f(direction).mul(t)).normalize();
        Quaternionf frame=new Quaternionf().slerp(lower,t);
        Vector3f frameY=new Vector3f(0,1,0).rotate(frame);
        frame.premul(new Quaternionf().rotationTo(frameY,tangent));
        // A gentle elbow taper prevents the inner fold bulging through itself on tight reaches.
        float taper=1-.22f*(1-direction.y)*16*t*t*one*one;
        return new Vector3f(x,0,z*Math.max(.6f,taper)).rotate(frame).add(center);
    }
}
