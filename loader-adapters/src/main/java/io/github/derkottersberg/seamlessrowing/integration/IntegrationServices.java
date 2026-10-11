package io.github.derkottersberg.seamlessrowing.integration;
import io.github.derkottersberg.seamlessrowing.client.RowingClient;
import io.github.derkottersberg.seamlessrowing.internal.ClientPlatformServices;
/** Explicitly constructed by each loader, with no reflective discovery or runtime framework. */
public abstract class IntegrationServices implements ClientPlatformServices {

    private boolean initialized,nea,pal,unsupported;
    private String status="Compatibility is local to this client.";
    public final void integrations() {
        if(initialized)return;initialized=true;
        try {
            if(isModLoaded("punchy")&&!modVersion("punchy").equals("2.8e"))unsupported=true;
            if(isModLoaded("realcamera")) {
                if(!modVersion("realcamera").equals("0.7.8-beta-44cc101"))unsupported=true;
                else io.github.derkottersberg.seamlessrowing.integration.RealCameraAdapter.register();
            }
            if(isModLoaded("firstperson")) {
                if(!modVersion("firstperson").startsWith("2.7.3")) unsupported=true;
                else io.github.derkottersberg.seamlessrowing.integration.FirstPersonAdapter.register();
            }
            if(isModLoaded("notenoughanimations")) {
                if(!java.util.Set.of("1.12.5","1.12.6").contains(modVersion("notenoughanimations"))) unsupported=true;
                else {io.github.derkottersberg.seamlessrowing.integration.NeaAdapter.register();nea=true;}
            }
            if(isModLoaded("player_animation_library")) {
                if(!modVersion("player_animation_library").equals("1.2.8+mc.26.3")) unsupported=true;
                else pal=true;
            }
            if(isModLoaded("entity_model_features") && modVersion("entity_model_features").startsWith("3.3.11"))
                io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.register();
        } catch(RuntimeException|LinkageError incompatible) {
            unsupported=true;
            org.slf4j.LoggerFactory.getLogger("Seamless Rowing").warn("Optional animation API unavailable; preserving existing rendering",incompatible);
        }
        status=unsupported ? "Unsupported animation API: rowing disabled."
                : (nea ? "NEA priority 1501. " : "")+(isModLoaded("firstperson") ? "FirstPersonModel. " : "")+(pal ? "PAL priority respected." : "");
    }
    public boolean animationSchedulerActive(){return nea;}
    public boolean paddleVisibilityHandoff(){return isModLoaded("punchy")&&modVersion("punchy").equals("2.8e");}
    public boolean unsupportedAnimationIntegration(){return unsupported;}
    public String integrationStatus(){return status;}
    public final void resourcesReloaded(net.minecraft.server.packs.resources.ResourceManager resources) {
        if(isModLoaded("entity_model_features") && modVersion("entity_model_features").startsWith("3.3.11"))
            io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.reload(resources);
    }
    public boolean playerGeometrySupported(net.minecraft.client.model.player.PlayerModel model) {
        if(!isModLoaded("entity_model_features"))return ClientPlatformServices.super.playerGeometrySupported(model);
        try{return io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.geometry(model);}
        catch(RuntimeException|LinkageError invalid){return false;}
    }
    public void prepareRowingModel(net.minecraft.client.model.player.PlayerModel model) {
        if(isModLoaded("entity_model_features"))io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.prepare(model);
    }
    public boolean externalAnimationActive(net.minecraft.client.player.AbstractClientPlayer player) {
        if(!pal) return false;
        try{return io.github.derkottersberg.seamlessrowing.integration.PalAdapter.active(player);}
        catch(RuntimeException|LinkageError invalid){unsupported=true;status="PAL rig unavailable: rowing safely disabled.";
            org.slf4j.LoggerFactory.getLogger("Seamless Rowing").warn(status,invalid);return true;}
    }
    public boolean customPlayerModel(net.minecraft.client.model.player.PlayerModel model) {
        if(!isModLoaded("entity_model_features")) return false;
        if(!modVersion("entity_model_features").startsWith("3.3.11")) {status="Unsupported EMF model API: existing visuals preserved.";return true;}
        try{
            boolean custom=io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.custom(model);
            if(custom) status="Custom EMF rigs keep their existing animations.";
            else if(io.github.derkottersberg.seamlessrowing.integration.EmfAdapter.freshAvailable())
                status="Fresh Player 1.1: Rowing owns its pose during strokes; Fresh resumes outside rowing.";
            return custom;
        }
        catch(RuntimeException|LinkageError invalid){status="EMF model unavailable: existing visuals preserved.";return true;}
    }
}
