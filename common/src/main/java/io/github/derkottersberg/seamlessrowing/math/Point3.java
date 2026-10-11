package io.github.derkottersberg.seamlessrowing.math;

public record Point3(double x, double y, double z) {
    public static final Point3 ZERO = new Point3(0, 0, 0);
    public Point3 {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("Nonfinite vector");
    }
    public Point3 add(Point3 b) { return new Point3(x+b.x, y+b.y, z+b.z); }
    public Point3 subtract(Point3 b) { return new Point3(x-b.x, y-b.y, z-b.z); }
    public Point3 scale(double n) { return new Point3(x*n, y*n, z*n); }
    public double dot(Point3 b) { return x*b.x+y*b.y+z*b.z; }
    public double length() { return Math.sqrt(dot(this)); }
    public Point3 unit() { double l=length(); return l<1e-9 ? ZERO : scale(1/l); }
    public static Point3 lerp(Point3 a, Point3 b, double t) { return a.add(b.subtract(a).scale(t)); }
}
