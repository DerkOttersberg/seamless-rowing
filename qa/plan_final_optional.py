"""Explicit optional coverage for final bytes; these plans still require native execution."""
from pathlib import Path
import argparse,json
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
BASE=Path('/mnt/c/Users/derko/Desktop/minecraft/seamless-rowing')
p=argparse.ArgumentParser();p.add_argument('--suffix',required=True);a=p.parse_args()
assert a.suffix.isalnum()
jobs=[]
for source in ('modern-paddles-b-jobs.json','legacy-paddles-b-jobs.json'):
    for original in json.loads((ROOT/source).read_text()):
        if original.get('scenario')!='fresh':continue
        job=dict(original,name=original['name'].replace('paddles-b','optional-'+a.suffix))
        if job['repo'].endswith('seamless-rowing') or job['repo'].endswith('seamless-rowing-26.2'):
            job['backend']='opengl'
        jobs.append(job)
# Exercise the distinct 1.20.1 Hold My Items ID with actual attacks and eating.
original=json.loads((ROOT/'legacy-surface-p-jobs.json').read_text())[0]
jobs.insert(0,dict(original,name='1201-fabric-hmi-'+a.suffix))
for source in ('shader-f-jobs.json','skin-c-jobs.json','boat-e-jobs.json','edge-e-jobs.json'):
    for original in json.loads((ROOT/source).read_text()):
        prefix=original['name'].rsplit('-',1)[0]
        jobs.append(dict(original,name=prefix+'-'+a.suffix))
target=ROOT/('final-optional-'+a.suffix+'-jobs.json')
target.write_text(json.dumps(jobs,indent=2));print(target,len(jobs),'native runs')
