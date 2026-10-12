"""Generate a dated, current-byte report without promoting pending native gates."""
from pathlib import Path
import argparse,datetime,json,subprocess,tomllib
BASE=Path(__file__).resolve().parents[2]
PRIMARY=BASE/'seamless-rowing'
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('--accepted',action='store_true');a=p.parse_args()
if a.accepted:
    subprocess.run(['python3',str(Path(__file__).with_name('package_ports.py')),str(a.stage),'--output',str(PRIMARY/'release-bundles/preflight-only'),'--check-only'],check=True)
subprocess.run(['python3',str(Path(__file__).with_name('audit_acceptance.py')),str(a.stage)],check=True)
audit=json.loads((PRIMARY/'qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
records=[]
for file in a.stage.glob('*results.json'):
    data=json.loads(file.read_text(encoding='utf-8-sig'))
    if isinstance(data,list):records.extend(r for r in data if r.get('pass') is True)
def accepted_for(cell):
    return [r for r in records if (r.get('hashes') or r.get('report',{}).get('hashes',{}).get('rower',{})).get(cell['filename'])==cell['sha256']]
lines=['# Seamless Rowing 0.1.0 packaged acceptance','',
    'Generated '+datetime.datetime.now(datetime.timezone.utc).isoformat()+('. All eleven builds passed the required current-byte native acceptance gates. Manual visual review and the bundle manifest retain the evidence and source commits.' if a.accepted else '. Release-candidate status until all required gates, visual review and packaging checks pass.')+' Historical preview or different-JAR results do not clear these cells.','',
    '| Minecraft | Loader | Java | SHA-256 | Passing native gates |','|---|---|---:|---|---|']
for c in audit['cells']:
    lines.append('| '+c['minecraft']+' | '+c['loader']+' | '+('25' if c['minecraft'].startswith('26.') else '17' if c['minecraft']=='1.20.1' else '21')+' | `'+c['sha256']+'` | '+(', '.join(sorted(c['gates'])) or 'Pending')+' |')
lines+=['','The mandatory core gates are the complete boat/skin/handedness/armor/FOV matrix, resource and pose recovery, native settings at GUI scales 1/2/4, the available Seamless suite, a two-client dedicated-server run, and a mixed client installation for each of the eleven cells. Representative branches additionally require save/restart/reconnect, active animation-library handoff, shader rendering, Fresh skin/armor/camera matrices, fallback rendering and native terrain/death/dimension/disconnect transitions.','',
    'The older branches additionally require a native armor visibility regression. Their adapter accepts Minecraft’s standard HumanoidArmorModel subclass and submits its current surfaces directly. The first-person armor pass draws only arm surfaces; head, chest and leg surfaces must have zero submissions. This also preserves the native trim and glint passes. The previously bypassed armor-model subclass and its incorrect first-person chestplate are superseded by the final hashes below.','',
    'Boat transforms follow each native renderer generation. Minecraft 1.20.1/1.21.1 apply bubble wobble while submerged; 26.2/26.3 suppress it. The older adapters use that older rule, with actual submerged-bubble hand samples required before acceptance. The failing earlier 1.20.1 run measured 2.34 model pixels; the corrected regression measures below 0.00003 model pixels without altering vanilla physics.','',
    'Yaw interpolation also follows the native renderer generation. The older LevelRenderer passes linearly interpolated boat yaw; modern render state uses wrapped rotation interpolation. The older adapters now match their native yaw, including the 179 to -179 degree boundary. Each older loader requires real boundary-render frames with less than 0.001 degree difference. The full two-client animation stack additionally measures remote observer hand contact; the earlier 1.64-pixel observer mismatch is fixed in the final builds.','',
    'The final shared elbow solver rotates its bend pole with the shoulder-to-handle direction. Native bubble banking exposed a fixed-pole inversion when a grip moved above shoulder height; the revised solver keeps the anatomical branch through that crossing. A direct hinge quaternion also removes sideways cancellation noise near straight reaches. A new mirrored continuity/contact regression and the independent native bubble anatomy probe both gate these final builds.','',
    'The native matrix has 76 cases on 26.x and 74 on the older branches. It covers every vanilla boat and chest boat material, including the bamboo rafts. Oak boats, oak chest boats, bamboo rafts and bamboo chest rafts receive the cross-product of classic/slim skins, left/right main hand and all three camera views with enchanted, trimmed armor. First-person FOV 30 and 110 and third-person FOV 30/70 are exercised. Exact case labels and per-case sample counts are retained in the native JSON.','',
    'All builds are client-only. Fabric needs the matching Fabric API. No SeamlessLib or runtime animation framework is required. The four simple version branches contain their supported loader modules; 1.20.1 has Fabric and Forge.','',
    'Optional inputs are locked by SHA-256 in each native result and its input manifest. Fresh Player Extension 1.1 is recognized by its two original model hashes with EMF 3.3.11 and ETF 7.2.5. Fresh resumes outside rowing; rowing uses its verified vanilla geometry and retains the skin texture. FirstPersonModel 2.7.3, Not Enough Animations 1.12.5/1.12.6, Punchy 2.8e, matching Real Camera, Hold My Items and Eating Animation builds are exercised where available. The 3D Skin Layers 1.11.3 fallback preserves its first-person rendering and uses rigid third-person arms. Unknown API versions, custom player rigs and unverified boat geometry preserve existing rendering.','',
    'Private background displays use Mesa software rendering on Ubuntu, bounded heaps, low priority and two separate CPU-limited slots. This is genuine packaged client/server testing; it does not certify every GPU driver, resource pack, custom armor or arbitrary modpack. Shader evidence uses the matching Iris/Oculus build and Complementary Reimagined r5.9.3. Sound checks count native paddle events; the background capture uses a null audio device.','',
    'FirstPersonModel temporarily yields its first-person body overlay while Rowing owns the arms. Its overlay resumes when rowing stops or an item action takes ownership. Punchy yields its competing boat-hand animation, and Hold My Items receives a complete handoff during grouped attacks. These are deliberate rendering handoffs rather than a promise that multiple mods simultaneously pose the same arms.','',
    'Better Combat on modern NeoForge is tested with its required server-side installation as well as the client. The server has no Rowing installation. Installed gameplay add-ons may have their own server requirements. Other protection, combat or custom-model systems are not implicitly certified by these combinations.','',
    'Mixed-installation tests include a viewer without Rowing. During the final run, a bare NeoForge 21.1.255 observer without Rowing or QA hit NoSuchElementException in its VanillaConnectionNetworkFilter while handling the vanilla brand packet. Its logs are preserved. The same Rowing-enabled client passed with an official vanilla observer; subsequent mixed tests use the original Mojang client and an empty mods directory. This is a recorded loader/networking limitation, not a Rowing networking patch.','',
    'Failed attempts remain in the evidence history. The final install bundle includes only gameplay JARs, licensing, installation guidance, final hashes and acceptance evidence. QA mods, compile-only stubs, test worlds and third-party packs are excluded. GitHub publication and CurseForge uploads remain separate actions.','']
lines+=['## Toolchain and runtime pins','','| Branch | Fabric Loader / API | Forge | NeoForge |','|---|---|---|---|']
for mc in ('26.3','26.2','1.21.1','1.20.1'):
    repo=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc))
    pins=tomllib.loads((repo/'gradle/libs.versions.toml').read_text())['versions']
    lines.append('| '+mc+' | '+pins['fabric-loader']+' / '+pins['fabric-api']+' | '+pins['forge']+' | '+(pins.get('neoforge','Unavailable') if mc!='1.20.1' else 'Not applicable')+' |')
lines+=['','26.2 NeoForge also uses runtime 26.2.0.87 for the matching modern Emotecraft/Better Combat combination, whose dependencies require that loader update. The base Rowing build pin remains 26.2.0.75. Modern Forge has no matching builds of several graphics/player-model add-ons in the resolved catalog; its vanilla rendering is tested without claiming those unavailable combinations.','',
    '## Measured frame cadence','','The 26.3 Fabric timing fixtures run at 320 × 240 on Mesa software rendering, with Sodium and no screenshot readbacks. They require actual low- and high-frame-rate measurements; merely setting a cap is insufficient.','',
    '| Run | Requested cap | Average FPS | Maximum measured window FPS | Native hand contact (pixels) |','|---|---:|---:|---:|---:|']
for name in ('263-fabric-cadence-low-av','263-fabric-cadence-high-av'):
    profile=a.stage/('client-'+name);cadence=profile/'rowing-frame-cadence.json';behavior=profile/'rowing-fresh-behavior.json'
    if cadence.is_file() and behavior.is_file():
        c=json.loads(cadence.read_text());b=json.loads(behavior.read_text())
        lines.append('| '+name+' | '+('10' if '-low-' in name else '144')+' | '+f"{c['averageFps']:.2f}"+' | '+f"{c.get('maximumMeasuredWindowFps',0):.2f}"+' | '+f"{b['maximumErrorPixels']:.6f}"+' |')
lines+=['','These are measured software-renderer rates, not a certification of 144 FPS on every GPU. The same tick/interpolation logic is shared across loaders, with independent regression tests for repeated renders between ticks.','']
lines+=['## Exact optional versions observed in passing runs','','F = Fabric, G = Forge, N = NeoForge. Entries below require a passing run with both the current Rowing hash and the exact add-on filename. A dash means no passing matching installation was recorded; it is not a claim of incompatibility. Per-run combinations and all JAR hashes are retained in the evidence manifests.','','| Add-on | 26.3 | 26.2 | 1.21.1 | 1.20.1 |','|---|---|---|---|---|']
resolved=json.loads((PRIMARY/'qa-artifacts/integration-matrix/resolved.json').read_text())
for key,project in resolved.items():
    values=[]
    for mc in ('26.3','26.2','1.21.1','1.20.1'):
        installed={}
        for cell in (c for c in audit['cells'] if c['minecraft']==mc):
            version=project['cells'].get(mc+'/'+cell['loader'])
            if not version:continue
            filename=version['file']['filename']
            if any(filename in (r.get('hashes') or r.get('report',{}).get('hashes',{}).get('rower',{})) for r in accepted_for(cell)):
                installed.setdefault(version['version'],[]).append({'fabric':'F','forge':'G','neoforge':'N'}[cell['loader']])
        values.append('<br>'.join('`'+version+'` ('+'/'.join(loaders)+')' for version,loaders in installed.items()) or '—')
    lines.append('| '+project['title'].replace('|','/')+' | '+' | '.join(values)+' |')
lines+=['','The native shader fixture holds an ordinary rowing or stopped phase when shader compilation leaves too few drawn frames. It waits for actual Fresh model observations without injecting samples or changing the release animation. The bubble fixture keeps air above the water surface, avoiding vanilla underwater ejection before the measured bubble cycle. Earlier fixture failures remain in the preserved history.','',
    'Visual clips are assembled from native frame captures, timed by client ticks. The software renderer may repeat frames while compiling shaders. Original key frames, contact sheets and quantitative probes accompany the clips; the videos contain no captured audio.','']
(PRIMARY/'docs/PORT_ACCEPTANCE.md').write_text('\n'.join(lines))
print(PRIMARY/'docs/PORT_ACCEPTANCE.md')
