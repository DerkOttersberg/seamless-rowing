"""Write the preview report from exported final-candidate results, never historical passes."""
from pathlib import Path
import json
repo=Path(__file__).resolve().parents[1]
out=repo/'qa-artifacts/preview-3-26.3-fabric'
evidence=json.loads((out/'acceptance.json').read_text());runs=evidence['nativeResults']
matrix=runs['s2']['rowing-matrix.json']
anatomy=[r['rowing-anatomy.json'] for r in runs.values()]
surface=[r['rowing-surface.json'] for r in runs.values()]
units=sum(int(t['tests']) for t in evidence['unitSuites'])
report=f'''# Continuous elbows and spine: 26.3 Fabric preview.3

Date: **2026-10-11**. Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.3**, Java **25**. This is an animation preview for user review before the remaining ports; production gates remain open.

```
{evidence['candidate']}
SHA-256 {evidence['sha256']}
```

## Animation changes

The previous preview used two independently capped arm blocks and a small rigid torso tilt. This preview renders a single continuous skin envelope across each elbow, with shared half-pixel rings, original UVs and no internal end caps. A rounded joint and gentle inner-fold taper preserve a continuous silhouette while its two bones remain six pixels long. Skin, sleeves and supported vanilla armor use the same geometry; native trim/glint rendering is retained. First person omits the proximal shoulder near the camera and keeps the world-projected wrist on the handle.

The original torso clip now drives a curved lumbar-to-chest spine. Its pitch keys range from −0.20 to +0.22 radians; bend grows from 55% at the lumbar spine to 145% at the chest. The hip center stays seated, the head and shoulder roots follow the chest, and turning adds a small shoulder twist. Extended reaches add at most 1.5 pixels of bounded scapular protraction to avoid locked elbows without stretching bones. The spine uses the paddle track's six-tick startup and four-tick recovery, retaining its current pose when vanilla resets the stopped phase. Attacks and item use yield torso movement.

FirstPersonModel's intentionally distant hidden head is preserved instead of being fed through the spine curve. The native body part/renderer is retained; original body geometry is restored before ordinary poses. Unsupported torso topology disables the rowing visual rather than moving the head away from a custom body. Immutable render snapshots own rotations and spine parameters; deferred rendering only reads them. Only visible surfaces deform for the current perspective.

## Final-hash checks

| Gate | Recorded result |
|---|---|
| Clean build and archive validation | **{units} unit tests** passed, with common isolation, authoring/export and standalone client JAR validation. No QA classes or SeamlessLib packaged. |
| Regressions | Elbow direction/hinge continuity, fixed bone lengths, mirrored/unreachable targets, tangent-continuous skin boundaries, fixed wrists, bounded shoulder reach, anchored hips/shoulder width, periodic spine motion, stop/restart continuity, eligibility, render-read purity, settings persistence and validated clip reloads. |
| Native boat/equipment matrix, s2 | **{matrix['plannedCases']}/{matrix['plannedCases']} cases**, **{sum(matrix['sampleCounts'].values())}** native hand/handle comparisons, maximum **{matrix['maximumErrorPixels']:.8f} pixels**, all eight phase bins; **{matrix['duplicateArmSubmissions']}** duplicate submissions and **{matrix['nativePaddleSounds']}** native paddle sound events. |
| Geometry coverage | Twenty vanilla boats/chest boats/rafts. Four representative geometries additionally cover classic/slim skins, both handedness settings, all three player camera views and enchanted diamond chest armor with gold Spire trim. Recorded FOVs are 30, 70 and 110; this is not the complete cross-product. |
| Actual textured surfaces | **{sum(s['submittedSkinSamples'] for s in surface)}** submitted skin inspections, **{sum(s['brokenSharedRings'] for s in surface)}** broken shared rings, **{sum(s['invalidFaces'] for s in surface)}** invalid faces; maximum surface-wrist/bone difference **{max(s['maximumSkinWristToBonePixels'] for s in surface):.8f} pixels**. |
| Spine attachment and recovery | **{sum(s['nativeBodySamples'] for s in surface)}** native skin torso inspections; maximum neck/torso separation **{max(s['maximumNeckToTorsoPixels'] for s in surface):.8f} pixels**. **{sum(s['restoredVanillaBodySamples'] for s in surface)}** ordinary-pose inspections, **{sum(s['failedBodyRestores'] for s in surface)}** failed body restores. Expanded equipment geometry is identified from its original native bounds and excluded from this skin-neck comparison. |
| Elbow anatomy | **{sum(a['samples'] for a in anatomy)}** solver and **{sum(a['submittedSamples'] for a in anatomy)}** submitted native joint checks; zero reversed, sideways or inverted hinges. Maximum bone-length error **{max(a['maximumLimbLengthErrorPixels'] for a in anatomy):.8f} pixels**. |
| Three-view preview, s1 | First person and both third-person views passed. Native contact maximum **{runs['s1']['rowing-contact.json']['nativeMaximumErrorPixels']:.8f} pixels**. |
| Reload/stop recovery, s3 | **{runs['s3']['rowing-lifecycle.json']['assertions']}** assertions passed for clear/recovery, resource reload/recovery and stopped-state cleanup. Damage wobble captured. |
| Vanilla server and two clients, pair14 | Driver and observer passed contact/ownership, anatomy and surface gates. Not Enough Animations **1.12.5**, FirstPersonModel **2.7.3** and PAL **1.2.8+mc.26.3** were installed. Native inputs exercise turning, stop/start, backwards-only movement, attacks, bow use and a higher-priority PAL action. Per-profile actual hashes, counters and reports are retained. |

Evidence is retained in `qa-artifacts/preview-3-26.3-fabric`: final-candidate reports, before/after JAR hashes, logs, unit XML, native stills, review sheets and four continuous videos. The first-person and cropped third-person GIFs are 800 pixels wide, under 2 MB and contain 32 captures at 20 client ticks per second. Full videos are 1280×720; decoded counts are verified against their source sequences. The isolated audio driver is silent; native sound-event routing is measured separately.

## Review and limits

The intentionally relocated-head predicate is covered by a unit regression. The integration run measured hidden parts and visible neck attachment; its direct relocated-head counter was zero, so it is not a measurement of every FirstPersonModel hiding path.

Inspect the continuous first-person, third-person-back, third-person-front and observer footage for the rounded elbow silhouette, torso stroke, seated hips, neck attachment, shoulder contact, textures, starts/stops and interruption recovery. A preliminary stronger-lean candidate failed the one-pixel contact gate on a raft reach (1.112513 pixels). Its reports and logs remain in diagnostics; bounded shoulder motion corrects that reach in the final candidate. Stop-phase continuity was also corrected before these fresh passes.

Tests used private background WSL/Xvfb displays, two CPU cores and reduced priority. Minecraft used software Vulkan/llvmpipe, Mesa 25.2.8, LLVM 20.1.2; this is not a hardware or high-FPS benchmark. Offline test identities produce expected Realms diagnostics. The temporary environment is removed and WSL shut down after evidence export; cleanup verification is retained.

Hardware GPU, shaders, high FPS, the complete lifecycle/modpack matrix, truly unmodded observers, custom resource packs/models/armor and modded boats remain unverified. The settings/EMF/ETF/Skin Layers/Emotecraft and Seamless-suite runs from preview.1 are historical evidence, not passes for this new hash. The ten remaining version/loader builds wait for this animation review. GitHub publication and CurseForge uploads remain separate actions.
'''
(repo/'docs/MOTION_PREVIEW_ACCEPTANCE.md').write_text(report,encoding='utf-8')
print('Wrote measured final-hash report')
