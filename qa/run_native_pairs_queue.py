"""Sequential genuine two-client acceptance, with separate preserved profiles per run."""
from pathlib import Path
import json,os,subprocess,sys,time
from prepare_native_runtime import ROOT
QA=Path(__file__).resolve().parent
WRAPPER=QA.parents[1]/'tools/Run-IsolatedMinecraftClient.sh'
jobs=json.loads(Path(sys.argv[1]).read_text());results=[]
output=ROOT/(Path(sys.argv[1]).stem+'-results.json')
for job in jobs:
    name=job['name'];result={'name':name,'job':job,'started':time.time()}
    print('START PAIR',name,flush=True)
    try:
        command=['python3',str(QA/'prepare_native_pair.py')]
        for key,value in job.items():
            if key=='vanilla_observer':
                if value:command.append('--vanilla-observer')
            elif key!='prepared':command+=['--'+key.replace('_','-'),str(value)]
        if not job.get('prepared'):
            with (ROOT/(name+'-prepare.log')).open('w') as log:
                subprocess.run(command,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=1800)
        path=ROOT/(name+'-launch.log');deadline=time.monotonic()+10800
        while True:
            with path.open('w') as log:
                run=subprocess.run(['bash',str(WRAPPER),'python3',str(QA/'run_native_pair.py'),name],
                    env=dict(os.environ,ALSOFT_DRIVERS='null',ISOLATED_CLIENT_TIMEOUT_SECONDS='1200'),
                    stdout=log,stderr=subprocess.STDOUT,timeout=1260)
            if 'Another isolated Minecraft client is already running.' not in path.read_text(errors='replace'):break
            if time.monotonic()>deadline:raise TimeoutError('Private display slot stayed busy')
            time.sleep(5)
        result['exitCode']=run.returncode
        report=ROOT/name/'pair-results.json'
        result['report']=json.loads(report.read_text()) if report.exists() else {}
        result['pass']=run.returncode==0 and result['report'].get('pass') is True
    except Exception as failure:result['error']=repr(failure);result['pass']=False
    result['finished']=time.time();results.append(result);output.write_text(json.dumps(results,indent=2))
    print('FINISH PAIR',name,'PASS' if result['pass'] else 'FAIL',flush=True)
    if not result['pass']:break
sys.exit(0 if len(results)==len(jobs) and all(r['pass'] for r in results) else 1)
