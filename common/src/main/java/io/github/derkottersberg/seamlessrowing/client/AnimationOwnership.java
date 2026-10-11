package io.github.derkottersberg.seamlessrowing.client;

/** Per-model grants from an optional animation scheduler; reset before every setup. */
public interface AnimationOwnership {
    default void rowingRestorePose(){}
    void rowingGrant(boolean left);
    boolean rowingGranted(boolean left);
}
