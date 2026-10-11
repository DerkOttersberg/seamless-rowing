"""Audit native results against current distribution bytes, preserving failed history."""
from pathlib import Path
import hashlib,json,re,sys
ROOT=Path(sys.argv[1])
BASE=Path(__file__).resolve().parents[2]
cells=[]
for mc in ('26.3','26.2','1.21.1','1.20.1'):
    repo=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc))
    version=next(s.split('=',1)[1] for s in (repo/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='))
    for loader in ('fabric','forge','neoforge'):
        if mc=='1.20.1' and loader=='neoforge':continue
        jar=repo/loader/'build/libs'/f'seamless-rowing-{version}-{loader}.jar'
        cells.append(dict(minecraft=mc,loader=loader,jar=str(jar),filename=jar.name,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),gates={}))
history=[]
for file in ROOT.glob('*results.json'):
    data=json.loads(file.read_text(encoding='utf-8-sig'))
    if not isinstance(data,list):continue
    for result in data:
        job=result.get('job',{});name=result.get('name','');passed=result.get('pass') is True
        snapshots=result.get('hashes') or result.get('report',{}).get('hashes',{}).get('rower',{})
        for cell in cells:
            if snapshots.get(cell['filename'])!=cell['sha256']:continue
            scenario=job.get('scenario') or ('mixed' if job.get('vanilla_observer') else 'reconnect' if job.get('reconnect_from') else 'sdk-pair' if job.get('extra_mods') else 'pair')
            history.append(dict(name=name,cell=cell['minecraft']+'/'+cell['loader'],scenario=scenario,pass_=passed,source=file.name))
            if passed:cell['gates'].setdefault(scenario,[]).append(name)
report=dict(cells=cells,history=history)
output=BASE/'seamless-rowing/qa-artifacts/port-acceptance'
output.mkdir(parents=True,exist_ok=True)
(output/'current-hash-audit.json').write_text(json.dumps(report,indent=2))
for c in cells:print(c['minecraft'],c['loader'],c['sha256'][:12],','.join(sorted(c['gates'])))
