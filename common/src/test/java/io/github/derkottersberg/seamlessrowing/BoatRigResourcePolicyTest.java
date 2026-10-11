package io.github.derkottersberg.seamlessrowing;
import io.github.derkottersberg.seamlessrowing.client.BoatRigResourcePolicy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BoatRigResourcePolicyTest {
    @Test void replacementModelsYieldButTexturesAndPlayerRigsDoNot(){
        assertTrue(BoatRigResourcePolicy.replacement("minecraft","emf/cem/oak_boat.jem"));
        assertTrue(BoatRigResourcePolicy.replacement("minecraft","optifine/cem/boat_oak.jem"));
        assertTrue(BoatRigResourcePolicy.replacement("minecraft","emf/cem/bamboo_chest_raft.jem"));
        assertFalse(BoatRigResourcePolicy.replacement("minecraft","emf/cem/player.jem"));
        assertFalse(BoatRigResourcePolicy.replacement("minecraft","textures/entity/boat/oak.png"));
        assertFalse(BoatRigResourcePolicy.replacement("other","emf/cem/boat.jem"));
    }
}
