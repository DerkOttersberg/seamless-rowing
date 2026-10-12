"""Detach an explicit acceptance plan into one of the two owned display slots."""
from pathlib import Path
import argparse,json,os,subprocess
qa=Path(__file__).resolve().parent;root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
p=argparse.ArgumentParser();p.add_argument('jobs');p.add_argument('--pairs',action='store_true');p.add_argument('--slot',choices=('default','legacy'),default='default');a=p.parse_args()
assert Path(a.jobs).name==a.jobs and a.jobs.endswith('-jobs.json')
assert isinstance(json.loads((root/a.jobs).read_text()),list)
env=dict(os.environ)
if a.slot=='legacy':env['ISOLATED_CLIENT_LOCK_FILE']='/tmp/seamless-rowing-legacy.lock'
else:env.pop('ISOLATED_CLIENT_LOCK_FILE',None)
script='run_native_pairs_queue.py' if a.pairs else 'run_native_queue.py'
with (root/a.jobs.replace('-jobs.json','-queue.log')).open('a') as log:
    child=subprocess.Popen(['python3',str(qa/script),str(root/a.jobs)],stdin=subprocess.DEVNULL,
                            stdout=log,stderr=subprocess.STDOUT,env=env,start_new_session=True)
print('Started owned',a.slot,'queue',a.jobs,'PID',child.pid)
