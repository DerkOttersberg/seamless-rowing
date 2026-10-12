# Version ports

One repository, one branch per Minecraft line, containing the applicable loader modules. No runtime SeamlessLib or animation framework dependency.

| Branch | Loaders | Java | Current gate |
|---|---|---:|---|
| 26.3 | Fabric, Forge, NeoForge | 25 | Accepted against final JAR hashes; see packaged acceptance |
| 26.2 | Fabric, Forge, NeoForge | 25 | Accepted against final JAR hashes; see packaged acceptance |
| 1.21.1 | Fabric, Forge, NeoForge | 21 | Accepted against final JAR hashes; see packaged acceptance |
| 1.20.1 | Fabric, Forge | 17 | Accepted against final JAR hashes; see packaged acceptance |

26.x uses official unobfuscated names and deferred draw snapshots. Legacy branches adapt the immediate rendering pipeline, skin/armor APIs, model mesh access and settings input, and produce remapped distribution JARs with working refmaps. Catalog and loader pins come from the corresponding existing Seamless Dogs branches. Legacy quaternion-to-Euler conversion avoids the incompatible XYZ accessor in its bundled JOML version.

The QA modules are separate mods, never distribution dependencies. Runtime fixtures use disposable worlds, private Xvfb displays and official packaged loaders. Reports must record actual product JAR hashes, exact optional mod versions and limitations. All 11 builds passed their applicable current-byte gates. Exact scope, optional versions, failed history and limitations are recorded in [packaged acceptance](docs/PORT_ACCEPTANCE.md).

Fresh Player Extension 1.1 is recognized by the original player model resource hashes. While rowing, its EMF adapter selects native geometry; outside rowing Fresh resumes. Other custom rigs retain their rendering. The pack and third-party assets are not redistributed. Optional API support is version-specific and must be checked again for each port.

Native tests use an owned disposable WSL instance, private displays and worlds, with two explicit CPU-limited display slots. Acceptance records include failures and exact add-on hashes. Installed gameplay add-ons may require their own server installation; this is separate from Rowing's client-only behavior. In particular, Better Combat on NeoForge cannot be tested against a vanilla server merely by installing it on the client.

Mutable arm and torso surfaces have a native polygon submission path. Both ordinary ModelPart draws and EMF's direct cube compilation must bypass cached static cuboids. Merely checking the stored mesh would miss a graphics mod drawing an older cuboid and incorrect texture coordinates; the packaged QA mod records real vertex submissions.

The shared elbow solver keeps its bend plane stable when a native banked grip crosses shoulder height and uses a direct hinge rotation near straight reaches. Older renderers use vanilla linear yaw interpolation and apply submerged bubble wobble; modern render state uses its own wrapped-yaw and submerged-wobble rules. Native boundary, bubble, armor-visibility and vertex probes validate these differences.
