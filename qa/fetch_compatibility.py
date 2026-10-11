"""Resolve matching official Modrinth releases into an owned QA cache."""
from pathlib import Path
import hashlib,json,urllib.request,urllib.parse
root=Path(r'D:\SeamlessRowingQA-20261011\compatibility');root.mkdir(exist_ok=True)
headers={'User-Agent':'SeamlessRowing/0.1.0 local compatibility QA'}
def get(path):
    with urllib.request.urlopen(urllib.request.Request('https://api.modrinth.com/v2/'+path,headers=headers),timeout=60) as r:return json.load(r)
report={}
for project in ['sodium','iris','3dskinlayers','entity-model-features','entitytexturefeatures','emotecraft']:
    try:
        query=urllib.parse.urlencode({'game_versions':json.dumps(['26.3']),'loaders':json.dumps(['fabric'])})
        versions=get('project/'+project+'/version?'+query)
        if not versions:report[project]={'unavailable':True};continue
        v=versions[0];files=[f for f in v['files'] if f.get('primary')]
        f=(files or v['files'])[0];path=root/f['filename']
        if not path.exists():
            with urllib.request.urlopen(urllib.request.Request(f['url'],headers=headers),timeout=90) as r:path.write_bytes(r.read())
        actual=hashlib.sha512(path.read_bytes()).hexdigest()
        if actual!=f['hashes']['sha512']:raise ValueError('Hash mismatch')
        report[project]={'id':v['id'],'version':v['version_number'],'file':path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'dependencies':v['dependencies'],'url':f['url']}
    except Exception as failure:report[project]={'error':str(failure)}
    print(project,report[project].get('version',report[project]),flush=True)
(root/'resolved.json').write_text(json.dumps(report,indent=2))
