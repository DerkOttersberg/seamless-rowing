package io.github.derkottersberg.seamlessrowing.forge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod("seamlessrowing")
public final class RowingForge {
    public RowingForge() {
        if(FMLEnvironment.dist.isClient())RowingForgeClient.initialize(FMLJavaModLoadingContext.get());
    }
}
