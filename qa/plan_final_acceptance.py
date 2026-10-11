"""Plan missing gates against current bytes. Never turn an older passing JAR into acceptance."""
from pathlib import Path
import argparse,hashlib,json,subprocess
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
BASE=Path('/mnt/c/Users/derko/Desktop/minecraft')
QA=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--suffix',required=True);p.add_argument('--skip-scheduled',action='store_true');a=p.parse_args()
assert a.suffix.isalnum()
subprocess.run(['python3',str(QA/'audit_acceptance.py'),str(ROOT)],check=True)
audit=json.loads((BASE/'seamless-rowing/qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
modern=[];legacy=[];pairs=[];scheduled=set()
if a.skip_scheduled:
    for line in subprocess.check_output(['ps','-eo','args'],text=True).splitlines():
        if not line.startswith('python3 '+str(QA/'run_native_')):continue
        plan=Path(line.split()[-1])
        if not plan.is_file() or plan.suffix!='.json':continue
        jobs=json.loads(plan.read_text());result=ROOT/(plan.stem+'-results.json')
        complete={r['name'] for r in json.loads(result.read_text())} if result.is_file() else set()
        for job in jobs:
            if job['name'] in complete:continue
            for cell in audit['cells']:
                repo=BASE/('seamless-rowing'+('' if cell['minecraft']=='26.3' else '-'+cell['minecraft']))
                if job.get('repo')!=str(repo) or job.get('loader')!=cell['loader']:continue
                scenario=job.get('scenario') or ('mixed' if job.get('vanilla_observer') else 'reconnect' if job.get('reconnect_from') else 'sdk-pair' if job.get('extra_mods') else 'pair')
                profile=ROOT/('client-'+job['name']+('' if job.get('scenario') else '-rower'))
                jar=profile/'mods'/cell['filename']
                if not profile.exists() or jar.is_file() and hashlib.sha256(jar.read_bytes()).hexdigest()==cell['sha256']:
                    scheduled.add((cell['minecraft'],cell['loader'],scenario))
    print(len(scheduled),'current-hash gates already scheduled; acceptance still requires their results')
for cell in audit['cells']:
    mc,loader=cell['minecraft'],cell['loader'];code=mc.replace('.','')
    root=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc))
    common=dict(repo=str(root),loader=loader,fixture=str(ROOT/'fixtures'/mc/'rowing-world'))
    target=modern if mc.startswith('26.') else legacy
    for scenario in ('matrix','lifecycle','settings','suite'):
        if cell['gates'].get(scenario) or (mc,loader,scenario) in scheduled:continue
        job=dict(common,name=f'{code}-{loader}-{scenario}-{a.suffix}',scenario=scenario)
        if scenario=='suite':job['extra_mods']=str(BASE/'seamless-rowing/qa-artifacts/integration-matrix'/f'suite{code}-{loader}')
        target.append(job)
    for scenario in ('pair','mixed'):
        if cell['gates'].get(scenario) or (mc,loader,scenario) in scheduled:continue
        job=dict(common,name=f'{code}-{loader}-{scenario}-{a.suffix}')
        if scenario=='mixed':job['vanilla_observer']=True
        pairs.append(job)
    if (mc,loader) in (('26.3','fabric'),('26.2','neoforge'),('1.21.1','fabric'),('1.20.1','forge')) and not cell['gates'].get('reconnect') and (mc,loader,'reconnect') not in scheduled:
        # Use a verified, saved server from a successful current-hash baseline pair.
        source=(cell['gates'].get('pair') or [f'{code}-{loader}-pair-{a.suffix}'])[0]
        pairs.append(dict(common,name=f'{code}-{loader}-reconnect-{a.suffix}',reconnect_from=source))
for name,jobs in [('modern',modern),('legacy',legacy),('pairs',pairs)]:
    (ROOT/f'final-{name}-{a.suffix}-jobs.json').write_text(json.dumps(jobs,indent=2))
    print(name,len(jobs),'missing final-hash gates')
