package io.github.derkottersberg.seamlessrowing.fabric.integration;

import net.minecraft.client.model.player.PlayerModel;
import traben.entity_model_features.models.IEMFModel;

/** Vanilla EMF wrappers may render; authored custom models/animations keep full ownership. */
public final class EmfAdapter {
    public static boolean custom(PlayerModel model) {
        if(!((Object)model instanceof IEMFModel emf)) return false;
        var root=emf.emf$getEMFRootModel();
        return emf.emf$isEMFModel()&&(root==null||root.containsCustomModel||root.containsCustomAnims);
    }
}
