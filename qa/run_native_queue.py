#!/usr/bin/env python3
"""Run explicit packaged acceptance profiles sequentially on disposable private displays."""
from pathlib import Path
import json,os,subprocess,sys,time
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
QA=Path(__file__).resolve().parent
WRAPPER=QA.parents[1]/'tools/Run-IsolatedMinecraftClient.sh'
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
jobs=json.loads(Path(sys.argv[1]).read_text());results=[]
results_file=ROOT/(Path(sys.argv[1]).stem+'-results.json')
for job in jobs:
    name=job['name'];print('START',name,flush=True)
    result={'name':name,'started':time.time(),'job':job}
    try:
        args=['python3',str(QA/'prepare_native_runtime.py')]
        for key,value in job.items():
            if key=='realcamera':
                if value:args.append('--realcamera')
            else:args.extend(['--'+key.replace('_','-'),str(value)])
        with (ROOT/(name+'-prepare.log')).open('w') as log:
            subprocess.run(args,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=900)
        env=dict(os.environ,ALSOFT_DRIVERS='null',ISOLATED_CLIENT_TIMEOUT_SECONDS='900')
        launch_log=ROOT/(name+'-launch.log')
        deadline=time.monotonic()+10800
        while True:
            with launch_log.open('w') as log:
                run=subprocess.run(['bash',str(WRAPPER),'python3',str(ROOT/('run_client_'+name+'.py'))],env=env,stdout=log,stderr=subprocess.STDOUT,timeout=960)
            # A second isolated queue may own this display slot. No game started in this case.
            if 'Another isolated Minecraft client is already running.' not in launch_log.read_text(errors='replace'):
                break
            if time.monotonic()>deadline:raise TimeoutError('Private display slot stayed busy')
            time.sleep(5)
        result['exitCode']=run.returncode
        profile=ROOT/('client-'+name)
        reports={p.name:json.loads(p.read_text()) for p in profile.glob('rowing*.json')}
        scenario=job.get('scenario','fresh')
        expected={'control':['rowing-fresh-behavior.json'],'suite':['rowing-suite.json','rowing-anatomy.json','rowing-surface.json'],'boat-fallback':['rowing-boat-fallback.json'],'edge':['rowing-edge.json','rowing-anatomy.json','rowing-surface.json'],'settings':['rowing-settings.json'],'lifecycle':['rowing-lifecycle.json','rowing-anatomy.json','rowing-surface.json'],'fallback':['rowing-fallback.json']}.get(scenario,
            ['rowing-anatomy.json','rowing-surface.json','rowing-matrix.json' if scenario=='matrix' else 'rowing-fresh-behavior.json'])
        result['reports']=reports
        extra_reports=['rowing-paddle-visibility.json','rowing-fresh-model.json','rowing-first-person-ownership.json']
        result['pass']=run.returncode==0 and all(reports.get(p,{}).get('pass') is True for p in expected) and all(reports[p].get('pass') is True for p in extra_reports if p in reports and scenario not in ('settings','fallback','control'))
        if scenario in ('fresh','matrix','suite','lifecycle','edge'):
            result['pass'] &= reports.get('rowing-native-surfaces.json',{}).get('pass') is True
        if job.get('fresh_pack'):
            console=launch_log.read_text(errors='replace')
            loaded=any('Reloading ResourceManager:' in line and 'file/'+Path(job['fresh_pack']).name in line for line in console.splitlines())
            result['resourcePackLogVerified']=loaded;result['pass'] &= loaded
            if Path(job['fresh_pack']).name=='FA+Player-v1.1.zip' and scenario not in ('settings','fallback','control'):
                result['pass'] &= reports.get('rowing-fresh-model.json',{}).get('pass') is True
        if scenario in ('fresh','matrix') and 'rowing-first-person-ownership.json' in reports:
            result['pass'] &= reports['rowing-first-person-ownership.json'].get('nativeHandsBodyQueries',0)>0
        if job.get('shader_pack'):
            console=launch_log.read_text(errors='replace')
            active='Using shaderpack: '+Path(job['shader_pack']).name in console
            result['shaderPackLogVerified']=active;result['pass'] &= active
        result['hashes']=json.loads((profile/'jar-hashes-before.json').read_text())
    except Exception as failure:
        result['error']=repr(failure);result['pass']=False
    result['finished']=time.time();results.append(result)
    results_file.write_text(json.dumps(results,indent=2))
    print('FINISH',name,'PASS' if result['pass'] else 'FAIL',flush=True)
sys.exit(0 if all(r['pass'] for r in results) else 1)
