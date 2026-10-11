"""Keep fresh final-hash motion evidence separate from earlier preview results."""
from pathlib import Path
import hashlib, json, shutil, zipfile, xml.etree.ElementTree as ET

repo=Path(__file__).resolve().parents[1]
stage=Path(r'D:\SeamlessRowingQA-20261011')
out=repo/'qa-artifacts/preview-3-26.3-fabric'
out.mkdir(parents=True,exist_ok=True)
jar=repo/'fabric/build/libs/seamless-rowing-0.1.0-preview.3+mc26.3-fabric.jar'
candidate=hashlib.sha256(jar.read_bytes()).hexdigest()
expected='cac41d1f1b601989c711e020f4c20d3d1ccd56efb697fb3eafcf880518ac8041'
if candidate!=expected:raise RuntimeError('Unexpected final candidate')
results={}
for run,primary in {'s1':'rowing-contact.json','s2':'rowing-matrix.json','s3':'rowing-lifecycle.json',
                    'pair14rower':'rowing-multiplayer.json','pair14observer':'rowing-multiplayer.json'}.items():
    profile=stage/('client-'+run);target=out/run;target.mkdir(exist_ok=True)
    hashes={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (profile/'mods').glob('*.jar')}
    before=json.loads((profile/'jar-hashes-final.json').read_text(encoding='utf-8-sig'))
    if hashes!=before or hashes[jar.name]!=candidate:raise RuntimeError('Changed candidate in '+run)
    reports={name:json.loads((profile/name).read_text()) for name in (primary,'rowing-anatomy.json','rowing-surface.json')}
    if not all(r['pass'] for r in reports.values()):raise RuntimeError('Failed gate: '+run)
    if run=='s2':
        matrix=reports[primary]
        if len(matrix['strokePhaseBins'])!=8 or matrix['duplicateArmSubmissions']!=0:raise RuntimeError('Incomplete stroke/duplicate gate')
    for name in reports:shutil.copy2(profile/name,target/name)
    shutil.copy2(profile/'logs/latest.log',target/'latest.log')
    (target/'actual-jar-hashes.json').write_text(json.dumps(hashes,indent=2))
    mods=[]
    for f in sorted((profile/'mods').glob('*.jar')):
        with zipfile.ZipFile(f) as z:meta=json.loads(z.read('fabric.mod.json'),strict=False)
        mods.append({'id':meta['id'],'version':meta['version'],'file':f.name,'sha256':hashes[f.name]})
    (target/'tested-mods.json').write_text(json.dumps(mods,indent=2));results[run]=reports
    if run in ('s2','s3'):
        (target/'screenshots').mkdir(exist_ok=True)
        for f in (profile/'screenshots').glob('*.png'):shutil.copy2(f,target/'screenshots'/f.name)
    if (stage/(run+'-launch.log')).exists():shutil.copy2(stage/(run+'-launch.log'),target/'launch.log')
pair=out/'pair14';pair.mkdir(exist_ok=True)
for name in ('metadata.json','pair-results.json','server-console.log','rower-console.log','observer-console.log'):
    shutil.copy2(stage/'pair14'/name,pair/name)
shutil.copy2(stage/'pair14-launch.log',pair/'launch.log')
diagnostics=out/'diagnostics';diagnostics.mkdir(exist_ok=True)
# Initial geometry review remains identifiable as a different candidate, never a final pass.
for run in ('m1','m2','m3','n1','n2','p2','p3','pair13rower','pair13observer'):
    initial=diagnostics/run;initial.mkdir(exist_ok=True)
    for name in ('rowing-contact.json','rowing-matrix.json','rowing-anatomy.json','rowing-surface.json','jar-hashes.json','logs/latest.log'):
        if (stage/('client-'+run)/name).exists():shutil.copy2(stage/('client-'+run)/name,initial/Path(name).name)
    if (stage/(run+'-launch.log')).exists():shutil.copy2(stage/(run+'-launch.log'),initial/'launch.log')
(diagnostics/'superseded-candidates.txt').write_text('m1: initial surface candidate 120fb303. m2/m3: 20512b4f; failed raft contact (1.112513 pixels). n1/n2/p2/p3/pair13: 87dfaea2; corrected contact and stop easing. n2 initially measured inflated armor as a skin-neck gap; p2 records original native equipment bounds -1..13 and excludes equipment from that skin check. pair13 passed contact/ownership but exposed the FirstPersonModel head-relocation interaction (5000 pixels) during spine deformation. Final CAC41D1F preserves intentionally hidden/relocated heads and uses fresh s1/s2/s3/pair14 acceptance. All earlier results are diagnostics only.\n')
oldpair=diagnostics/'pair13';oldpair.mkdir(exist_ok=True)
for name in ('metadata.json','pair-results.json','rower-console.log','observer-console.log'):shutil.copy2(stage/'pair13'/name,oldpair/name)
shutil.copy2(repo/'build-motion-preview.log',out/'clean-build.log')
tests=[]
for f in sorted((repo/'common/build/test-results/test').glob('TEST-*.xml')):
    root=ET.parse(f).getroot()
    if int(root.attrib['failures']) or int(root.attrib['errors']):raise RuntimeError('Failed unit tests')
    tests.append(dict(root.attrib));shutil.copy2(f,out/f.name)
(out/'acceptance.json').write_text(json.dumps({'candidate':jar.name,'sha256':candidate,'date':'2026-10-11',
    'minecraft':'26.3','loader':'Fabric 0.19.5','java':25,'productionReady':False,
    'stage':'continuous elbows and articulated spine preview; user feedback pending',
    'unitSuites':tests,'nativeResults':results,
    'limitations':['Software Vulkan/llvmpipe; hardware, shaders and high FPS unverified.',
        'Only recorded 26.3 Fabric cases are passes for this hash; the other ten builds wait for feedback.',
        'Full release lifecycle, modpack and mixed-client matrix remains open.']},indent=2))
print('Frozen final-hash motion evidence:',out)
