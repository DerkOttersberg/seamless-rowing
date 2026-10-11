"""Download exact optional integration inputs and their required dependencies, for one loader cell."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import hashlib,json,sys,urllib.request,urllib.parse
R=Path(__file__).resolve().parents[1]
mc,loader,name,*projects=sys.argv[1:]
if loader not in ('fabric','forge','neoforge') or not name.replace('-','').isalnum():raise ValueError('Invalid matrix cell')
root=R/'qa-artifacts/integration-matrix'/name
root.mkdir(parents=True,exist_ok=False)
matrix=json.loads((root.parent/'resolved.json').read_text());chosen={};unavailable=[];pinned=set();aliases={}
def request(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Seamless Rowing exact compatibility inputs'}),timeout=120) as response:return response.read()
def choose(project,version=None,required=False):
    if version:
        v=json.loads(request('https://api.modrinth.com/v2/version/'+version))
    else:
        versions=json.loads(request('https://api.modrinth.com/v2/project/'+project+'/version?'+urllib.parse.urlencode({'game_versions':json.dumps([mc]),'loaders':json.dumps([loader])})))
        versions.sort(key=lambda v:v['date_published'],reverse=True)
        if not versions:raise ValueError('Required dependency has no matching build: '+project)
        v=versions[0]
    if mc not in v['game_versions'] or loader not in v['loaders']:raise ValueError('Required dependency matrix mismatch: '+project)
    canonical=v['project_id'];aliases[project]=canonical
    if canonical in chosen:
        if canonical in pinned:
            if required and version and chosen[canonical]['id']!=v['id']:raise ValueError('Conflicting exact dependency pins: '+project)
            return
        if chosen[canonical]['id']==v['id']:
            if required and version:pinned.add(canonical)
            return
        if not required:return
    chosen[canonical]=v
    if required and version:pinned.add(canonical)
    for d in v['dependencies']:
        if d['dependency_type']=='required':choose(d['project_id'],d.get('version_id'),True)
for project in projects:
    cell=matrix[project]['cells'].get(mc+'/'+loader)
    if cell:choose(project,cell['id'])
    else:unavailable.append(project)
def download(pair):
    project,v=pair;f=next((f for f in v['files'] if f['primary']),v['files'][0]);data=request(f['url'])
    if hashlib.sha512(data).hexdigest()!=f['hashes']['sha512']:raise ValueError('Upstream hash mismatch')
    path=root/f['filename'];path.write_bytes(data)
    return path.name,hashlib.sha256(data).hexdigest()
with ThreadPoolExecutor(max_workers=4) as pool:jars=dict(pool.map(download,chosen.items()))
(root/'manifest.json').write_text(json.dumps({'minecraft':mc,'loader':loader,'jars':jars,'versions':{p:chosen[c]['version_number'] for p,c in aliases.items()},'unavailable':unavailable},indent=2),encoding='utf8')
print(root);print('Resolved',len(jars),'inputs; unavailable:',unavailable)
