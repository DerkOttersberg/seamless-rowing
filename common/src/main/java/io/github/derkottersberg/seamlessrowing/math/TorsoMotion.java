package io.github.derkottersberg.seamlessrowing.math;

import org.joml.Vector3f;

/** Fixed-seat spine: a restrained lumbar bend grows through the chest, anchored at the hips. */
public final class TorsoMotion {
    private TorsoMotion() {}
    public static boolean attachedHead(float x,float y,float z) {
        return Float.isFinite(x+y+z)&&Math.abs(x)<32&&Math.abs(y)<32&&Math.abs(z)<32;
    }
    public static Vector3f point(float x,float y,float z,float lean,float twist) {
        float h=12-y,angle=lean*(.55f+.9f*h/12),yaw=twist*h/12;
        float vertical,forward;
        if(Math.abs(lean)<.00001f) { vertical=h;forward=0; }
        else {
            double base=lean*.55,rate=lean*.9/12;
            vertical=(float)((Math.sin(base+rate*h)-Math.sin(base))/rate);
            forward=(float)((Math.cos(base)-Math.cos(base+rate*h))/rate);
        }
        float rx=(float)(x*Math.cos(yaw)+z*Math.sin(yaw));
        float rz=(float)(z*Math.cos(yaw)-x*Math.sin(yaw));
        return new Vector3f(rx,12-vertical-rz*(float)Math.sin(angle),-forward+rz*(float)Math.cos(angle));
    }
}
