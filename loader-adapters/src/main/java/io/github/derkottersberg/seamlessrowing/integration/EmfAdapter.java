package io.github.derkottersberg.seamlessrowing.integration;

import net.minecraft.client.model.player.PlayerModel;
import traben.entity_model_features.models.IEMFModel;
import traben.entity_model_features.EMFAnimationApi;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Uses EMF's public per-entity model condition, never a persistent model/animation lock. */
public final class EmfAdapter {
    private static boolean fresh;
    public static void reload(ResourceManager resources) {
        fresh=FreshPlayerRig.known(digest(resources,"player.jem"),digest(resources,"player_slim.jem"));
    }
    private static String digest(ResourceManager resources,String file) {
        var resource=resources.getResource(Identifier.fromNamespaceAndPath("minecraft","emf/cem/"+file));
        if(resource.isEmpty())return "";
        try(var input=resource.get().open()) {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
        } catch(java.io.IOException|java.security.NoSuchAlgorithmException invalid) {return "";}
    }
    public static boolean freshAvailable(){return fresh;}
    public static void prepare(PlayerModel model) {
        if(fresh && (Object)model instanceof IEMFModel emf) {
            var root=emf.emf$getEMFRootModel();
            if(root!=null&&geometry(root.vanillaRoot))root.setVariantStateTo(0);
        }
    }
    public static void register() {
        reload(Minecraft.getInstance().getResourceManager());
        try {
            EMFAnimationApi.registerVanillaModelCondition(entity -> fresh
                    && (Object)entity instanceof AbstractClientPlayer player && RowingClient.requestsRowingRig(player));
        } catch(Exception unavailable) {throw new IllegalStateException("EMF model ownership condition unavailable",unavailable);}
    }
    public static boolean geometry(PlayerModel model) {
        if(fresh && (Object)model instanceof IEMFModel emf) {
            var root=emf.emf$getEMFRootModel();
            if(root==null)return false;
            return geometry(root.vanillaRoot);
        }
        return TorsoSurface.supported(model.body)&&ArmMesh.supported(model.leftArm)&&ArmMesh.supported(model.rightArm);
    }
    private static boolean geometry(ModelPart root) {
        try{return TorsoSurface.supported(root.getChild("body"))
                && ArmMesh.supported(root.getChild("left_arm"))&&ArmMesh.supported(root.getChild("right_arm"));}
        catch(java.util.NoSuchElementException missing){return false;}
    }
    public static boolean custom(PlayerModel model) {
        if(!((Object)model instanceof IEMFModel emf)) return false;
        var root=emf.emf$getEMFRootModel();
        return emf.emf$isEMFModel()&&(root==null||root.containsCustomModel||root.containsCustomAnims)
                && !(fresh&&geometry(model));
    }
}
