"""Package the inspected preview only; source/QA JARs and test worlds are never distributed."""
from pathlib import Path
import json,hashlib,shutil,subprocess,zipfile
repo=Path(__file__).resolve().parents[1]
evidence=repo/'qa-artifacts/preview-3-26.3-fabric'
accept=json.loads((evidence/'acceptance.json').read_text())
jar=repo/'fabric/build/libs'/accept['candidate']
if hashlib.sha256(jar.read_bytes()).hexdigest()!=accept['sha256']:raise RuntimeError('Changed preview')
media=json.loads((evidence/'media/media-validation.json').read_text())
if not media['pass']:raise RuntimeError('Unvalidated footage')
if not json.loads((evidence/'cleanup.json').read_text())['pass']:raise RuntimeError('Temporary environment still present')
target=repo/'release-preview/26.3-fabric-preview3';target.mkdir(parents=True,exist_ok=True)
shutil.copy2(jar,target/jar.name);shutil.copy2(repo/'LICENSE',target/'LICENSE')
shutil.copy2(repo/'docs/MOTION_PREVIEW_ACCEPTANCE.md',target/'ACCEPTANCE.md')
shutil.copy2(evidence/'acceptance.json',target/'acceptance.json')
shutil.copy2(evidence/'cleanup.json',target/'cleanup.json')
for name in ('first-person-stroke.gif','third-person-stroke.gif'):shutil.copy2(evidence/'media'/name,target/name)
commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=repo,text=True).strip()
(target/'SOURCE_COMMIT.txt').write_text(commit+'\n')
(target/'INSTALL.md').write_text(f'''# Install Seamless Rowing preview.3

Use Minecraft **26.3**, Java **25**, Fabric Loader **0.19.5** and Fabric API **0.161.0+26.3**.

Replace the earlier Rowing preview JAR with `{jar.name}` in your client mods directory. Keep only one Rowing version installed. No SeamlessLib or animation framework is required. The separate QA, common and sources JARs are not gameplay mods.

Mount a vanilla boat or raft as its controlling rider and use the ordinary controls. Review forward strokes, turning, starts/stops, attacking and item use from first person and both third-person views. Other viewers need the mod on their clients; a vanilla server works unchanged. Backwards-only movement keeps vanilla behavior.

This preview adds a stronger curved spine, a continuous textured elbow surface, eased torso recovery and bounded shoulder roll. The seated hips and fixed-length bones are retained. Optional settings start with articulated arms and torso movement enabled. Bind **Open rowing settings** under Options → Controls → Key Binds → Seamless Rowing; it starts unbound. Optional Mod Menu exposes the same screen.

SHA-256: `{accept['sha256']}`

This is the 26.3 Fabric animation checkpoint before the ten remaining ports. Hardware/shaders and the complete release matrix remain open. See ACCEPTANCE.md for measured passes and limits. GitHub publication and CurseForge uploads remain separate actions.
''',encoding='utf-8')
hashes={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in sorted(target.iterdir()) if f.is_file() and f.name!='SHA256SUMS.txt'}
(target/'SHA256SUMS.txt').write_text(''.join(h+'  '+name+'\n' for name,h in hashes.items()))
archive=repo/'release-preview/seamless-rowing-26.3-fabric-preview-3.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for f in sorted(target.iterdir()):z.write(f,f.name)
with zipfile.ZipFile(archive) as z:
    if [n for n in z.namelist() if n.endswith('.jar')]!=[jar.name]:raise RuntimeError('Unexpected archive JAR')
print(json.dumps({'archive':str(archive),'jarSha256':accept['sha256'],'sourceCommit':commit,'archiveSha256':hashlib.sha256(archive.read_bytes()).hexdigest()},indent=2))
