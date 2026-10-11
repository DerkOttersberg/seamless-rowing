"""Resolve exact upstream builds without pretending incompatible files match a matrix cell."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import hashlib,json,urllib.request,urllib.parse

ROOT=Path(__file__).resolve().parents[1]/'qa-artifacts/integration-matrix'
PROJECTS=('entity-model-features','entitytexturefeatures','first-person-model','not-enough-animations',
          'punchy-fpa','real-camera','3dskinlayers','sodium','iris','embeddium','oculus',
          'hold-my-items','eating-animation','player-animation-library','playeranimator','emotecraft','better-combat')
MATRIX={'26.3':('fabric','forge','neoforge'),'26.2':('fabric','forge','neoforge'),
        '1.21.1':('fabric','forge','neoforge'),'1.20.1':('fabric','forge')}
def request(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Seamless Rowing compatibility verification'}),timeout=90) as response:
        return response.read()
def resolve(project):
    try:
        metadata=json.loads(request('https://api.modrinth.com/v2/project/'+project))
        versions=json.loads(request('https://api.modrinth.com/v2/project/'+project+'/version'))
        cells={}
        for mc,loaders in MATRIX.items():
            for loader in loaders:
                matching=[v for v in versions if mc in v['game_versions'] and loader in v['loaders']]
                matching.sort(key=lambda v:v['date_published'],reverse=True)
                if matching:
                    v=matching[0];f=next((f for f in v['files'] if f['primary']),v['files'][0])
                    cells[mc+'/'+loader]={'version':v['version_number'],'id':v['id'],'file':f,'dependencies':v['dependencies'],'date':v['date_published']}
                else:cells[mc+'/'+loader]=None
        return project,{'title':metadata['title'],'downloads':metadata['downloads'],'source':metadata.get('source_url'),'cells':cells}
    except Exception as failed:return project,{'error':str(failed)}
if __name__=='__main__':
    ROOT.mkdir(parents=True,exist_ok=True)
    with ThreadPoolExecutor(max_workers=4) as pool:result=dict(pool.map(resolve,PROJECTS))
    (ROOT/'resolved.json').write_text(json.dumps(result,indent=2),encoding='utf8')
    for project,data in result.items():
        print(project,data.get('error') or {cell:v['version'] if v else None for cell,v in data['cells'].items()})
