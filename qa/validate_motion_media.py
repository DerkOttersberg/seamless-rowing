"""Validate decoded native frame counts, dimensions and GIF delivery limits."""
from pathlib import Path
from PIL import Image
import subprocess,json
repo=Path('/mnt/c/Users/derko/Desktop/minecraft/seamless-rowing')
stage=Path('/mnt/d/SeamlessRowingQA-20261011')
media=repo/'qa-artifacts/preview-3-26.3-fabric/media'
expected={'first-person':140,'third-person-back':140,'third-person-front':139,
          'observer':len(list((stage/'client-pair14observer/screenshots').glob('mp-*.png')))}
files={}
for name,count in expected.items():
    f=media/(name+'.mp4')
    data=json.loads(subprocess.check_output(['ffprobe','-v','error','-count_frames','-select_streams','v:0','-show_entries','stream=width,height,r_frame_rate,duration,nb_read_frames','-of','json',str(f)]))['streams'][0]
    if int(data['nb_read_frames'])!=count or data['width']!=1280 or data['height']!=720 or data['r_frame_rate']!='20/1':raise RuntimeError('Incomplete video: '+name)
    files[f.name]=data
for name in ('first-person-stroke','third-person-stroke'):
    f=media/(name+'.gif')
    with Image.open(f) as im:
        data={'width':im.width,'height':im.height,'frames':im.n_frames,'bytes':f.stat().st_size}
        if im.width>=850 or im.n_frames!=32 or f.stat().st_size>=2000000:raise RuntimeError('GIF delivery gate: '+name)
        files[f.name]=data
(media/'media-validation.json').write_text(json.dumps({'pass':True,'files':files,'captureRate':'20 client ticks per second; native images only'},indent=2))
print('All decoded media counts and delivery limits passed')
