"""Pause owned queue schedulers, leaving every already-running game untouched."""
from pathlib import Path
import json,os,signal,subprocess,sys
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
record=ROOT/'paused-review-schedulers.json'
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
prefix='/mnt/c/Users/derko/Desktop/minecraft/seamless-rowing/qa/run_native_'
if sys.argv[1]=='pause':
    assert not record.exists()
    paused=[]
    for line in subprocess.check_output(['ps','-eo','pid,args'],text=True).splitlines():
        fields=line.strip().split(None,1)
        if len(fields)!=2:continue
        pid,command=fields
        if not command.startswith('python3 '+prefix) or str(ROOT) not in command:continue
        keep=sys.argv[2:] or ['head-review-g-jobs.json','modern-immersive-gl-g-jobs.json']
        if any(Path(command.split()[-1]).name==name for name in keep):continue
        os.kill(int(pid),signal.SIGSTOP);paused.append({'pid':int(pid),'command':command})
    record.write_text(json.dumps(paused,indent=2));print('Paused only',len(paused),'queue schedulers; game processes continue')
elif sys.argv[1]=='resume':
    for task in json.loads(record.read_text()):
        path=Path('/proc')/str(task['pid'])/'cmdline'
        if path.exists():
            actual=path.read_bytes().replace(b'\0',b' ').decode().strip()
            assert actual==task['command'],actual
            os.kill(task['pid'],signal.SIGCONT)
    record.rename(ROOT/'resumed-review-schedulers.json');print('Resumed owned queue schedulers')
else:raise ValueError('Use pause or resume')
