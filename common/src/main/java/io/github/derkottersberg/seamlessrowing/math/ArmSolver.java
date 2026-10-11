package io.github.derkottersberg.seamlessrowing.math;

/** Analytic two-bone IK. The pole fixes elbow direction and avoids frame-to-frame flips. */
public final class ArmSolver {
    private ArmSolver() {}
    public record Solution(Point3 shoulder, Point3 elbow, Point3 hand, double error) {}
    public static Solution solve(Point3 shoulder, Point3 target, Point3 pole, double upper, double lower) {
        if (!(upper>0 && lower>0 && Double.isFinite(upper+lower)))
            throw new IllegalArgumentException("Invalid bone lengths");
        Point3 delta=target.subtract(shoulder);
        double distance=delta.length();
        Point3 direction=distance>1e-8 ? delta.scale(1/distance) : new Point3(0,1,0);
        double reach=Math.max(Math.abs(upper-lower)+1e-6,Math.min(upper+lower-1e-6,distance));
        Point3 bend=pole.subtract(direction.scale(pole.dot(direction)));
        if (bend.length()<1e-6) {
            Point3 alternate=Math.abs(direction.x())<0.8 ? new Point3(1,0,0) : new Point3(0,0,1);
            bend=alternate.subtract(direction.scale(alternate.dot(direction)));
        }
        bend=bend.unit();
        double along=(upper*upper-lower*lower+reach*reach)/(2*reach);
        double height=Math.sqrt(Math.max(0, upper*upper-along*along));
        Point3 hand=shoulder.add(direction.scale(reach));
        Point3 elbow=shoulder.add(direction.scale(along)).add(bend.scale(height));
        return new Solution(shoulder,elbow,hand,hand.subtract(target).length());
    }
}
