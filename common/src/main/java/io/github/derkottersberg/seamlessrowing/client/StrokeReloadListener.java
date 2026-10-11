package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.function.Consumer;

/** Vanilla resource lifecycle; loader registration and optional integrations are supplied explicitly. */
public final class StrokeReloadListener extends SimplePreparableReloadListener<float[]> {
    private final Consumer<ResourceManager> resourcesReloaded;
    public StrokeReloadListener(Consumer<ResourceManager> resourcesReloaded) {this.resourcesReloaded=resourcesReloaded;}
    protected float[] prepare(ResourceManager resources,ProfilerFiller profiler) {return StrokeProfile.prepare(resources);}
    protected void apply(float[] keys,ResourceManager resources,ProfilerFiller profiler) {
        StrokeProfile.apply(keys);RowingClient.clear();resourcesReloaded.accept(resources);
    }
}
