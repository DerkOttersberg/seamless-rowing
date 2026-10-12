"""Package only accepted gameplay bytes; refuse historical or partial native acceptance."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess,zipfile,xml.etree.ElementTree as ET
BASE=Path(__file__).resolve().parents[2]
PRIMARY=BASE/'seamless-rowing'
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('--output',type=Path,required=True);p.add_argument('--check-only',action='store_true');a=p.parse_args()
stage=a.stage.resolve();out=a.output.resolve()
assert json.loads((stage/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
assert out.is_relative_to(BASE) and not out.exists()
subprocess.run(['python3',str(Path(__file__).with_name('audit_acceptance.py')),str(stage)],check=True)
audit=json.loads((PRIMARY/'qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
records=[]
for resultfile in stage.glob('*results.json'):
    data=json.loads(resultfile.read_text(encoding='utf-8-sig'))
    if isinstance(data,list):records+=data
def matches(result,cell):
    hashes=result.get('hashes') or result.get('report',{}).get('hashes',{}).get('rower',{})
    return result.get('pass') is True and hashes.get(cell['filename'])==cell['sha256']
problems=[]
representatives={('26.3','fabric'),('26.2','neoforge'),('1.21.1','fabric'),('1.20.1','forge')}
for cell in audit['cells']:
    identity=(cell['minecraft'],cell['loader']);label='/'.join(identity)
    for gate in ('matrix','lifecycle','settings','suite','pair','mixed'):
        if not cell['gates'].get(gate):problems.append(label+': missing '+gate)
    accepted=[r for r in records if matches(r,cell)]
    if cell['minecraft'].startswith('1.') and not any(r.get('reports',{}).get('rowing-armor-visibility.json',{}).get('pass') is True for r in accepted):
        problems.append(label+': missing native first-person armor visibility regression')
    if cell['minecraft'].startswith('1.') and not any(r.get('reports',{}).get('rowing-lifecycle.json',{}).get('nativeYawBoundaryFrames',0)>0 and r['reports']['rowing-lifecycle.json'].get('nativeYawMaximumDifferenceDegrees',1)<0.001 for r in accepted):
        problems.append(label+': missing native yaw-wrap interpolation regression')
    if identity in {('1.21.1','fabric'),('1.20.1','forge')} and not any(r.get('reports',{}).get('rowing-edge.json',{}).get('underwaterBubbleOwnedTicks',0)>0 and r['reports']['rowing-edge.json'].get('maximumNativeContactPixels',2)<=1 for r in accepted):
        problems.append(label+': missing measured submerged bubble grips for this native renderer generation')
    if identity in representatives:
        for gate in ('reconnect','sdk-pair','fallback','boat-fallback','edge'):
            if not cell['gates'].get(gate):problems.append(label+': missing '+gate)
        if not any(r.get('shaderPackLogVerified') is True for r in accepted):problems.append(label+': missing active shader run')
        if not any(r.get('resourcePackLogVerified') is True and r.get('job',{}).get('scenario')=='matrix' for r in accepted):
            problems.append(label+': missing Fresh matrix for skin models, armor and camera settings')
        if not any(r.get('reports',{}).get('rowing-edge.json',{}).get('nativeBubbleOwnedTicks',0)>5 and r['reports']['rowing-edge.json'].get('maximumNativeContactPixels',2)<=1 for r in accepted):
            problems.append(label+': missing measured established grips during a native bubble cycle')
    if identity==('26.2','neoforge') and not any(any(name.startswith('bettercombat') for name in r.get('report',{}).get('serverModHashes',{})) for r in accepted):
        problems.append(label+': missing Better Combat with its required server installation')
    if cell['loader']!='forge' or cell['minecraft'].startswith('1.'):
        if not any(r.get('resourcePackLogVerified') is True and Path(r.get('job',{}).get('fresh_pack','')).name=='FA+Player-v1.1.zip' for r in accepted):
            problems.append(label+': missing Fresh Player 1.1 run')
    if identity==('1.20.1','fabric') and not any(r.get('reports',{}).get('rowing-fresh-behavior.json',{}).get('groupedAttackReleasedTicks',0)>0 for r in accepted):
        problems.append(label+': missing actual Hold My Items attack handoff')
    if identity==('26.3','fabric'):
        if not any(3<r.get('reports',{}).get('rowing-frame-cadence.json',{}).get('averageFps',0)<=15 and r['reports']['rowing-frame-cadence.json'].get('maximumMeasuredWindowFps',100)<=15 for r in accepted):
            problems.append(label+': missing measured low-FPS run')
        if not any(r.get('reports',{}).get('rowing-frame-cadence.json',{}).get('maximumMeasuredWindowFps',0)>=60 for r in accepted):
            problems.append(label+': missing measured high-FPS run')
if len(audit['cells'])!=11:problems.append('Expected exactly eleven distribution cells')
if problems:raise SystemExit('Acceptance incomplete:\n'+'\n'.join(problems))
if a.check_only:
    print('All eleven current-byte native acceptance cells and required optional gates passed.')
    raise SystemExit(0)
for cell in audit['cells']:
    repo=Path(cell['jar']).parents[3]
    if subprocess.check_output(['git','-C',str(repo),'status','--porcelain'],text=True).strip():
        raise SystemExit('Commit final source and documentation before packaging: '+str(repo))
    tests=list((repo/'common/build/test-results/test').glob('TEST-*.xml'))
    assert tests and sum(int(ET.parse(f).getroot().get('tests','0')) for f in tests)>=19
    assert all(int(ET.parse(f).getroot().get('failures','0'))==0 and int(ET.parse(f).getroot().get('errors','0'))==0 for f in tests)
    assert any(case.get('name')=='bankedGripCrossingShoulderHeightKeepsTheElbowBranchAndContact()' or case.get('name')=='bankedGripCrossingShoulderHeightKeepsTheElbowBranchAndContact' for f in tests for case in ET.parse(f).getroot().iter('testcase'))
native=PRIMARY/'qa-artifacts/port-acceptance/native'
assert (native/'manifest.json').is_file(), 'Archive native reports and visual evidence first'
assert json.loads((native/'manifest.json').read_text())['currentJarHashes']=={c['filename']:c['sha256'] for c in audit['cells']}
visual=PRIMARY/'qa-artifacts/port-acceptance/visual-review'
review=json.loads((visual/'review.json').read_text())
for mc in ('26.3','26.2','1.21.1','1.20.1'):
    assert any(r.get('pass') is True and r['minecraft']==mc and any(c['sha256']==r['gameplaySha256'] for c in audit['cells']) for r in review['runs']), 'Missing final-byte visual review: '+mc
out.mkdir(parents=True);(out/'mods').mkdir();(out/'evidence').mkdir()
manifest={'product':'Seamless Rowing','version':'0.1.0','publication':'Local accepted bundle; GitHub and CurseForge publication are separate','cells':[]}
for cell in audit['cells']:
    source=Path(cell['jar']);target=out/'mods'/cell['filename'];shutil.copy2(source,target)
    assert hashlib.sha256(target.read_bytes()).hexdigest()==cell['sha256']
    with zipfile.ZipFile(target) as jar:
        assert not any(n.startswith(('qa/','com/derko/swordthrow/','io/github/derkottersberg/swordthrow/')) for n in jar.namelist())
    repo=source.parents[3]
    if subprocess.check_output(['git','-C',str(repo),'status','--porcelain'],text=True).strip():
        raise SystemExit('Commit final source and documentation before packaging: '+str(repo))
    commit=subprocess.check_output(['git','-C',str(repo),'rev-parse','HEAD'],text=True).strip()
    manifest['cells'].append(dict(cell,sourceCommit=commit,jar='mods/'+target.name))
for name in ('LICENSE','README.md','PORTING.md'):shutil.copy2(PRIMARY/name,out/name)
shutil.copy2(PRIMARY/'docs/PORT_ACCEPTANCE.md',out/'PORT_ACCEPTANCE.md')
shutil.copytree(native,out/'evidence/native')
shutil.copytree(visual,out/'evidence/visual-review')
(out/'evidence/unit-tests').mkdir()
for mc in ('26.3','26.2','1.21.1','1.20.1'):
    repo=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc));target=out/'evidence/unit-tests'/mc;target.mkdir()
    for file in (repo/'common/build/test-results/test').glob('TEST-*.xml'):shutil.copy2(file,target/file.name)
for name in ('current-hash-audit.json',):shutil.copy2(PRIMARY/'qa-artifacts/port-acceptance'/name,out/'evidence'/name)
for file in stage.glob('*results.json'):
    if isinstance(json.loads(file.read_text(encoding='utf-8-sig')),list):shutil.copy2(file,out/'evidence'/file.name)
(out/'evidence/logs').mkdir()
for file in stage.glob('*-*.log'):
    shutil.copy2(file,out/'evidence/logs'/file.name)
for directory in stage.iterdir():
    if not directory.is_dir() or not (directory/'metadata.json').is_file() or not (directory/'server').is_dir():continue
    destination=out/'evidence/logs'/directory.name;destination.mkdir()
    for filename in ('metadata.json','pair-results.json','rower.json','observer.json','rower-console.log','observer-console.log','server-console.log'):
        file=directory/filename
        if file.is_file():shutil.copy2(file,destination/filename)
(out/'evidence/input-manifests').mkdir()
shutil.copy2(PRIMARY/'qa-artifacts/integration-matrix/resolved.json',out/'evidence/input-manifests/resolved-project-versions.json')
for source in {r.get('job',{}).get('extra_mods') for r in records}:
    if source and (Path(source)/'manifest.json').is_file():
        shutil.copy2(Path(source)/'manifest.json',out/'evidence/input-manifests'/(Path(source).name+'.json'))
(out/'manifest.json').write_text(json.dumps(manifest,indent=2))
(out/'SHA256SUMS.txt').write_text(''.join(c['sha256']+'  '+c['jar']+'\n' for c in manifest['cells']))
instructions=['# Install Seamless Rowing 0.1.0','','Choose one JAR matching both your Minecraft version and loader. Put that JAR in the client instance’s `mods` folder. Do not install multiple Rowing builds together. Fabric also needs the matching Fabric API. Java 17 is used for 1.20.1, Java 21 for 1.21.1, and Java 25 for 26.2/26.3.','','Rowing is client-only; servers need no installation. Each player who wants to see the animation installs it on their own client. No SeamlessLib or animation library is required.','','Use the normal boat controls. Only the controlling rider rows. Backwards-only movement keeps the vanilla animation. Item use and attacks take priority. Open the settings through the loader’s mod menu or bind “Open rowing settings” in Controls. That shortcut starts unbound.','','| Minecraft | Loader | File |','|---|---|---|']
instructions+=['| '+c['minecraft']+' | '+c['loader']+' | ['+c['filename']+']('+c['jar']+') |' for c in manifest['cells']]
instructions+=['','Fresh Player Extension 1.1 is supported with the tested EMF/ETF versions. Fresh keeps its player model outside rowing; the verified vanilla rig is used during rowing. Exact optional combinations, native measurements and limitations are recorded in `PORT_ACCEPTANCE.md` and `evidence`. Unknown custom rigs preserve their existing renderer.','', 'The bundle includes acceptance media and reports. Only files under `mods` are installed in Minecraft. All Rights Reserved, with permission for modpack inclusion as described in `LICENSE`.','']
(out/'INSTALL.md').write_text('\n'.join(instructions))
archive=out.with_name(out.name+'.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as zipout:
    for file in out.rglob('*'):
        if file.is_file():zipout.write(file,file.relative_to(out))
print(archive,hashlib.sha256(archive.read_bytes()).hexdigest())
