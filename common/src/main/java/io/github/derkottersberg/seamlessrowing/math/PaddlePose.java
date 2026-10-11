package io.github.derkottersberg.seamlessrowing.math;

/** Vanilla's paddle angles, before its mirrored right-side orientation. */
public record PaddlePose(float pitch, float yaw) {
    public static PaddlePose at(float phase) {
        return new PaddlePose((float)Math.toRadians(-60+45*(Math.sin(-phase)+1)/2),
                (float)Math.toRadians(-45+90*(Math.sin(-phase+1)+1)/2));
    }
    public static PaddlePose mix(PaddlePose a, PaddlePose b, float t) {
        if(t<=0) return a;
        if(t>=1) return b;
        return new PaddlePose(a.pitch+(b.pitch-a.pitch)*t, a.yaw+(b.yaw-a.yaw)*t);
    }
}
