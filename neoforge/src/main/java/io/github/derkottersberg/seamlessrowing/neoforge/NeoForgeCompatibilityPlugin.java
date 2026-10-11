package io.github.derkottersberg.seamlessrowing.neoforge;
import io.github.derkottersberg.seamlessrowing.integration.CompatibilityPlugin;
public final class NeoForgeCompatibilityPlugin extends CompatibilityPlugin {
    protected String loadedVersion(String id) {return net.neoforged.fml.loading.LoadingModList.get().getMods().stream().filter(m->m.getModId().equals(id)).map(m->m.getVersion().toString()).findFirst().orElse("");}
}
