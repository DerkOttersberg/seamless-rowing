"""Repeat required native gates after a shared animation rebuild, using verified input plans."""
from pathlib import Path
import argparse,json,os,subprocess
qa=Path(__file__).resolve().parent
root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
p=argparse.ArgumentParser();p.add_argument('--suffix',required=True);a=p.parse_args();assert a.suffix.isalnum()
old=json.loads((root/'pre-bend-audit.json').read_text())['cells']
records=[]
for file in root.glob('*results.json'):
    data=json.loads(file.read_text(encoding='utf-8-sig'))
    if isinstance(data,list):records.extend(data)
def matching(cell):
    return [r for r in records if r.get('pass') is True and (r.get('hashes') or r.get('report',{}).get('hashes',{}).get('rower',{})).get(cell['filename'])==cell['sha256']]
def latest(options):
    assert options,'No verified input plan for required gate'
    return max(options,key=lambda r:r.get('finished',0))['job']
def clone(job,cell,tag):
    value=dict(job,name=cell['minecraft'].replace('.','')+'-'+cell['loader']+'-'+tag+'-'+a.suffix)
    if value.get('scenario')!='settings':value.update(width=640,height=360)
    if tag.startswith('cadence'):value.update(width=320,height=240)
    assert not (root/('client-'+value['name'])).exists() and not (root/value['name']).exists()
    return value
sp=[];groups=[]
representatives={('26.3','fabric'),('26.2','neoforge'),('1.21.1','fabric'),('1.20.1','forge')}
for cell in old:
    identity=cell['minecraft'],cell['loader'];valid=matching(cell)
    for scenario in ('matrix','lifecycle','settings','suite'):
        candidates=[r for r in valid if r.get('job',{}).get('scenario')==scenario and (scenario!='matrix' or not r['job'].get('fresh_pack'))]
        sp.append(clone(latest(candidates),cell,scenario))
    if identity in representatives:
        sp.append(clone(latest([r for r in valid if r.get('resourcePackLogVerified') and r.get('job',{}).get('scenario')=='matrix']),cell,'fresh-matrix'))
        sp.append(clone(latest([r for r in valid if r.get('shaderPackLogVerified')]),cell,'shader'))
        for scenario,tag in [('fallback','skin'),('boat-fallback','boat-fallback')]:
            sp.append(clone(latest([r for r in valid if r.get('job',{}).get('scenario')==scenario]),cell,tag))
        edge=[r for r in valid if r.get('job',{}).get('scenario')=='edge']
        if not edge:
            edge=[r for r in records if r.get('job',{}).get('name')=='1201-forge-edge-au']
        sp.append(clone(latest(edge),cell,'edge'))
    elif cell['loader']!='forge' or cell['minecraft'].startswith('1.'):
        fresh=[r for r in valid if r.get('resourcePackLogVerified') and r.get('job',{}).get('scenario')=='fresh']
        if identity==('1.20.1','fabric'):
            fresh=[r for r in fresh if r.get('reports',{}).get('rowing-fresh-behavior.json',{}).get('groupedAttackReleasedTicks',0)>0]
        sp.append(clone(latest(fresh),cell,'fresh-optional'))
    pair=[]
    baseline=latest([r for r in valid if r.get('report') and not r['job'].get('extra_mods') and not r['job'].get('vanilla_observer') and not r['job'].get('reconnect_from')])
    pair.append(clone(baseline,cell,'pair'))
    pair.append(clone(latest([r for r in valid if r.get('job',{}).get('vanilla_observer')]),cell,'mixed'))
    if identity in representatives:
        reconnect=dict(baseline,reconnect_from=pair[0]['name'])
        pair.append(clone(reconnect,cell,'reconnect'))
        sdk=[r for r in valid if r.get('report') and r['job'].get('extra_mods') and not r['job'].get('server_mods')]
        if not sdk:
            sdk=[{'job':j,'finished':0} for j in json.loads((root/'sdk-p-jobs.json').read_text()) if j['repo']==baseline['repo'] and j['loader']==cell['loader']]
        pair.append(clone(latest(sdk),cell,'sdk'))
    if identity==('26.2','neoforge'):
        combat=[r for r in valid if any(n.startswith('bettercombat') for n in r.get('report',{}).get('serverModHashes',{}))]
        pair.append(clone(latest(combat),cell,'combat-server'))
    groups.append(pair)
for tag,name in [('cadence-low','263-fabric-cadence-low-ag'),('cadence-high','263-fabric-cadence-high-ag')]:
    cell=next(c for c in old if (c['minecraft'],c['loader'])==('26.3','fabric'))
    sp.append(clone(latest([r for r in matching(cell) if r['name']==name]),cell,tag))
# Review the previously failing bubble cycle and final shader footage before the longer matrices.
sp.sort(key=lambda j:(0 if j['name'].startswith('1201-forge-edge-') else 1 if j.get('shader_pack') else 2 if j.get('fresh_pack') and j['scenario']=='matrix' else 3,j['name']))
slots=[[],[]];load=[0,0]
for job in sp:
    index=load.index(min(load));slots[index].append(job)
    load[index]+=150 if job['scenario']=='matrix' else 85 if job.get('shader_pack') or job['scenario']=='edge' else 55
pairslots=[[j for group in groups for j in group],[]]
all_remaining=[j for j in sp if not (j.get('shader_pack') or j['scenario']=='edge' or j.get('fresh_pack') and j['scenario']=='matrix')]
for index in range(2):
    plans=[]
    # Split early visual gates from the remaining singleplayer work, and exercise multiplayer in between.
    early=[j for j in slots[index] if j.get('shader_pack') or j['scenario']=='edge' or j.get('fresh_pack') and j['scenario']=='matrix']
    # The native pair fixture owns one loopback port and synchronization marker set.
    # Keep all pairs serial in slot zero while slot one runs the remaining singleplayer work.
    remaining=all_remaining if index else []
    for kind,jobs,script in [('visual',early,'run_native_queue.py'),('pairs',pairslots[index],'run_native_pairs_queue.py'),('singleplayer',remaining,'run_native_queue.py')]:
        name=f'all-{index}-{kind}-{a.suffix}-jobs.json';(root/name).write_text(json.dumps(jobs,indent=2));plans.append((script,name))
    env=dict(os.environ)
    if index:env['ISOLATED_CLIENT_LOCK_FILE']='/tmp/seamless-rowing-legacy.lock'
    else:env.pop('ISOLATED_CLIENT_LOCK_FILE',None)
    code=('import subprocess; q='+repr(str(qa))+'; r='+repr(str(root))+'; plans='+repr(plans)+'\n'
          'for script,plan in plans:\n'
          ' with open(r+"/"+plan.replace("-jobs.json","-queue.log"),"a") as log:\n'
          '  subprocess.run(["python3",q+"/"+script,r+"/"+plan],stdout=log,stderr=subprocess.STDOUT)\n')
    with (root/f'all-slot-{index}-{a.suffix}.log').open('a') as log:
        child=subprocess.Popen(['python3','-c',code],env=env,stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
    print('Owned slot',index,'PID',child.pid,'SP',len(slots[index]),'pairs',len(pairslots[index]),flush=True)
print('Required jobs',len(sp)+sum(map(len,groups)),flush=True)
