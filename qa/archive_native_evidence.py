"""Preserve current-byte native evidence before removing the owned test environment."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('--visual',action='append',default=[]);a=p.parse_args()
PRIMARY=Path(__file__).resolve().parents[1]
stage=a.stage.resolve();dest=PRIMARY/'qa-artifacts/port-acceptance/native'
assert json.loads((stage/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
audit=json.loads((PRIMARY/'qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
cells={c['filename']:c for c in audit['cells']}
records={}
for file in stage.glob('*results.json'):
    data=json.loads(file.read_text(encoding='utf-8-sig'))
    if not isinstance(data,list):continue
    for r in data:
        hashes=r.get('hashes') or r.get('report',{}).get('hashes',{}).get('rower',{})
        if r.get('pass') is True and any(hashes.get(n)==c['sha256'] for n,c in cells.items()):records[r['name']]=r
dest.mkdir(parents=True,exist_ok=True)
# A release-byte change must not leave earlier videos labelled as fresh evidence.
history=dest.parent/'historical-native'
for previous in dest.iterdir():
    if not previous.is_dir() or previous.name in records:continue
    assert previous.resolve().is_relative_to(PRIMARY.resolve()/'qa-artifacts')
    history.mkdir(exist_ok=True);target=history/previous.name
    assert target.resolve().is_relative_to(PRIMARY.resolve()/'qa-artifacts')
    assert not target.exists()
    shutil.move(str(previous),str(target))
for name,r in records.items():
    out=dest/name;out.mkdir(exist_ok=True)
    (out/'result.json').write_text(json.dumps(r,indent=2))
    for file in stage.glob(name+'-*.log'):shutil.copy2(file,out/file.name)
    pair=stage/name
    if (pair/'server').is_dir():
        for filename in ('metadata.json','pair-results.json','rower-console.log','observer-console.log','server-console.log'):
            source=pair/filename
            if source.is_file():shutil.copy2(source,out/filename)
        profiles=json.loads((pair/'metadata.json').read_text())['profiles']
    else:profiles={'client':'client-'+name}
    for role,profile_name in profiles.items():
        profile=stage/profile_name;target=out/role;target.mkdir(exist_ok=True)
        for source in list(profile.glob('rowing*.json'))+[profile/'jar-hashes-before.json']:
            if source.is_file():shutil.copy2(source,target/source.name)
        if name not in a.visual:continue
        screenshots=profile/'screenshots'
        if not screenshots.is_dir():continue
        images=sorted(screenshots.glob('*.png'));media=target/'screenshots';media.mkdir(exist_ok=True)
        keyframes={'fresh-%03d.png'%tick for tick in (2,30,50,74,90,110,130,150,160,174,194,210,228,250,274)}
        matrix_sheet=dest.parent/'visual-review'/(name+'-armor.json')
        if matrix_sheet.is_file():
            inspected=json.loads(matrix_sheet.read_text())
            assert any(inspected['hashes'].get(n)==c['sha256'] for n,c in cells.items())
            keyframes.update(frame['frame'] for frame in inspected['frames'])
        for index,source in enumerate(images):
            if source.name in keyframes or source.name.startswith('mp-') and index%max(1,len(images)//12)==0:
                shutil.copy2(source,media/source.name)
        for prefix,fps in (('fresh-',10),('mp-',20)):
            sequence=sorted(screenshots.glob(prefix+'*.png'))
            if len(sequence)<10:continue
            digest=hashlib.sha256()
            for source in sequence:
                digest.update(source.name.encode());digest.update(source.read_bytes())
            encoding={'fps':fps,'frames':len(sequence),'sourceFramesSha256':digest.hexdigest(),'codec':'libx264-crf18-yuv420p'}
            encoding_file=target/(prefix.rstrip('-')+'-encoding.json')
            video=target/(prefix.rstrip('-')+'.mp4')
            if video.is_file() and encoding_file.is_file() and json.loads(encoding_file.read_text())==encoding:
                continue
            # Fresh captures alternate client ticks; multiplayer captures every tick.
            playlist=target/(prefix+'frames.txt')
            playlist.write_text(''.join("file '"+str(s).replace("'","'\\''")+"'\nduration "+str(1/fps)+'\n' for s in sequence))
            subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','concat','-safe','0','-i',str(playlist),'-an','-c:v','libx264','-threads','2','-preset','fast','-crf','18','-pix_fmt','yuv420p','-r',str(fps),'-movflags','+faststart',str(video)],check=True,timeout=900)
            playlist.unlink()
            encoding_file.write_text(json.dumps(encoding,indent=2))
manifest=[]
for source in dest.rglob('*'):
    if source.is_file() and source.name!='manifest.json':manifest.append({'path':str(source.relative_to(dest)),'sha256':hashlib.sha256(source.read_bytes()).hexdigest()})
(dest/'manifest.json').write_text(json.dumps({'currentJarHashes':{n:c['sha256'] for n,c in cells.items()},'visualRuns':a.visual,'files':manifest},indent=2))
print('Archived',len(records),'passing current-byte runs and',len(a.visual),'visual sequences to',dest)
