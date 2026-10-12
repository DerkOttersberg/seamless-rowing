package io.github.derkottersberg.seamlessrowing.math;

import org.joml.*;
import java.lang.Math;

/** Anatomical bend plane in vanilla model space: +Y down, +Z behind the player. */
public final class RowingArmKinematics {
    private RowingArmKinematics() {}
    public record Pose(ArmSolver.Solution joints, Quaternionf upper, Quaternionf lower) {
        public Pose { upper=new Quaternionf(upper); lower=new Quaternionf(lower); }
        @Override public Quaternionf upper() { return new Quaternionf(upper); }
        @Override public Quaternionf lower() { return new Quaternionf(lower); }
    }
    public static Pose solve(Point3 shoulder,Point3 target,boolean left,Point3 shaft) {
        // Rotate the bend pole with the sagittal reach direction. A fixed posterior
        // pole reverses the hinge when native boat banking lifts a grip above the shoulder.
        Point3 reach=target.subtract(shoulder);
        double sagittal=Math.hypot(reach.y(),reach.z());
        Point3 pole=sagittal>1e-6 ? new Point3(left?.25:-.25,-reach.z()/sagittal,reach.y()/sagittal)
                : new Point3(left?.25:-.25,0,1);
        var joints=ArmSolver.solve(shoulder,target,pole,6,6);
        Vector3f upperY=vector(joints.elbow().subtract(shoulder)).normalize();
        Vector3f lowerY=vector(joints.hand().subtract(joints.elbow())).normalize();
        Vector3f hinge=new Vector3f(lowerY).cross(upperY).normalize();
        Quaternionf upper=frame(hinge,upperY);
        // Construct the hinge directly rather than multiplying near-collinear frames;
        // the latter introduces sideways bend noise near full extension.
        float bend=(float)Math.atan2(new Vector3f(upperY).cross(lowerY).length(),upperY.dot(lowerY));
        Quaternionf lower=new Quaternionf().rotationX(-bend);
        Vector3f lowerZ=new Vector3f(hinge).cross(lowerY).normalize();
        Vector3f paddle=vector(shaft);
        float across=paddle.dot(hinge),forward=paddle.dot(lowerZ);
        float projected=across*across+forward*forward;
        // Small, continuous pronation follows the shaft without a 180-degree wrist flip.
        lower.rotateY(.25f*2*across*forward/(projected+.25f));
        return new Pose(joints,upper,lower);
    }
    private static Quaternionf frame(Vector3f hinge,Vector3f y) {
        return new Quaternionf().setFromNormalized(new Matrix3f(hinge,y,new Vector3f(hinge).cross(y).normalize()));
    }
    private static Vector3f vector(Point3 p) { return new Vector3f((float)p.x(),(float)p.y(),(float)p.z()); }
}
