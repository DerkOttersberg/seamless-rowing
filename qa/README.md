# Native preview harness

This module is a **separate QA mod**, not part of Seamless Rowing's distributable. Its initializer refuses display `:0`, missing private displays and Wayland desktop surfaces. It prepares an owned fixture copy, drives Minecraft's native input mappings and records actual submitted render transforms. The independent handle measurement comes from the vanilla boat model after its native `setupAnim`, not the rowing solver's own target formula.

The preview runner uses a temporary WSL distro, a private Xvfb display per client, two CPU cores, lower process priority, finite timeouts and a vanilla loopback server. It never drives the user's desktop Minecraft window. Offline QA identities use a placeholder token; their expected Realms/authentication warnings do not represent normal authenticated clients or online-mode acceptance tests.

## Scenarios

| Scenario | Native checks |
|---|---|
| default | Forward/turning/start/stop captures in first person and both third-person views; submitted hand contact. |
| matrix | 76 boat/raft, skin, handedness, camera, FOV and enchanted/trimmed armor cases; phase bins, sounds and duplicate rowing submissions. |
| lifecycle | State clear/recovery, actual resource reload, damage wobble and stopped-pose cleanup. |
| settings | Native buttons at GUI scales 1, 2 and 4; Cancel, Save, Reset and disk backup/reload. |
| fallback | 3D Skin Layers first-person preservation and rigid third-person state. |
| pair | Two independent packaged clients and a server without mods; actual bow use, attack input, active PAL animation, stopping/backwards behavior and remote rendering. |

`prepare_preview.py`, `prepare_pair.py` and the PowerShell final runner intentionally reference this machine's **owned QA fixture/cache paths**. They require an official 26.3 client/library launch manifest, Fabric dependencies, an owned fixture world and the temporary Java 25 runtime to be prepared first. They are not a portable Minecraft downloader, and must be adapted to another test host's owned paths. Never point them at a user's live instance/world.

`export_preview.py` validates passes and actual before/after JAR hashes, then copies reports, logs and representative stills into ignored `qa-artifacts`. `encode_preview.sh` encodes the native capture sequence at 20 client ticks per second; this is animation evidence, not a high-frame-rate benchmark. The isolated null audio driver keeps tests silent, while the sound probe records native, non-relative paddle sound submissions.

For the elbow correction, `export_elbow_preview.py` freezes separate preview.2 evidence. The anatomy probe checks forward flexion, absence of sideways hinge bending and fixed lengths from solver output, then independently checks the actual submitted model-part transforms after `setupAnim`. Contact measurements alone do not establish anatomically correct elbow direction. Both short GIFs and long captures must be reviewed through complete strokes.

The final runner contains a frozen candidate hash deliberately: rebuilds that change the release JAR require a fresh candidate, fresh profiles and new acceptance evidence. Failed test history is retained in diagnostics rather than presented as a pass. Export evidence before removing the temporary distro; do not package this QA JAR in install bundles.
