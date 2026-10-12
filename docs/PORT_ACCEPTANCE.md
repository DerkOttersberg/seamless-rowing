# Seamless Rowing 0.1.0 packaged acceptance

Generated 2026-10-12T19:07:31.739517+00:00. All eleven builds passed the required current-byte native acceptance gates. Manual visual review and the bundle manifest retain the evidence and source commits. Historical preview or different-JAR results do not clear these cells.

| Minecraft | Loader | Java | SHA-256 | Passing native gates |
|---|---|---:|---|---|
| 26.3 | fabric | 25 | `53435c45bc873fb2f5f4f0360b9c2cbfbe0f2ea189a98ef93613570920190c77` | boat-fallback, edge, fallback, fresh, lifecycle, matrix, mixed, pair, reconnect, sdk-pair, settings, suite |
| 26.3 | forge | 25 | `9f6592d39fda9f6ff05c8d31abc6088eb206b48ec8e2300c5ce78249b8277ac1` | lifecycle, matrix, mixed, pair, settings, suite |
| 26.3 | neoforge | 25 | `c045ba66ce5eec9ec86468cf52c4c493e15d3f7478d753dd61d9b2479433f685` | fresh, lifecycle, matrix, mixed, pair, settings, suite |
| 26.2 | fabric | 25 | `2d93227be38235045acc5a2217dde06faa7c82aa97bdfbafc9e4655892ff5544` | fresh, lifecycle, matrix, mixed, pair, settings, suite |
| 26.2 | forge | 25 | `25018f0b301413c5d8aee0b72635e9a89b0959c2db637b15d423a438b1a64b67` | lifecycle, matrix, mixed, pair, settings, suite |
| 26.2 | neoforge | 25 | `ecaa15b8df7f4da8c958b27ac9aab7c170098bb0661c1e6f546afee49e95a60b` | boat-fallback, edge, fallback, fresh, lifecycle, matrix, mixed, pair, reconnect, sdk-pair, settings, suite |
| 1.21.1 | fabric | 21 | `51067f37d8d9f624a7ba68b768cc2c606567dc7f2d465a5b76095345a130cbe6` | boat-fallback, edge, fallback, fresh, lifecycle, matrix, mixed, pair, reconnect, sdk-pair, settings, suite |
| 1.21.1 | forge | 21 | `bc2b455a19061e47475cfb40e2ef9fa982f0267d1cba94eec1bdb94bd8c45e37` | fresh, lifecycle, matrix, mixed, pair, settings, suite |
| 1.21.1 | neoforge | 21 | `b6409ee12263f51d63842ea7b1e4798905ecfe24365c33d737df1ccd570a2692` | fresh, lifecycle, matrix, mixed, pair, settings, suite |
| 1.20.1 | fabric | 17 | `43dcc42ab5cf346cfff69034704ecc6ec69c0981449be556c38a46946833f80d` | fresh, lifecycle, matrix, mixed, pair, settings, suite |
| 1.20.1 | forge | 17 | `09be781ee2d9b50aa2121da4c7e26436f039631ec74718bc238524996f7b6687` | boat-fallback, edge, fallback, fresh, lifecycle, matrix, mixed, pair, reconnect, sdk-pair, settings, suite |

The mandatory core gates are the complete boat/skin/handedness/armor/FOV matrix, resource and pose recovery, native settings at GUI scales 1/2/4, the available Seamless suite, a two-client dedicated-server run, and a mixed client installation for each of the eleven cells. Representative branches additionally require save/restart/reconnect, active animation-library handoff, shader rendering, Fresh skin/armor/camera matrices, fallback rendering and native terrain/death/dimension/disconnect transitions.

The older branches additionally require a native armor visibility regression. Their adapter accepts Minecraft’s standard HumanoidArmorModel subclass and submits its current surfaces directly. The first-person armor pass draws only arm surfaces; head, chest and leg surfaces must have zero submissions. This also preserves the native trim and glint passes. The previously bypassed armor-model subclass and its incorrect first-person chestplate are superseded by the final hashes below.

Boat transforms follow each native renderer generation. Minecraft 1.20.1/1.21.1 apply bubble wobble while submerged; 26.2/26.3 suppress it. The older adapters use that older rule, with actual submerged-bubble hand samples required before acceptance. The failing earlier 1.20.1 run measured 2.34 model pixels; the corrected regression measures below 0.00003 model pixels without altering vanilla physics.

Yaw interpolation also follows the native renderer generation. The older LevelRenderer passes linearly interpolated boat yaw; modern render state uses wrapped rotation interpolation. The older adapters now match their native yaw, including the 179 to -179 degree boundary. Each older loader requires real boundary-render frames with less than 0.001 degree difference. The full two-client animation stack additionally measures remote observer hand contact; the earlier 1.64-pixel observer mismatch is fixed in the final builds.

The final shared elbow solver rotates its bend pole with the shoulder-to-handle direction. Native bubble banking exposed a fixed-pole inversion when a grip moved above shoulder height; the revised solver keeps the anatomical branch through that crossing. A direct hinge quaternion also removes sideways cancellation noise near straight reaches. A new mirrored continuity/contact regression and the independent native bubble anatomy probe both gate these final builds.

The native matrix has 76 cases on 26.x and 74 on the older branches. It covers every vanilla boat and chest boat material, including the bamboo rafts. Oak boats, oak chest boats, bamboo rafts and bamboo chest rafts receive the cross-product of classic/slim skins, left/right main hand and all three camera views with enchanted, trimmed armor. First-person FOV 30 and 110 and third-person FOV 30/70 are exercised. Exact case labels and per-case sample counts are retained in the native JSON.

All builds are client-only. Fabric needs the matching Fabric API. No SeamlessLib or runtime animation framework is required. The four simple version branches contain their supported loader modules; 1.20.1 has Fabric and Forge.

Optional inputs are locked by SHA-256 in each native result and its input manifest. Fresh Player Extension 1.1 is recognized by its two original model hashes with EMF 3.3.11 and ETF 7.2.5. Fresh resumes outside rowing; rowing uses its verified vanilla geometry and retains the skin texture. FirstPersonModel 2.7.3, Not Enough Animations 1.12.5/1.12.6, Punchy 2.8e, matching Real Camera, Hold My Items and Eating Animation builds are exercised where available. The 3D Skin Layers 1.11.3 fallback preserves its first-person rendering and uses rigid third-person arms. Unknown API versions, custom player rigs and unverified boat geometry preserve existing rendering.

Private background displays use Mesa software rendering on Ubuntu, bounded heaps, low priority and two separate CPU-limited slots. This is genuine packaged client/server testing; it does not certify every GPU driver, resource pack, custom armor or arbitrary modpack. Shader evidence uses the matching Iris/Oculus build and Complementary Reimagined r5.9.3. Sound checks count native paddle events; the background capture uses a null audio device.

FirstPersonModel temporarily yields its first-person body overlay while Rowing owns the arms. Its overlay resumes when rowing stops or an item action takes ownership. Punchy yields its competing boat-hand animation, and Hold My Items receives a complete handoff during grouped attacks. These are deliberate rendering handoffs rather than a promise that multiple mods simultaneously pose the same arms.

Better Combat on modern NeoForge is tested with its required server-side installation as well as the client. The server has no Rowing installation. Installed gameplay add-ons may have their own server requirements. Other protection, combat or custom-model systems are not implicitly certified by these combinations.

Mixed-installation tests include a viewer without Rowing. During the final run, a bare NeoForge 21.1.255 observer without Rowing or QA hit NoSuchElementException in its VanillaConnectionNetworkFilter while handling the vanilla brand packet. Its logs are preserved. The same Rowing-enabled client passed with an official vanilla observer; subsequent mixed tests use the original Mojang client and an empty mods directory. This is a recorded loader/networking limitation, not a Rowing networking patch.

Failed attempts remain in the evidence history. The final install bundle includes only gameplay JARs, licensing, installation guidance, final hashes and acceptance evidence. QA mods, compile-only stubs, test worlds and third-party packs are excluded. GitHub publication and CurseForge uploads remain separate actions.

## Toolchain and runtime pins

| Branch | Fabric Loader / API | Forge | NeoForge |
|---|---|---|---|
| 26.3 | 0.19.5 / 0.161.0+26.3 | 26.3-66.0.9 | 26.3.0.48-beta |
| 26.2 | 0.19.5 / 0.159.0+26.2 | 26.2-65.1.3 | 26.2.0.75 |
| 1.21.1 | 0.19.5 / 0.116.17+1.21.1 | 1.21.1-52.1.16 | 21.1.255 |
| 1.20.1 | 0.19.5 / 0.92.12+1.20.1 | 1.20.1-47.4.26 | Not applicable |

26.2 NeoForge also uses runtime 26.2.0.87 for the matching modern Emotecraft/Better Combat combination, whose dependencies require that loader update. The base Rowing build pin remains 26.2.0.75. Modern Forge has no matching builds of several graphics/player-model add-ons in the resolved catalog; its vanilla rendering is tested without claiming those unavailable combinations.

## Measured frame cadence

The 26.3 Fabric timing fixtures run at 320 × 240 on Mesa software rendering, with Sodium and no screenshot readbacks. They require actual low- and high-frame-rate measurements; merely setting a cap is insufficient.

| Run | Requested cap | Average FPS | Maximum measured window FPS | Native hand contact (pixels) |
|---|---:|---:|---:|---:|
| 263-fabric-cadence-low-av | 10 | 9.31 | 9.69 | 0.000014 |
| 263-fabric-cadence-high-av | 144 | 56.91 | 91.52 | 0.001182 |

These are measured software-renderer rates, not a certification of 144 FPS on every GPU. The same tick/interpolation logic is shared across loaders, with independent regression tests for repeated renders between ticks.

## Exact optional versions observed in passing runs

F = Fabric, G = Forge, N = NeoForge. Entries below require a passing run with both the current Rowing hash and the exact add-on filename. A dash means no passing matching installation was recorded; it is not a claim of incompatibility. Per-run combinations and all JAR hashes are retained in the evidence manifests.

| Add-on | 26.3 | 26.2 | 1.21.1 | 1.20.1 |
|---|---|---|---|---|
| [EMF] Entity Model Features | `3.3.11-fabric-26.3` (F)<br>`3.3.11-neoforge-26.3` (N) | `3.3.11-fabric-26.2` (F)<br>`3.3.11-neoforge-26.2` (N) | `3.3.11-fabric-1.21` (F)<br>`3.3.11-forge-1.21` (G)<br>`3.3.11-neoforge-1.21` (N) | `3.3.11-fabric-1.20.1` (F)<br>`3.3.11-forge-1.20.1` (G) |
| [ETF] Entity Texture Features | `7.2.5-fabric-26.3` (F)<br>`7.2.5-neoforge-26.3` (N) | `7.2.5-fabric-26.2` (F)<br>`7.2.5-neoforge-26.2` (N) | `7.2.5-fabric-1.21` (F)<br>`7.2.5-forge-1.21` (G)<br>`7.2.5-neoforge-1.21` (N) | `7.2.5-fabric-1.20.1` (F)<br>`7.2.5-forge-1.20.1` (G) |
| First-person Model | `2.7.3` (F/N) | `2.7.3` (F/N) | `2.7.3` (F/G/N) | `2.7.3` (F/G) |
| Not Enough Animations | `1.12.6` (F/N) | `1.12.5` (F/N) | `1.12.6` (F/G/N) | `1.12.6` (F/G) |
| Punchy! | `2.8e` (F/N) | `2.8e` (F/N) | `2.8e` (F/G/N) | `2.8e` (F/G) |
| Real Camera | `0.7.8-beta-26.3` (F/N) | `0.7.8-beta-26.2` (F/N) | `0.7.8-beta-1.21.1` (F/N) | `0.7.8-beta-1.20.1` (G) |
| 3D Skin Layers | `1.11.3` (F) | `1.11.3` (N) | `1.11.3` (F) | `1.11.3` (G) |
| Sodium | — | `mc26.2-0.9.2-neoforge` (N) | — | — |
| Iris Shaders | `1.11.7+26.3-fabric` (F) | `1.11.4+26.2-neoforge` (N) | `1.8.14-beta.1+1.21.1-fabric` (F) | `1.7.6+1.20.1` (F) |
| Embeddium | — | — | — | `0.3.31+mc1.20.1` (G) |
| Oculus | — | — | — | `1.20.1-1.8.0` (G) |
| Hold My Items | — | — | `4.3` (F) | `4.2.5H1` (F) |
| Eating Animation | — | — | `1.9.72` (F) | `1.9.61` (F) |
| Player Animation Library | `1.2.8` (F) | `1.2.8` (N) | `1.1.6` (F/N) | — |
| playerAnimator | — | — | `2.0.4+1.21.1-fabric` (F)<br>`2.0.4+1.21.1-forge` (N) | `1.0.2-rc1+1.20-fabric` (F)<br>`1.0.2-rc1+1.20-forge` (G) |
| Emotecraft | `3.5.0-a.build.171` (F) | `3.4.0-b.build.170` (N) | `2.4.12+1.21.1-fabric` (F) | `2.2.7-b.build.50+1.20.1-fabric` (F)<br>`2.2.7-b.build.50+1.20.1-forge` (G) |
| Better Combat | `3.2.2+26.3-fabric` (F) | `3.2.2+26.2-neoforge` (N) | `2.4.0+1.21.1-fabric` (F) | `1.9.0+1.20.1-fabric` (F)<br>`1.9.0+1.20.1-forge` (G) |

The native shader fixture holds an ordinary rowing or stopped phase when shader compilation leaves too few drawn frames. It waits for actual Fresh model observations without injecting samples or changing the release animation. The bubble fixture keeps air above the water surface, avoiding vanilla underwater ejection before the measured bubble cycle. Earlier fixture failures remain in the preserved history.

Visual clips are assembled from native frame captures, timed by client ticks. The software renderer may repeat frames while compiling shaders. Original key frames, contact sheets and quantitative probes accompany the clips; the videos contain no captured audio.
