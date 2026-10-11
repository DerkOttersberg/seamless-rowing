package io.github.derkottersberg.seamlessrowing.forge;
import io.github.derkottersberg.seamlessrowing.integration.CompatibilityPlugin;
public final class ForgeCompatibilityPlugin extends CompatibilityPlugin {
    protected String loadedVersion(String id) {return net.minecraftforge.fml.loading.LoadingModList.get().getMods().stream().filter(m->m.getModId().equals(id)).map(m->m.getVersion().toString()).findFirst().orElse("");}
}
