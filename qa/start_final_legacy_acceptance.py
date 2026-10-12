"""Balance final-hash legacy acceptance across the two isolated owned display slots."""
from pathlib import Path
import argparse,json,os,subprocess
qa=Path(__file__).resolve().parent;root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
p=argparse.ArgumentParser();p.add_argument('--suffix',default='ar');a=p.parse_args();assert a.suffix.isalnum();suffix=a.suffix
audit=json.loads((qa.parent/'qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
core=json.loads((root/('final-legacy-'+suffix+'-jobs.json')).read_text())
for job in core:
    if job['scenario']!='settings':job.update(width=640,height=360)
extras=json.loads((root/'legacy-extras-ap-jobs.json').read_text())
extras=[dict(j,name=j['name'].removesuffix('-ap')+'-'+suffix) for j in extras if j.get('scenario')!='edge' or not next(c for c in audit['cells'] if j['repo'].endswith('-'+c['minecraft']) and j['loader']==c['loader'])['gates'].get('edge')]
for job in extras:
    job.update(width=640,height=360)
for source in ('skin-c-jobs.json','boat-e-jobs.json'):
    for job in json.loads((root/source).read_text()):
        if job['repo'].endswith(('seamless-rowing-1.21.1','seamless-rowing-1.20.1')):
            extras.append(dict(job,name=job['name'].rsplit('-',1)[0]+'-'+suffix,width=640,height=360))
pairs=json.loads((root/('final-pairs-'+suffix+'-jobs.json')).read_text())
for job in json.loads((root/'sdk-p-jobs.json').read_text()):
    if job['name'] in ('1211-fabric-sdk-p','1201-forge-sdk-p'):
        cell=next(c for c in audit['cells'] if job['repo'].endswith('-'+c['minecraft']) and job['loader']==c['loader'])
        if not cell['gates'].get('sdk-pair'):pairs.append(dict(job,name=job['name'].removesuffix('-p')+'-'+suffix))
plans={}
for code,mc in [('1211','1.21.1'),('1201','1.20.1')]:
    plans['final-sp-'+code+'-'+suffix]= [j for j in core+extras if j['repo'].endswith('-'+mc)]
    plans['final-mp-'+code+'-'+suffix]= [j for j in pairs if j['repo'].endswith('-'+mc)]
for name,jobs in plans.items():
    assert all(not (root/('client-'+j['name'])).exists() for j in jobs)
    (root/(name+'-jobs.json')).write_text(json.dumps(jobs,indent=2))
sequences=[
    [('run_native_pairs_queue.py','final-mp-1211-'+suffix+'-jobs.json'),('run_native_queue.py','final-sp-1201-'+suffix+'-jobs.json')],
    [('run_native_queue.py','final-sp-1211-'+suffix+'-jobs.json'),('run_native_pairs_queue.py','final-mp-1201-'+suffix+'-jobs.json')],
]
for slot,sequence in enumerate(sequences):
    env=dict(os.environ)
    if slot==1:env['ISOLATED_CLIENT_LOCK_FILE']='/tmp/seamless-rowing-legacy.lock'
    else:env.pop('ISOLATED_CLIENT_LOCK_FILE',None)
    code=('import subprocess; qa='+repr(str(qa))+'; root='+repr(str(root))+'; sequence='+repr(sequence)+'\n'
          'for script,plan in sequence:\n'
          ' with open(root+"/"+plan.replace("-jobs.json","-queue.log"),"a") as log:\n'
          '  result=subprocess.run(["python3",qa+"/"+script,root+"/"+plan],stdout=log,stderr=subprocess.STDOUT)\n'
          '  if result.returncode: break\n')
    with (root/('final-slot-'+str(slot)+'-'+suffix+'.log')).open('a') as log:
        child=subprocess.Popen(['python3','-c',code],stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,env=env,start_new_session=True)
    print('Started final owned slot',slot,'PID',child.pid,sequence,flush=True)
print('Final jobs:',{name:len(jobs) for name,jobs in plans.items()},flush=True)
