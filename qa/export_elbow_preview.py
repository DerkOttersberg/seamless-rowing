"""Freeze preview.2 evidence separately from the superseded preview.1 captures."""
from pathlib import Path
import hashlib, json, shutil, zipfile, xml.etree.ElementTree as ET

repo=Path(__file__).resolve().parents[1]
stage=Path(r'D:\SeamlessRowingQA-20261011')
out=repo/'qa-artifacts/preview-2-26.3-fabric'
out.mkdir(parents=True,exist_ok=True)
jar=repo/'fabric/build/libs/seamless-rowing-0.1.0-preview.2+mc26.3-fabric.jar'
candidate=hashlib.sha256(jar.read_bytes()).hexdigest()
results={}
for run,report in {'f1':'rowing-contact.json','f2':'rowing-matrix.json','f3':'rowing-lifecycle.json',
                   'pair11rower':'rowing-multiplayer.json','pair11observer':'rowing-multiplayer.json'}.items():
    profile=stage/('client-'+run)
    target=out/run;target.mkdir(exist_ok=True)
    hashes={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (profile/'mods').glob('*.jar')}
    before=json.loads((profile/'jar-hashes-final.json').read_text(encoding='utf-8-sig'))
    if hashes!=before or hashes[jar.name]!=candidate: raise RuntimeError('Changed candidate in '+run)
    reports={name:json.loads((profile/name).read_text()) for name in (report,'rowing-anatomy.json')}
    if not all(r['pass'] for r in reports.values()): raise RuntimeError('Failed gate: '+run)
    for name in reports: shutil.copy2(profile/name,target/name)
    shutil.copy2(profile/'logs/latest.log',target/'latest.log')
    (target/'actual-jar-hashes.json').write_text(json.dumps(hashes,indent=2))
    mods=[]
    for f in sorted((profile/'mods').glob('*.jar')):
        with zipfile.ZipFile(f) as z: meta=json.loads(z.read('fabric.mod.json'),strict=False)
        mods.append({'id':meta['id'],'version':meta['version'],'file':f.name,'sha256':hashes[f.name]})
    (target/'tested-mods.json').write_text(json.dumps(mods,indent=2))
    results[run]=reports
    if run in ('f2','f3'):
        (target/'screenshots').mkdir(exist_ok=True)
        for f in (profile/'screenshots').glob('*.png'): shutil.copy2(f,target/'screenshots'/f.name)
    launch=stage/(run+'-launch.log')
    if launch.exists(): shutil.copy2(launch,target/'launch.log')
pair=out/'pair11';pair.mkdir(exist_ok=True)
for name in ('metadata.json','pair-results.json','server-console.log','rower-console.log','observer-console.log'):
    shutil.copy2(stage/'pair11'/name,pair/name)
shutil.copy2(stage/'pair11-launch.log',pair/'launch.log')
diagnostics=out/'diagnostics';diagnostics.mkdir(exist_ok=True)
for name in ('prerequisites.log','e1-prerequisites-not-ready.log'):
    if (stage/name).exists(): shutil.copy2(stage/name,diagnostics/name)
for run in ('e1','e2','e3','pair10rower','pair10observer'):
    profile=stage/('client-'+run);old=diagnostics/run;old.mkdir(exist_ok=True)
    for f in profile.glob('rowing-*.json'): shutil.copy2(f,old/f.name)
    for name in ('jar-hashes-final.json','logs/latest.log'):
        if (profile/name).exists(): shutil.copy2(profile/name,old/Path(name).name)
(diagnostics/'superseded-candidate.txt').write_text('Candidate d193376b tests passed their narrower checks; superseded after rearward-reach regression exposed potential hinge inversion. Final acceptance uses only d862f745 with native hinge-orientation probes.\n')
shutil.copy2(repo/'build-elbow-preview.log',out/'clean-build.log')
tests=[]
for f in sorted((repo/'common/build/test-results/test').glob('TEST-*.xml')):
    root=ET.parse(f).getroot()
    if int(root.attrib['failures']) or int(root.attrib['errors']): raise RuntimeError('Failed unit tests')
    tests.append(dict(root.attrib));shutil.copy2(f,out/f.name)
(out/'acceptance.json').write_text(json.dumps({'candidate':jar.name,'sha256':candidate,'date':'2026-10-11',
    'minecraft':'26.3','loader':'Fabric 0.19.5','java':25,'productionReady':False,
    'stage':'elbow correction preview; user feedback pending','unitSuites':tests,'nativeResults':results,
    'limitations':['Software Vulkan/llvmpipe; hardware, shaders and high FPS unverified.',
        'Only recorded 26.3 Fabric cases are passes for this hash. Other ten builds wait for feedback.',
        'Full release lifecycle, modpack and mixed-client matrix remains open.']},indent=2))
print('Frozen new-candidate evidence:',out)
