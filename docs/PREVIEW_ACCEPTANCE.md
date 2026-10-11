# 26.3 Fabric preview acceptance

**Historical preview.1 evidence.** User visual review found backwards elbow bending despite the passing hand-contact checks. Its animation is superseded by preview.2; these results do not establish acceptance of the new JAR. See [the correction report](ELBOW_PREVIEW_ACCEPTANCE.md).

Date: **2026-10-11**. Scope: **0.1.0-preview.1+mc26.3**, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25. This is the requested animation feedback checkpoint. **Production release gates and the other ten builds remain open.**

Release JAR: `seamless-rowing-0.1.0-preview.1+mc26.3-fabric.jar`

```
SHA-256 f49494a27cbd9701b6b031844698dee224ebffd3f8e12240152147c3305dd855
```

## Implemented behavior

The controlling rider follows the actual native paddle transforms, including the boat's seat-relative position, rotation and damage/bubble transform. Six-pixel upper arms and six-pixel forearms use fixed-length IK with a stable elbow pole and shaft-aligned wrist orientation. The original Blockbench torso curve is retimed to native paddle phase. Classic/slim skin, sleeve and supported vanilla armor UVs remain native; equipment rendering retains trims and glint. First-person arms use the world projection, with proximal shoulder geometry masked near the camera.

Tick snapshots control shared paddle/hand start and stop easing. Deferred rendering consumes captured poses and visibility. Item use releases both arms, attacks release the active arm, and PAL animation ownership yields to the external animation. Backwards-only movement preserves vanilla behavior. The mod changes neither boat physics nor network traffic; selected items are stowed only in render state.

Client settings have five visual toggles, scope/status text, Reset/Cancel/Save, validated atomic saves and backups. The native settings key starts unbound. Fabric Mod Menu is optional. There is no SeamlessLib or required runtime animation framework. See [installation instructions](PREVIEW_INSTALL.md) and [port gates](PORT_PLAN.md).

## Final candidate evidence

Native evidence is exported to `qa-artifacts/preview-26.3-fabric`, including actual JAR hashes, raw reports, logs, unit XML, 76 matrix stills, settings/fallback/wobble stills, continuous footage and review sheets. Large evidence and install bundles are ignored by Git; the source and this report are committed. The QA JAR is separate and excluded from the install bundle.

| Gate | Observed result |
|---|---|
| Clean build and archive audit | Passed; 10 unit tests, common loader isolation, authoring/export check and standalone/client-only JAR metadata. No QA classes or SeamlessLib bundled. |
| Native geometry matrix, r20 | **76/76 cases**, 3,060 submitted-hand/native-handle comparisons, worst **0.467669 model pixels**, samples in all eight stroke bins, zero duplicate rowing-arm submissions. |
| Boat, skin and equipment coverage | All 20 tested vanilla boat/chest-boat/raft types. Four representative geometries cover classic/slim, both handedness settings and all three camera views with an enchanted diamond chestplate and gold Spire trim. Additional first-person boat/raft cases at FOV 30; other cases include FOV 70 and 110. This is not every cross-product permutation. |
| Optional API combination, r19 | NEA/FirstPersonModel/PAL/EMF/ETF/Emotecraft installed; all three views produced native contact samples, worst 0.00001573 pixels. |
| Skin Layers fallback, r18 | First-person overlay preserved; 69 rigid third-person ticks. Rigid mode is a conservative visual fallback, not the articulated contact guarantee. |
| Reload and cleanup, r21 | Five native assertions: clear/recovery, actual resource reload/recovery and stopped-pose cleanup. Damage wobble sampled; 604 contact comparisons, worst 0.00000810 pixels. |
| Settings, r22 | Nine native UI assertions at GUI scales 1, 2 and 4; Cancel, Save, Reset, backup and disk reload passed. Screens inspected for legibility. |
| Combined Seamless suite, r23 | Packaged coexistence run passed in all three views; 34 contact samples, worst 0.00001211 pixels. This does not validate every sibling mod feature. |
| Two-client vanilla server, pair8 | Driver and observer passed. Driver recorded 39 item-use release ticks, seven attack-release ticks, 15 active-PAL release ticks, 24 stopped ticks and 18 backwards-only ticks. Contact maxima: driver 0.00001028 pixels; observer 0.00002855 pixels. No duplicate rowing submissions. |
| Final reviewed capture, pair9 | Fresh native run with camera warm-up and closer observer framing; same release JAR. Both clients passed, with the same driver ownership assertions, 23/38 native contact samples and maxima of 0.00001079/0.00001266 pixels. No duplicate rowing submissions. |
| Native sound routing | Matrix recorded 456 native paddle sound submissions. Pair8 driver/observer recorded 88/90 respectively. The probe accepts non-relative entity/world sounds; the mod adds no sound replacement. |

The contact probe compares **submitted mesh hand tips** with the **native boat model's rendered handle transform in the same frame**, during established grips. It is independent of the solver's own target computation. Reach clamping keeps bone lengths fixed even for the chest-boat extremes. Unit coverage includes eligibility, mirrored IK, unreachable targets, interpolation/read purity, periodic torso curves, malformed clips and config preservation. The QA-only warm-up changed its harness hash for pair9; each run retains its actual QA and release hashes. Final footage comes from pair9, with 82 native paddle sound submissions per client.

## Compatibility actually exercised

All versions below are **26.3 Fabric** builds. Their exact files and SHA-256 values are in `tested-mods.json`.

| Integration | Exact build | Preview result and limit |
|---|---|---|
| Not Enough Animations | 1.12.5 | Registered API animation at priority 1501; native paired run with item-action priority. |
| FirstPersonModel | 2.7.3 | Activation API coordinated the rowing view; visually reviewed local arms and relinquishment. Its own rendering resumes on interruptions. |
| Player Animation Library | 1.2.8+mc.26.3 | Actual QA-only animation layer caused rowing to yield. No animation library is required when absent. |
| Emotecraft | 3.5.0-a.build.171 | Installed coexistence passed. Full authored Emotecraft clip coverage remains open; PAL ownership was exercised separately. |
| Entity Model Features / Entity Texture Features | 3.3.11 / 7.2.5 | Default native rigs passed. Custom resource-pack model/animation flags use the preserve-existing-rendering adapter; custom-pack visual acceptance remains open. |
| 3D Skin Layers | 1.11.3 | Native first-person preservation and rigid third-person fallback passed. |
| Mod Menu | 21.0.0 | Optional entrypoint present; the settings screen passed native UI checks with this build installed. |
| Sodium / Iris | Iris 1.11.7+mc26.3 with its required Sodium 0.9.2 | **Unverified.** A preceding-candidate attempt failed to obtain an OpenGL context in the private display, then Iris initialization aborted. Diagnostic log and that attempt's hashes are retained. Neither Sodium nor shaders receive a compatibility pass for the final JAR. |
| Embeddium / Oculus / older PlayerAnimator / combat systems | No matched test for this preview | Deferred to the relevant ports/hardware matrix. Recognized unsupported custom player rigs conservatively keep their existing rendering. |

The suite run included Sword Throw 2.1.1+mc26.3, Seamless Dogs 0.2.0+mc26.3, Seamless Crafting 2.1.1+mc26.3, Seamless Deconstructing Workbench 2.1.1+mc26.3, Seamless API 2.0.2+mc26.3 and Pretty Meteors 2.0.2+mc26.3. Seamless API was needed by sibling mods in that test, not by Rowing. Custom boat resource-pack geometry has not been tested; do not treat native renderer identity alone as verification of such a pack's geometry.

## Visual review and remaining gates

Inspect the first-person, third-person-back, third-person-front and observer MP4s. The short GIFs are 800 pixels wide and below 2 MB; the third-person GIF is cropped around the subject. Full footage preserves the frame. Captures are encoded at 20 client ticks per second. The private display uses a null audio driver, so videos are silent; sound-event evidence is recorded separately.

Review sheets sample multiple stroke phases, turning, item interruptions and recovery. Articulated elbows remain outward and connected in the inspected native skin/armor poses, and the seated legs retain vanilla placement. The first-person world projection means narrow FOVs and looking away can move the hands outside the frustum. Rigid fallback/contact style and first-person shoulder masking are explicit feedback points, not claims of universal custom-model support.

Tests used isolated background WSL/Xvfb surfaces, two CPU cores and reduced priority. Minecraft fell back from an unavailable OpenGL visual to software Vulkan/llvmpipe. Offline test identities produce expected auth/Realms warnings. This proves the recorded packaged cases; it does not establish hardware GPU, shader, high-frame-rate or online-mode coverage.

After export and hash validation, the owned `SeamlessRowingQA-20261011` distro was unregistered. Its Windows staging folder was moved to the Recycle Bin, and WSL was shut down. The pre-existing Ubuntu and Docker distros remained stopped. No user game, world or pre-existing fixture cache was changed by cleanup. The retained `cleanup.json` records verification.

Before production: obtain user animation feedback; complete the other ten loader/version builds; validate hardware renderers/shaders and the full low/high-FPS camera matrix; exercise passengers, land/ice, real bubble columns, rapid mounts/dismounts, death/dimension/disconnect/unload, late tracking, reconnect/server restart, a genuinely unmodded observer, custom resource packs/rigs/armor, verified modded boats and supported combat/emote combinations. The current reload/clear test is not a substitute for all lifecycle gates. GitHub publication and CurseForge uploads remain separate actions.
