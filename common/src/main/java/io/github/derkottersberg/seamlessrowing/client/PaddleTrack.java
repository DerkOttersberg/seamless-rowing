package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.math.PaddlePose;

/** Tick-owned transition state. Sampling never changes the animation clock. */
public final class PaddleTrack {
    private boolean active;
    private int age=100;
    private float oldWeight, weight;
    private float fromWeight;
    private PaddlePose from=PaddlePose.at(0), last=PaddlePose.at(0);
    private float fromLean,lastLean;
    public void tick(boolean nextActive, float phase) {
        oldWeight=weight;
        if (nextActive!=active) { from=last; fromLean=lastLean; fromWeight=weight; age=0; active=nextActive; }
        age++;
        float eased=ease(age/(active ? 6f : 4f));
        weight=fromWeight+((active ? 1f : 0f)-fromWeight)*eased;
        last=sample(phase,1);
        lastLean=lean(phase,1);
    }
    public float weight(float partial) { return oldWeight+(weight-oldWeight)*partial; }
    public PaddlePose sample(float phase, float partial) {
        float t=ease((age-1+partial)/(active ? 6f : 4f));
        return PaddlePose.mix(from,PaddlePose.at(active ? phase : 0),t);
    }
    public float lean(float phase,float partial) {
        float t=ease((age-1+partial)/(active ? 6f : 4f));
        return fromLean+((active ? StrokeProfile.lean(phase) : 0)-fromLean)*t;
    }
    private static float ease(float value) {
        float t=Math.clamp(value,0f,1f);
        return t*t*t*(t*(t*6-15)+10);
    }
}
