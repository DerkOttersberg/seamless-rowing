# Elbow correction: 26.3 Fabric preview.2

Date: **2026-10-11**. Candidate: **0.1.0-preview.2+mc26.3**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.3**, Java **25**. This is the replacement animation preview; user visual acceptance and production release gates remain open.

```
seamless-rowing-0.1.0-preview.2+mc26.3-fabric.jar
SHA-256 d862f745ef14d700ea21d2d0c17193eb3613bf8217badfa19812e4df12019891
```

## Correction

The first preview's hand-contact checks did not establish correct elbow anatomy. Its forward/outward elbow pole and independent upper/forearm roll could make the joint appear to bend backwards. The new solver selects a posterior bend plane with modest lateral clearance, shares a hinge frame between the two segments and limits shaft-driven wrist pronation continuously. It preserves six-pixel limb lengths, hand targets, tick interpolation and native skin, sleeve and equipment textures.

A preliminary correction retained a downward pole bias. A stronger rearward-reach regression exposed a possible upside-down hinge orientation, so that bias was removed before delivery. The superseded candidate's reports, actual hashes and review sheet remain in diagnostics. Only the final hash above receives the passes below.

## Final-hash checks

| Gate | Recorded result |
|---|---|
| Clean build and archive checks | Passed; **12 unit tests**, loader isolation, animation authoring/export and standalone client JAR checks. No QA classes or SeamlessLib in the release JAR. |
| Full-stroke regressions | Both arms, front and rear reaches, correct hinge orientation, forward flexion, fixed lengths, continuous elbow/wrist paths and twist, collapsed/unreachable targets. |
| Native geometry matrix, f2 | **76/76 cases**, **2,928** submitted-hand/native-handle comparisons; maximum **0.467414 pixels**, all eight stroke bins, zero duplicate arm submissions, 456 native paddle sound submissions. |
| Model and equipment coverage | Twenty vanilla boat/chest-boat/raft types. Four representative geometries additionally cover classic/slim skins, both handedness settings, three camera views and enchanted/trimmed armor; FOV 30, 70 and 110 across recorded cases. This is not every cross-product permutation. |
| Rendered elbow anatomy | **3,359** actual submitted-mesh checks across f1, f2, f3 and both paired clients: zero reversed, sideways or inverted hinges. **5,201** solver checks including equipment: zero violations; maximum limb-length error 0.00000144 pixels. Checks cover established grips. |
| Three-view preview, f1 | First person and both third-person views passed; 32 native contact comparisons, maximum 0.00001067 pixels. |
| Reload/recovery, f3 | Five assertions passed: clear/recovery, resource reload/recovery and stopped-pose cleanup. Damage wobble captured; 338 native contact comparisons, maximum 0.00000793 pixels. |
| Two clients and a vanilla server, pair11 | Driver and observer passed. Native contact maxima: 0.00000875 / 0.00001706 pixels; 23 / 38 comparisons. Driver released for bow use (39 ticks), attacks (7), active PAL animation (15), stopping (24) and backwards-only movement (18). Zero duplicate submissions; 78 native paddle sound events per client. |
| Optional animation APIs | Paired clients included Not Enough Animations **1.12.5**, FirstPersonModel **2.7.3** and PAL **1.2.8+mc.26.3**. Actual per-profile JAR hashes and metadata are retained. |

Evidence: `qa-artifacts/preview-2-26.3-fabric`. It contains final-hash reports, before/after archive hashes, logs, unit XML, matrix/reload stills, review sheets and four continuous videos. The two short GIFs are 800 pixels wide and under 2 MB. Videos are 1280×720 at 20 captured client ticks per second; this is animation evidence rather than a frame-rate benchmark. Null audio keeps the videos silent; native sound routing is measured separately. Encoded frame counts are validated against the input sequences.

Review covered forward strokes, turning, starts/stops, camera movement, item interruptions and observer rendering. The new bends stay consistent instead of rolling the hinge backwards. Natural world projection can move hands outside the frustum at some phases, camera angles or narrow FOVs. The preview retains the existing conservative custom-rig and overlay fallbacks.

## Scope and remaining gates

Tests used temporary private WSL/Xvfb displays, two CPU cores and reduced priority. OpenGL was unavailable there; native Minecraft used software Vulkan/llvmpipe, Mesa **25.2.8**, LLVM **20.1.2**. Offline QA identities produce expected Realms authentication diagnostics. The isolated setup is removed and WSL shut down after evidence export; cleanup verification is retained separately.

Hardware GPU/shaders/high FPS, the complete lifecycle and modpack matrix, genuinely unmodded observers, custom resource packs/rigs/armor and modded boats remain unverified. Settings UI, Skin Layers, EMF/ETF, Emotecraft and the combined Seamless suite were exercised on preview.1; those historical results are not new final-hash passes. Production release gates remain open. The other ten builds wait for feedback on this corrected preview. GitHub publication and CurseForge uploads remain separate actions.
