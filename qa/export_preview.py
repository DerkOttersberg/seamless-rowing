"""Preserve final-hash native evidence before disposing of the owned test distro."""
from pathlib import Path
import hashlib, json, shutil, zipfile, xml.etree.ElementTree as ET

repo = Path(__file__).resolve().parents[1]
stage = Path(r'D:\SeamlessRowingQA-20261011')
out = repo / 'qa-artifacts' / 'preview-26.3-fabric'
out.mkdir(parents=True, exist_ok=True)
jar = repo / 'fabric/build/libs/seamless-rowing-0.1.0-preview.1+mc26.3-fabric.jar'
candidate = hashlib.sha256(jar.read_bytes()).hexdigest()
expected = 'f49494a27cbd9701b6b031844698dee224ebffd3f8e12240152147c3305dd855'
if candidate != expected:
    raise RuntimeError('Evidence belongs to another candidate')
scenarios = {
    'r18': 'rowing-fallback.json', 'r19': 'rowing-contact.json',
    'r20': 'rowing-matrix.json', 'r21': 'rowing-lifecycle.json',
    'r22': 'rowing-settings.json', 'r23': 'rowing-contact.json',
    'pair8rower': 'rowing-multiplayer.json', 'pair8observer': 'rowing-multiplayer.json',
    'pair9rower': 'rowing-multiplayer.json', 'pair9observer': 'rowing-multiplayer.json'
}
results, mods = {}, {}
for name, report in scenarios.items():
    profile = stage / ('client-' + name)
    result = json.loads((profile / report).read_text(encoding='utf-8-sig'))
    if not result['pass']:
        raise RuntimeError('Failed final gate: ' + name)
    target = out / name
    target.mkdir(exist_ok=True)
    hashes = {f.name: hashlib.sha256(f.read_bytes()).hexdigest() for f in (profile / 'mods').glob('*.jar')}
    if hashes[jar.name] != candidate:
        raise RuntimeError('Wrong JAR in profile: ' + name)
    if (profile / 'jar-hashes-final.json').exists():
        before = json.loads((profile / 'jar-hashes-final.json').read_text(encoding='utf-8-sig'))
        if before != hashes:
            raise RuntimeError('Profile changed after test: ' + name)
    (target / 'actual-jar-hashes.json').write_text(json.dumps(hashes, indent=2))
    shutil.copy2(profile / report, target / report)
    if (profile / 'logs/latest.log').exists():
        shutil.copy2(profile / 'logs/latest.log', target / 'latest.log')
    launch = stage / (name + '-final-launch.log')
    if launch.exists():
        shutil.copy2(launch, target / 'launch.log')
    mods[name] = []
    for f in sorted((profile / 'mods').glob('*.jar')):
        with zipfile.ZipFile(f) as z:
            # Some optional mods use literal description newlines accepted by Fabric's parser.
            meta = json.loads(z.read('fabric.mod.json'), strict=False)
        mods[name].append({'id': meta['id'], 'version': meta['version'], 'file': f.name, 'sha256': hashes[f.name]})
    shots = profile / 'screenshots'
    if shots.exists():
        # Matrix, settings, fallback and wobble stills are small and useful for independent review.
        if name in ('r18', 'r20', 'r21', 'r22'):
            (target / 'screenshots').mkdir(exist_ok=True)
            for file in shots.glob('*.png'):
                shutil.copy2(file, target / 'screenshots' / file.name)
    results[name] = result
for name in ('pair8', 'pair9'):
    pair = out / name
    pair.mkdir(exist_ok=True)
    for file in ('pair-results.json', 'metadata.json', 'rower-console.log', 'observer-console.log', 'server-console.log'):
        shutil.copy2(stage / name / file, pair / file)
    shutil.copy2(stage / (name + '-final-launch.log'), pair / 'launch.log')
diagnostics = out / 'diagnostics'
diagnostics.mkdir(exist_ok=True)
for run in ('r13', 'r15'):
    file = stage / (run + '-final-launch.log')
    if file.exists():
        shutil.copy2(file, diagnostics / file.name)
    profile = stage / ('client-' + run)
    if (profile / 'jar-hashes-final.json').exists():
        shutil.copy2(profile / 'jar-hashes-final.json', diagnostics / (run + '-actual-jar-hashes.json'))
if (stage / 'compatibility/resolved.json').exists():
    shutil.copy2(stage / 'compatibility/resolved.json', out / 'downloaded-compatibility-builds.json')
shutil.copy2(repo / 'final-build.log', out / 'clean-build.log')
tests = []
for path in sorted((repo / 'common/build/test-results/test').glob('TEST-*.xml')):
    root = ET.parse(path).getroot()
    tests.append({key: root.attrib.get(key, '') for key in ('name', 'tests', 'failures', 'errors', 'skipped')})
    shutil.copy2(path, out / path.name)
(out / 'tested-mods.json').write_text(json.dumps(mods, indent=2))
(out / 'acceptance.json').write_text(json.dumps({
    'candidate': jar.name, 'sha256': candidate, 'minecraft': '26.3', 'loader': 'Fabric 0.19.5',
    'java': '25', 'date': '2026-10-11', 'stage': 'packaged preview; user feedback pending',
    'productionReady': False, 'serverWithoutMods': True, 'unitSuites': tests,
    'nativeResults': results,
    'limitations': [
        'Hardware GPU, high frame rates and shader compatibility unverified. Iris could not initialize OpenGL in the isolated display.',
        '3D Skin Layers intentionally retains its first-person overlay and uses rigid third-person arms.',
        'Custom resource-pack rigs, modded boats and nonstandard armor require verified adapters; existing rendering is preserved.',
        'The full lifecycle, edge-case and mixed-client release matrix remains open. The other ten builds wait for preview feedback.'
    ]
}, indent=2))
print('Exported verified native evidence:', out)
