package io.github.derkottersberg.seamlessrowing.internal;

import java.nio.file.Path;

public interface PlatformServices {
    Path configDirectory();
    boolean isModLoaded(String id);
    String modVersion(String id);
}
