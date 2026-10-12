"""Review actual armored skin/hand/camera captures from a passing native matrix."""
from pathlib import Path
import argparse,json,re
from PIL import Image,ImageDraw

p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('names',nargs='+');a=p.parse_args()
out=Path(__file__).resolve().parents[1]/'qa-artifacts/port-acceptance/visual-review';out.mkdir(parents=True,exist_ok=True)
for name in a.names:
    profile=a.stage/('client-'+name)
    assert json.loads((profile/'rowing-matrix.json').read_text())['pass'] is True
    labels=re.findall(r'ROWING_MATRIX_BEGIN ([^\r\n]+)',(a.stage/(name+'-launch.log')).read_text(errors='replace'))
    selected=[(i,label) for i,label in enumerate(labels) if label.startswith('oak_boat/') and label.endswith('/true')]
    assert len(selected)==12
    canvas=Image.new('RGB',(1280,900),'#20242b');draw=ImageDraw.Draw(canvas)
    frames=[]
    for index,(case,label) in enumerate(selected):
        filename='matrix-%02d.png'%case;source=Image.open(profile/'screenshots'/filename).convert('RGB')
        if 'FIRST_PERSON' not in label:
            w,h=source.size;source=source.crop((int(w*.31),int(h*.33),int(w*.72),int(h*.87)))
        source.thumbnail((320,265));x=(index%4)*320;y=(index//4)*300
        canvas.paste(source,(x+(320-source.width)//2,y+32));draw.text((x+5,y+3),label.replace('oak_boat/','').replace('THIRD_PERSON_','TP_'),fill='white')
        frames.append({'frame':filename,'case':label})
    canvas.save(out/(name+'-armor.png'))
    (out/(name+'-armor.json')).write_text(json.dumps({'run':name,'frames':frames,'hashes':json.loads((profile/'jar-hashes-before.json').read_text())},indent=2))
print(out)
