package io.github.derkottersberg.seamlessrowing.fabric;
import io.github.derkottersberg.seamlessrowing.integration.CompatibilityPlugin;
import net.fabricmc.loader.api.FabricLoader;
public final class FabricCompatibilityPlugin extends CompatibilityPlugin {
    protected String loadedVersion(String id) {return FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getVersion().getFriendlyString()).orElse("");}
}
