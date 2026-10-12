"""Resume only unfinished gates in the explicitly owned disposable test stage."""
from pathlib import Path
import json, os, subprocess

QA = Path(__file__).resolve().parent
ROOT = Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro'] == 'SeamlessRowingPortsQA-20261011'
assert not any('run_native_queue.py' in line or 'run_native_pairs_queue.py' in line
               for line in subprocess.check_output(['ps', '-eo', 'args'], text=True).splitlines())
completed = {r['name'] for r in json.loads((ROOT/'final-optional-u-jobs-results.json').read_text()) if r.get('pass') is True}
pending = [dict(j, name=j['name'].rsplit('-', 1)[0]+'-aj')
           for j in json.loads((ROOT/'final-optional-u-jobs.json').read_text())
           if j['name'] not in completed and j.get('scenario') != 'edge']
shaders = [j for j in pending if j.get('shader_pack')]
others = [j for j in pending if not j.get('shader_pack')]
edges = [dict(j, name=j['name'].rsplit('-', 1)[0]+'-aj')
         for j in json.loads((ROOT/'edge-ah-jobs.json').read_text())]
legacy = json.loads((ROOT/'final-legacy-aj-jobs.json').read_text())
modern = json.loads((ROOT/'final-modern-aj-jobs.json').read_text())
plans = [('resume-shaders-aj', shaders), ('resume-sp-aj', edges+legacy+modern+others)]
for name, jobs in plans:
    (ROOT/(name+'-jobs.json')).write_text(json.dumps(jobs, indent=2))

# Each detached parent runs one sequence. Their clients use two independent,
# bounded private display slots; no third rendering slot is introduced.
sequences = [
    [('run_native_queue.py', 'resume-shaders-aj-jobs.json'),
     ('run_native_pairs_queue.py', 'final-pairs-aj-jobs.json')],
    [('run_native_queue.py', 'resume-sp-aj-jobs.json')],
]
for index, sequence in enumerate(sequences):
    environment = dict(os.environ)
    if index == 1:
        environment['ISOLATED_CLIENT_LOCK_FILE'] = '/tmp/seamless-rowing-legacy.lock'
    code = ('import subprocess; sequence='+repr(sequence)+'; qa='+repr(str(QA))+'; root='+repr(str(ROOT))+'\n'
            'for script, plan in sequence:\n'
            ' with open(root+"/"+plan.replace("-jobs.json", "-queue.log"), "a") as log:\n'
            '  subprocess.run(["python3", qa+"/"+script, root+"/"+plan], stdout=log, stderr=subprocess.STDOUT)\n')
    with (ROOT/('resume-slot-'+str(index)+'-aj.log')).open('a') as log:
        process = subprocess.Popen(['python3', '-c', code], stdin=subprocess.DEVNULL,
                                   stdout=log, stderr=subprocess.STDOUT, env=environment, start_new_session=True)
    print('Started owned slot', index, 'PID', process.pid, 'sequence', sequence, flush=True)
print('Missing single-client jobs:', len(shaders)+len(edges)+len(legacy)+len(modern)+len(others), flush=True)
