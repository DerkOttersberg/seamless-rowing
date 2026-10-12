"""Stop obsolete owned QA after a gameplay rebuild; preserve its recorded history."""
from pathlib import Path
import argparse,json,os,signal,subprocess,time
qa=Path(__file__).resolve().parent;root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
p=argparse.ArgumentParser();p.add_argument('jobs');p.add_argument('--pairs',action='store_true');a=p.parse_args()
assert Path(a.jobs).name==a.jobs and a.jobs.endswith('-jobs.json')
expected='python3 '+str(qa/('run_native_pairs_queue.py' if a.pairs else 'run_native_queue.py'))+' '+str(root/a.jobs)
rows=[line.strip().split(None,2) for line in subprocess.check_output(['ps','-eo','pid,pgid,args'],text=True).splitlines()[1:]]
matches=[row for row in rows if len(row)==3 and row[2]==expected]
assert len(matches)==1,matches
pid=int(matches[0][0]);group=os.getpgid(pid)
if group!=pid:
    leader=next(row[2] for row in rows if int(row[0])==group)
    assert leader.startswith('python3 -c ') and str(qa) in leader and str(root) in leader and a.jobs in leader
members=[{'pid':int(row[0]),'command':row[2]} for row in rows if len(row)==3 and int(row[1])==group]
assert all(str(root) in member['command'] or member['command'].startswith(('Xvfb ','/bin/sh /usr/bin/xvfb-run','bash /mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh','timeout ','python3 /mnt/c/Users/derko/Desktop/minecraft/seamless-rowing/qa/')) for member in members)
(root/(a.jobs.removesuffix('-jobs.json')+'-cancelled-for-rebuild.json')).write_text(json.dumps({'reason':'Gameplay JAR rebuild makes the pending acceptance obsolete','time':time.time(),'processGroup':group,'members':members},indent=2))
os.killpg(group,signal.SIGTERM)
print('Stopped obsolete owned QA group',group,'with',len(members),'processes; completed results preserved')
