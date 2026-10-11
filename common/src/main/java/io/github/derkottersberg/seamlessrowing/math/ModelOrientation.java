package io.github.derkottersberg.seamlessrowing.math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
/** Native Z-Y-X model angles, avoiding the roll conversion defect in bundled JOML 1.10.5. */
public final class ModelOrientation {
    private ModelOrientation(){}
    public static Vector3f angles(Quaternionf q){
        double x=q.x,y=q.y,z=q.z,w=q.w,n=x*x+y*y+z*z+w*w;
        if(!(n>1e-12)||!Double.isFinite(n))throw new IllegalArgumentException("Invalid model rotation");
        double pitch=Math.asin(Math.max(-1,Math.min(1,2*(w*y-z*x)/n)));
        double roll=Math.atan2(2*(w*x+y*z),w*w-x*x-y*y+z*z);
        double yaw=Math.atan2(2*(w*z+x*y),w*w+x*x-y*y-z*z);
        return new Vector3f((float)roll,(float)pitch,(float)yaw);
    }
}
