# Seamless Rowing

Client-only boat rowing animation with paddle-driven articulated arms, original fixed-seat torso poses and native skin/armor rendering. All Rights Reserved, with modpack inclusion permission in `LICENSE`.

## Preview

Preview **0.1.0-preview.4** retains the approved spine stroke and continuous textured elbows, and adds tested handoffs for Fresh Player Extension 1.1 and compatible first-person mods. Minecraft 26.3 Fabric, Forge and NeoForge adapters are implemented; packaged acceptance is in progress. Fabric requires Fabric API; Forge and NeoForge use their native APIs. SeamlessLib and a runtime animation framework are not required. A vanilla multiplayer server works unchanged; each viewer needs this mod to see its animation.

Use normal boat controls. Only the controlling rider animates. Forward/turning strokes follow native paddles; backwards-only movement keeps vanilla behavior. Item use releases both hands, and attacking releases the active hand. Selected items are hidden visually while their hand rows; inventory and boat physics are unchanged. Sounds remain vanilla, including resource-pack replacements.

Bind **Open rowing settings** in Options → Controls → Key Binds → Seamless Rowing. It starts unbound. Optional Mod Menu exposes the same screen. The settings screen has enable, first person, third person, articulated elbows and torso toggles. Save writes a validated client JSON with an atomic replacement and previous-file backup; Cancel discards the draft.

Unrecognized replacement player/boat renderers and modded boat IDs retain their existing rendering. The 3D Skin Layers fallback uses rigid third-person arms and preserves its first-person rendering. Only version-matched optional APIs are enabled; unsupported versions disable rowing and appear in compatibility status. Custom boat resource-pack geometry remains a compatibility gate. See [the current preview report](docs/MOTION_PREVIEW_ACCEPTANCE.md) for tested combinations and limits, and the [elbow correction](docs/ELBOW_PREVIEW_ACCEPTANCE.md) and [first preview](docs/PREVIEW_ACCEPTANCE.md) reports for historical evidence. This preview is not an all-version production release.

## Build and architecture

Run `gradlew.bat clean check build :qa:jar` with Java 25. The distributable is in `fabric/build/libs`; **never install or distribute the separate QA JAR as the mod**.

`common` owns eligibility, paddle geometry, interpolation, fixed-length IK, immutable pose snapshots, settings and narrow rendering hooks. Explicit Fabric, Forge and NeoForge services supply lifecycle, keys, resource reload and menus. Their shared `loader-adapters` source directory contains optional integrations and is excluded from `common`. Architectury/Loom are build tools only. There is no new networking protocol, server entrypoint or library shading. The 26.3 deferred renderer receives captured visibility and pose data rather than advancing animation during rendering.

`qa` is a separate packaged-client fixture and independent native-render probe. It compares submitted hand tips with the native paddle's actual model matrix in the same frame. Unit tests cover eligibility, mirrored IK, unreachable targets, render-read purity, transitions, resource clips and settings preservation. See `animation-authoring` for original source poses.

## Version branches after preview approval

One repository per product, with one branch per Minecraft line containing its loaders:

| Branch | Planned loaders | Java |
|---|---|---:|
| 26.3 | Fabric, Forge, NeoForge | 25 |
| 26.2 | Fabric, Forge, NeoForge | 25 |
| 1.21.1 | Fabric, Forge, NeoForge | 21 |
| 1.20.1 | Fabric, Forge | 17 |

The approved preview is being ported to this matrix. Every line requires its own native adapters, packaged tests and final-hash acceptance evidence; compiling a port does not establish release readiness. GitHub publication and CurseForge uploads are separate actions. See [port status](PORTING.md).
