"""Contact sheets from the observer client's actual multiplayer frame captures."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('names',nargs='+');a=p.parse_args()
out=Path(__file__).resolve().parents[1]/'qa-artifacts/port-acceptance/visual-review';out.mkdir(parents=True,exist_ok=True)
for name in a.names:
    pair=a.stage/name;assert json.loads((pair/'pair-results.json').read_text())['pass'] is True
    metadata=json.loads((pair/'metadata.json').read_text());profile=a.stage/metadata['profiles']['observer']
    result=json.loads((profile/'rowing-multiplayer.json').read_text());assert result['pass'] is True
    canvas=Image.new('RGB',(1280,720),'#20242b');draw=ImageDraw.Draw(canvas)
    for index,tick in enumerate((60,100,180,250)):
        source=Image.open(profile/'screenshots'/('mp-%04d.png'%tick)).convert('RGB')
        w,h=source.size;source=source.crop((int(w*.25),int(h*.20),int(w*.77),int(h*.80)))
        source.thumbnail((640,330));x=(index%2)*640;y=(index//2)*360
        canvas.paste(source,(x+(640-source.width)//2,y+26));draw.text((x+8,y+5),name+' / observer / '+str(tick),fill='white')
    canvas.save(out/(name+'-observer.png'))
    (out/(name+'-observer.json')).write_text(json.dumps({'run':name,'views':['OBSERVER'],'ticks':[60,100,180,250],'report':result,'hashes':json.loads((profile/'jar-hashes-before.json').read_text())},indent=2))
print(out)
