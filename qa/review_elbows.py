"""Contact sheets for inspecting complete native strokes; no pose/image synthesis."""
from pathlib import Path
from PIL import Image,ImageDraw
import sys

stage=Path('/mnt/d/SeamlessRowingQA-20261011')
out=Path(sys.argv[1]);out.mkdir(parents=True,exist_ok=True)
sample=sys.argv[2] if len(sys.argv)>2 else 'f1'
pair=sys.argv[3] if len(sys.argv)>3 else 'pair11'
if not sample.isalnum() or not pair.isalnum():raise ValueError('Invalid profile')
rows=[('first person','client-'+sample,'rowing-%03d.png',[42,46,50,54,58,62,66,70],None),
      ('front','client-'+sample,'rowing-%03d.png',[218,222,226,230,234,238,242,246],(400,290,880,650))]
if (stage/('client-'+pair+'observer')/'screenshots/mp-0030.png').exists():
    rows.append(('observer','client-'+pair+'observer','mp-%04d.png',[15,19,23,31,35,39,43,47],(280,180,1000,720)))
sheet=Image.new('RGB',(1600,len(rows)*620),(25,28,32));draw=ImageDraw.Draw(sheet)
for row,(label,profile,pattern,frames,crop) in enumerate(rows):
    for column,frame in enumerate(frames):
        image=Image.open(stage/profile/'screenshots'/(pattern%frame)).convert('RGB')
        if crop:image=image.crop(crop)
        image.thumbnail((400,285),Image.Resampling.LANCZOS)
        x=column%4*400;y=row*620+column//4*310
        sheet.paste(image,(x,y));draw.text((x+8,y+288),label+' / '+str(frame),fill='white')
sheet.save(out/'elbow-stroke-review.jpg',quality=94)
print(out/'elbow-stroke-review.jpg')
if (stage/('client-'+pair+'rower')/'screenshots/mp-0380.png').exists():
    frames=[1,11,17,23,31,41,52,64,76,89,97,108,155,171,182,208,223,250,295,310,324,342,358,380]
    sheet=Image.new('RGB',(1600,6*250),(25,28,32));draw=ImageDraw.Draw(sheet)
    for index,frame in enumerate(frames):
        image=Image.open(stage/('client-'+pair+'rower')/'screenshots'/('mp-%04d.png'%frame)).convert('RGB')
        image=image.resize((400,225),Image.Resampling.LANCZOS)
        x=index%4*400;y=index//4*250
        sheet.paste(image,(x,y));draw.text((x+8,y+230),'rower / '+str(frame),fill='white')
    sheet.save(out/'interruptions-review.jpg',quality=94)
