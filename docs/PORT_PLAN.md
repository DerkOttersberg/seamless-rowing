# Preview checkpoint and port plan

The approved checkpoint is 26.3 Fabric. Review its packaged footage and in-game feel before copying animation behavior into the ten remaining loader builds. No branch or loader placeholder is a release.

## Implemented layout

`common` owns immutable paddle/pose data, controller eligibility, tick transitions, IK, settings, native geometry adapters and version-specific narrow rendering hooks. `fabric` supplies lifecycle, resource reload, settings entrypoints and explicit optional API adapters. `qa` is a separate native-client harness and is excluded from the release JAR.

The original Blockbench project lives in `animation-authoring`. Its torso keys are checked against the runtime export. Limb endpoints are derived from native rendered handles; each six-pixel arm segment keeps its length. No external animation asset, player skin, SeamlessLib or animation runtime is bundled.

The existing Seamless four-branch pattern is retained. Toolchain pins are isolated in the version catalog rather than scattered through loader code. The initial 26.3 pins follow the corresponding Seamless branch: Architectury plugin 3.5.169, Loom no-remap 1.17.493, Gradle 9.6.0, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3. Forge 66.0.9 and NeoForge 26.3.0.48-beta are planned pins, not tested builds.

## Work after animation feedback

| Version branch | Remaining loaders | Required adaptation |
|---|---|---|
| 26.3 | Forge, NeoForge | Explicit client lifecycle, keys, reload/menu entrypoints and optional APIs; verify deferred render-state capture. |
| 26.2 | Fabric, Forge, NeoForge | Use that branch's pins and native render APIs; verify paddle matrices and captured draw state independently. |
| 1.21.1 | Fabric, Forge, NeoForge | Java 21; adapt older player/boat/armor and first-person rendering; retain native texture, trim and glint behavior. |
| 1.20.1 | Fabric, Forge | Java 17; mapped names/toolchain and earlier rendering APIs; only matching optional integration versions. |

Use one repository with branches `26.3`, `26.2`, `1.21.1` and `1.20.1`. Keep deterministic math and eligibility behavior equivalent while adapting Minecraft-facing types. Do not advertise a modded boat unless its geometry adapter has been measured in a packaged client.

## Release gates

Each final JAR needs its own hash-bound report, clean build, packaged singleplayer and two-client vanilla-server tests. Include installations with and without the client mod, late tracking, reconnect/restart, dimension/death/unload cleanup, passengers, damage, bubble columns, land/ice and rapid mounting. Expand camera/FOV and low/high frame-rate checks on a hardware GPU. Use the native submitted-mesh/native-paddle probe rather than only evaluating the IK formula.

Test exact matching Sodium/Embeddium, Iris/Oculus, NEA, FirstPersonModel, 3D Skin Layers, EMF/ETF and emote/combat builds. Exercise custom resource-pack rigs and textures, overlay fallbacks, actual emote clips and direct-item ownership. Record unavailable builds and unverified combinations explicitly. Run the available Seamless suite as a coexistence test, without treating that as validation of every sibling feature.

The isolated software renderer cannot establish Iris/shader compatibility. Its OpenGL context failure must be resolved with a suitable test display/GPU before a production claim. GitHub publication and CurseForge uploads remain separate actions.
