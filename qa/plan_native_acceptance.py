"""Write explicit acceptance plans; executing the plans still requires real game reports."""
from pathlib import Path
import json
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
BASE=Path('/mnt/c/Users/derko/Desktop/minecraft')
ART=BASE/'seamless-rowing/qa-artifacts/integration-matrix'
FRESH=BASE/'seamless-rowing/qa-artifacts/fresh-compat-26.3/dependencies/FA+Player-v1.1.zip'
cells=[(mc,loader) for mc in ('26.3','26.2','1.21.1','1.20.1') for loader in ('fabric','forge','neoforge') if (mc,loader)!=('1.20.1','neoforge')]
def job(mc,loader,scenario):
    code=mc.replace('.','');suffix='' if mc=='26.3' else '-'+mc
    return dict(name=code+'-'+loader+'-'+scenario+'-paddles-b',repo=str(BASE/('seamless-rowing'+suffix)),loader=loader,fixture=str(ROOT/'fixtures'/mc/'rowing-world'),scenario=scenario)
def fresh(mc,loader):
    item=job(mc,loader,'fresh');short='fab' if loader=='fabric' else 'neo' if loader=='neoforge' else 'forge'
    group=ART/('fresh'+mc.replace('.','')+short)
    if (group/'manifest.json').is_file():item.update(extra_mods=str(group),fresh_pack=str(FRESH),realcamera=loader!='forge' or mc=='1.20.1')
    return item
for name,selection in [('modern',[(m,l) for m,l in cells if m.startswith('26.')]),('legacy',[(m,l) for m,l in cells if m.startswith('1.')])]:
    jobs=[fresh(m,l) for m,l in selection if (ART/('fresh'+m.replace('.','')+('fab' if l=='fabric' else 'neo' if l=='neoforge' else 'forge'))/'manifest.json').is_file()]
    jobs += [job(m,l,scenario) for scenario in ('matrix','lifecycle','settings') for m,l in selection]
    (ROOT/(name+'-paddles-b-jobs.json')).write_text(json.dumps(jobs,indent=2))
pairs=[]
for mc,loader in cells:
    item=job(mc,loader,'pair');item.pop('scenario');pairs.append(item)
# Persisted native server state is reused by a second genuine pair after shutdown.
for mc,loader in [('26.3','fabric'),('26.2','neoforge'),('1.21.1','fabric'),('1.20.1','forge')]:
    item=job(mc,loader,'reconnect');item.pop('scenario');item['reconnect_from']=mc.replace('.','')+'-'+loader+'-pair-paddles-b';pairs.append(item)
(ROOT/'pairs-paddles-b-jobs.json').write_text(json.dumps(pairs,indent=2))
print('Prepared',len(cells),'native cells and',len(pairs),'two-client runs')
