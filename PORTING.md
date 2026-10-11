# Version ports

One repository, one branch per Minecraft line, containing the applicable loader modules. No runtime SeamlessLib or animation framework dependency.

| Branch | Loaders | Java | Current gate |
|---|---|---:|---|
| 26.3 | Fabric, Forge, NeoForge | 25 | All compile; packaged compatibility and recovery tests in progress |
| 26.2 | Fabric, Forge, NeoForge | 25 | Native port pending |
| 1.21.1 | Fabric, Forge, NeoForge | 21 | Native port pending |
| 1.20.1 | Fabric, Forge | 17 | Native port pending |

26.x uses official unobfuscated names and deferred draw snapshots. Legacy branches must adapt the immediate rendering pipeline, skin/armor APIs, model mesh access and settings input, and produce remapped distribution JARs with working refmaps. Catalog and loader pins come from the corresponding existing Seamless Dogs branches.

The QA modules are separate mods, never distribution dependencies. Runtime fixtures use disposable worlds, private Xvfb displays and official packaged loaders. Reports must record actual product JAR hashes, exact optional mod versions and limitations. No build is production-ready until its applicable gates pass.

Fresh Player Extension 1.1 is recognized by the original player model resource hashes. While rowing, its EMF adapter selects native geometry; outside rowing Fresh resumes. Other custom rigs retain their rendering. The pack and third-party assets are not redistributed. Optional API support is version-specific and must be checked again for each port.
