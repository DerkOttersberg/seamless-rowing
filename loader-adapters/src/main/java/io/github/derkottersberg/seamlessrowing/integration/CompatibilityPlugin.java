package io.github.derkottersberg.seamlessrowing.integration;

import org.spongepowered.asm.mixin.extensibility.*;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;
import java.util.Set;

/** Each loader supplies its own early metadata query, without loading optional mod classes. */
public abstract class CompatibilityPlugin implements IMixinConfigPlugin {
    protected abstract String loadedVersion(String id);
    public void onLoad(String mixinPackage) {}
    public String getRefMapperConfig(){return null;}
    public boolean shouldApplyMixin(String target,String mixin) {return loadedVersion("punchy").equals("2.8e");}
    public void acceptTargets(Set<String> mine,Set<String> others) {}
    public List<String> getMixins(){return null;}
    public void preApply(String target,ClassNode node,String mixin,IMixinInfo info) {}
    public void postApply(String target,ClassNode node,String mixin,IMixinInfo info) {}
}
