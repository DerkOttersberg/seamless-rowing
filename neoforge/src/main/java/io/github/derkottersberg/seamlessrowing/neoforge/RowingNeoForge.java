package io.github.derkottersberg.seamlessrowing.neoforge;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
@Mod(value="seamlessrowing",dist=Dist.CLIENT)
public final class RowingNeoForge {
    public RowingNeoForge(ModContainer container) {RowingNeoForgeClient.initialize(container);}
}
