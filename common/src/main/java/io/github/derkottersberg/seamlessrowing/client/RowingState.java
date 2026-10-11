package io.github.derkottersberg.seamlessrowing.client;

import org.joml.Matrix4f;

public interface RowingState {
    RowingPose rowingPose();
    void rowingPose(RowingPose pose);
    Matrix4f rowingBase();
    void rowingBase(Matrix4f base);
    boolean rowingHandsOnly();
    void rowingHandsOnly(boolean handsOnly);
}
