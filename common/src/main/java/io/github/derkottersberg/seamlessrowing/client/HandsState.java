package io.github.derkottersberg.seamlessrowing.client;

public interface HandsState {
    boolean rowingOwnMain();
    boolean rowingOwnOff();
    void rowingOwnership(boolean main,boolean off);
}
