"""Prepare the additional final-byte gates needed after the legacy armor correction."""
from pathlib import Path
import json
root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
assert json.loads((root/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
extras=[]
for job in json.loads((root/'final-optional-u-jobs.json').read_text()):
    if job['name'] in ('1201-fabric-hmi-u','1211-forge-fresh-optional-u','1211-neoforge-fresh-optional-u'):
        extras.append(dict(job,name=job['name'].removesuffix('-u')+'-ap',width=640,height=360))
for file in ('fresh-matrix-y-jobs.json','shader-f-jobs.json','edge-e-jobs.json'):
    for job in json.loads((root/file).read_text()):
        if not job['repo'].endswith(('seamless-rowing-1.21.1','seamless-rowing-1.20.1')):continue
        extras.append(dict(job,name=job['name'].rsplit('-',1)[0]+'-ap'))
(root/'legacy-extras-ap-jobs.json').write_text(json.dumps(extras,indent=2))
pairs=json.loads((root/'final-pairs-ap-jobs.json').read_text())
for job in json.loads((root/'sdk-p-jobs.json').read_text()):
    if job['name'] in ('1211-fabric-sdk-p','1201-forge-sdk-p'):
        pairs.append(dict(job,name=job['name'].removesuffix('-p')+'-ap'))
(root/'final-pairs-ap-jobs.json').write_text(json.dumps(pairs,indent=2))
print('Prepared',len(extras),'additional legacy single-client gates and',len(pairs),'remaining two-client gates')
