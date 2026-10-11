# Minecraft 1.21.1 native port

Fabric, Forge 52 and NeoForge 21.1 use native immediate rendering hooks, remapped distribution JARs and loader-specific refmaps/access transformers. Shared mathematics and the approved continuous arm/spine surfaces remain unchanged. Native armor passes reuse their texture, trim and glint layers with copied articulated geometry. First-person projection omits third-person body overlays.

Build checks and 17 common regression tests pass. Packaged client, multiplayer and optional integration gates are in progress. This branch is a preview and is not production-ready. Compatibility claims require native test evidence tied to the final JAR hashes.
