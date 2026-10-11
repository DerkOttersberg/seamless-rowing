package io.github.derkottersberg.seamlessrowing.client;

/** Per-model grants from an optional animation scheduler; reset before every setup. */
public interface AnimationOwnership {
    void rowingGrant(boolean left);
    boolean rowingGranted(boolean left);
}
