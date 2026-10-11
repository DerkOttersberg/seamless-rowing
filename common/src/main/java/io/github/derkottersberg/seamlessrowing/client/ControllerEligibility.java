package io.github.derkottersberg.seamlessrowing.client;

/** Pure eligibility policy shared by every version adapter. */
public record ControllerEligibility(boolean alive,boolean sleeping,boolean spectator,
                                    boolean seatedInBoat,boolean controller,boolean matchingGeometry) {
    public boolean allowed() { return alive&&!sleeping&&!spectator&&seatedInBoat&&controller&&matchingGeometry; }
}
