# Seamless Rowing

Client-only boat rowing animation with paddle-driven articulated arms, original fixed-seat torso poses and native skin/armor rendering. All Rights Reserved, with modpack inclusion permission in `LICENSE`.

## Accepted release 0.1.0

[Seamless Rowing on CurseForge](https://www.curseforge.com/minecraft/mc-mods/seamless-rowing/preview) was submitted with all 11 accepted builds on 13 October 2026. Every file selects automatic publication after approval. The new project and files are awaiting review; submission does not imply public availability. See the [upload receipt](docs/CURSEFORGE_RELEASE.md) for file IDs, hashes, GIFs and observed moderation states.

**0.1.0** retains the approved spine stroke and continuous textured elbows. The four version branches contain all 11 Fabric, Forge and NeoForge builds in the table below. All 11 builds passed the required packaged native gates against their final JAR hashes, including two-client dedicated-server and mixed-installation tests. Current results and limitations are recorded in the packaged acceptance report; historical previews remain separate. Fabric requires Fabric API; Forge and NeoForge use their native APIs. SeamlessLib and a runtime animation framework are not required. Install Rowing on clients. Each viewer needs it to see the animation; a server without Rowing uses ordinary vanilla boat synchronization.

Use normal boat controls. Only the controlling rider animates. Forward/turning strokes follow native paddles; backwards-only movement keeps vanilla behavior. Item use releases both hands, and attacking releases the active hand. Hold My Items receives a complete first-person handoff during attacks because its grouped animation controls both hands. Selected items are hidden visually while their hand rows; inventory and boat physics are unchanged. Sounds remain vanilla, including resource-pack replacements.

Bind **Open rowing settings** in Options → Controls → Key Binds → Seamless Rowing. It starts unbound. Optional Mod Menu exposes the same screen. The settings screen has enable, first person, third person, articulated elbows and torso toggles. Save writes a validated client JSON with an atomic replacement and previous-file backup; Cancel discards the draft.

Unrecognized replacement player/boat renderers and modded boat IDs retain their existing rendering. Custom boat resource geometry disables rowing rather than guessing paddle positions. The 3D Skin Layers fallback uses rigid third-person arms and preserves its first-person rendering. Only version-matched optional APIs are enabled; unsupported versions disable rowing and appear in compatibility status. Fresh Player Extension 1.1 uses its own models outside rowing and the verified vanilla rig during rowing. Other Fresh or custom-player packs require a verified adapter. See [port status](PORTING.md) and the historical [motion preview](docs/MOTION_PREVIEW_ACCEPTANCE.md), [elbow correction](docs/ELBOW_PREVIEW_ACCEPTANCE.md) and [first preview](docs/PREVIEW_ACCEPTANCE.md) reports.

FirstPersonModel temporarily yields its body overlay while Rowing owns first-person arms, then resumes when rowing releases control. Not Enough Animations uses rowing priority 1501 below direct item actions. Punchy yields its boat-hand animation during Rowing's first-person strokes. Exact tested add-on versions and native evidence are recorded in [packaged acceptance](docs/PORT_ACCEPTANCE.md).

## Build and architecture

Run `gradlew.bat clean check build` with Java 25 as the Gradle launcher and the branch's Java compiler toolchain installed. The compiled runtime targets are Java 17, 21 or 25 as shown below. Distributions are in each loader's `build/libs` directory. Install the JAR matching both Minecraft and loader. **Never install or distribute the separate QA JAR or optional API stubs as the mod.**

`common` owns eligibility, paddle geometry, interpolation, fixed-length IK, immutable pose snapshots, settings and narrow rendering hooks. Explicit Fabric, Forge and NeoForge services supply lifecycle, keys, resource reload and menus. Their shared `loader-adapters` source directory contains optional integrations and is excluded from `common`. Architectury/Loom are build tools only. There is no new networking protocol, server entrypoint or library shading. The 26.3 deferred renderer receives captured visibility and pose data rather than advancing animation during rendering.

Our mutable polygon surfaces submit their current vertices and original texture coordinates through native vertex consumers. Static cuboid caches cannot represent their deformation; only the owned surfaces bypass those caches. Ordinary models retain the graphics mod's rendering path, and shaders, lighting, armor, trims and glint retain their native consumers.

`qa` is a separate packaged-client fixture and independent native-render probe. It compares submitted hand tips with the native paddle's actual model matrix in the same frame and counts actual polygon vertex submissions. Unit tests cover eligibility, mirrored IK, unreachable targets, render-read purity, transitions, resource clips and settings preservation. See `animation-authoring` for original source poses.

## Version branches

One repository per product, with one branch per Minecraft line containing its loaders:

| Branch | Loader modules | Java |
|---|---|---:|
| 26.3 | Fabric, Forge, NeoForge | 25 |
| 26.2 | Fabric, Forge, NeoForge | 25 |
| 1.21.1 | Fabric, Forge, NeoForge | 21 |
| 1.20.1 | Fabric, Forge | 17 |

All four native ports passed packaged singleplayer, multiplayer, settings, recovery and applicable compatibility gates. The release bundle contains exact hashes, source commits and acceptance evidence. GitHub publication remains a separate action. CurseForge submission is recorded in the upload receipt above.
