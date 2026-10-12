"""Run repaired native fixtures, then always resume the paused owned queue."""
from pathlib import Path
import argparse, json, os, subprocess

qa = Path(__file__).resolve().parent
root = Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro'] == 'SeamlessRowingPortsQA-20261011'
assert (root/'paused-review-schedulers.json').is_file()
p=argparse.ArgumentParser();p.add_argument('--jobs',default='priority-ak-jobs.json');a=p.parse_args()
assert Path(a.jobs).name==a.jobs and a.jobs.endswith('-jobs.json')
count=len(json.loads((root/a.jobs).read_text()));assert 1<=count<=20
prefix=a.jobs.removesuffix('-jobs.json')
environment = dict(os.environ, ISOLATED_CLIENT_LOCK_FILE='/tmp/seamless-rowing-legacy.lock')
with (root/(prefix+'-queue.log')).open('a') as log:
    worker = subprocess.Popen(['python3',str(qa/'run_native_queue.py'),str(root/a.jobs)],
                              stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,
                              env=environment,start_new_session=True)
with (root/(prefix+'-resume.log')).open('a') as log:
    watcher = subprocess.Popen(['python3',str(qa/'resume_after_priority.py'),
                                '--result',prefix+'-jobs-results.json','--count',str(count)],
                               stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,
                               start_new_session=True)
print('Owned priority queue PID',worker.pid,'bounded resume watcher PID',watcher.pid)
