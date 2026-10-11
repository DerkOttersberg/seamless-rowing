"""Package only accepted gameplay bytes; refuse historical or partial native acceptance."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess,zipfile
BASE=Path(__file__).resolve().parents[2]
PRIMARY=BASE/'seamless-rowing'
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
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
    if identity in representatives:
        for gate in ('reconnect','sdk-pair','fallback','boat-fallback','edge'):
            if not cell['gates'].get(gate):problems.append(label+': missing '+gate)
        if not any(r.get('shaderPackLogVerified') is True for r in accepted):problems.append(label+': missing active shader run')
    if cell['loader']!='forge' or cell['minecraft'].startswith('1.'):
        if not any(r.get('resourcePackLogVerified') is True and Path(r.get('job',{}).get('fresh_pack','')).name=='FA+Player-v1.1.zip' for r in accepted):
            problems.append(label+': missing Fresh Player 1.1 run')
if len(audit['cells'])!=11:problems.append('Expected exactly eleven distribution cells')
if problems:raise SystemExit('Acceptance incomplete:\n'+'\n'.join(problems))
out.mkdir(parents=True);(out/'mods').mkdir();(out/'evidence').mkdir()
manifest={'product':'Seamless Rowing','version':'0.1.0','publication':'Local accepted bundle; GitHub and CurseForge publication are separate','cells':[]}
for cell in audit['cells']:
    source=Path(cell['jar']);target=out/'mods'/cell['filename'];shutil.copy2(source,target)
    assert hashlib.sha256(target.read_bytes()).hexdigest()==cell['sha256']
    with zipfile.ZipFile(target) as jar:
        assert not any(n.startswith(('qa/','com/derko/swordthrow/')) for n in jar.namelist())
    repo=source.parents[3]
    commit=subprocess.check_output(['git','-C',str(repo),'rev-parse','HEAD'],text=True).strip()
    manifest['cells'].append(dict(cell,sourceCommit=commit,jar='mods/'+target.name))
for name in ('LICENSE','README.md','PORTING.md'):shutil.copy2(PRIMARY/name,out/name)
for name in ('current-hash-audit.json',):shutil.copy2(PRIMARY/'qa-artifacts/port-acceptance'/name,out/'evidence'/name)
for file in stage.glob('*results.json'):
    if isinstance(json.loads(file.read_text(encoding='utf-8-sig')),list):shutil.copy2(file,out/'evidence'/file.name)
(out/'manifest.json').write_text(json.dumps(manifest,indent=2))
(out/'SHA256SUMS.txt').write_text(''.join(c['sha256']+'  '+c['jar']+'\n' for c in manifest['cells']))
archive=out.with_suffix('.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as zipout:
    for file in out.rglob('*'):
        if file.is_file():zipout.write(file,file.relative_to(out))
print(archive,hashlib.sha256(archive.read_bytes()).hexdigest())
