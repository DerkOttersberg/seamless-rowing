"""Contact sheets for reviewing real final-hash captures, without replacing raw evidence."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('names',nargs='+');a=p.parse_args()
out=Path(__file__).resolve().parents[1]/'qa-artifacts/port-acceptance/visual-review';out.mkdir(parents=True,exist_ok=True)
for name in a.names:
    profile=a.stage/('client-'+name)
    canvas=Image.new('RGB',(1280,900),'#20242b');draw=ImageDraw.Draw(canvas)
    ticks=(14,26,52,66,106,116,128,136,162,170,182,194)
    for index,tick in enumerate(ticks):
        source=Image.open(profile/'screenshots'/('fresh-%03d.png'%tick)).convert('RGB')
        if tick>=85:
            w,h=source.size;source=source.crop((int(w*.31),int(h*.33),int(w*.72),int(h*.87)))
        source.thumbnail((320,274));x=(index%4)*320;y=(index//4)*300
        canvas.paste(source,(x+(320-source.width)//2,y+24));draw.text((x+8,y+5),name+' / '+str(tick),fill='white')
    canvas.save(out/(name+'.png'))
print(out)
