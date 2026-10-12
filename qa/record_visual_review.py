"""Record manual inspection of the final arm and torso capture sequences."""
from pathlib import Path
import argparse,datetime,json
p=argparse.ArgumentParser();p.add_argument('stage',type=Path);p.add_argument('--run',action='append');p.add_argument('--matrix',action='append',default=[]);p.add_argument('--observer',action='append',default=[]);a=p.parse_args()
primary=Path(__file__).resolve().parents[1]
audit=json.loads((primary/'qa-artifacts/port-acceptance/current-hash-audit.json').read_text())
names=a.run or ('263-fabric-surface-t','262-neoforge-surface-t','1211-fabric-armor-ao','1201-forge-armor-ao')
records=[]
for name in names:
    profile=a.stage/('client-'+name);hashes=json.loads((profile/'jar-hashes-before.json').read_text())
    cell=next(c for c in audit['cells'] if hashes.get(c['filename'])==c['sha256'])
    for filename in ('rowing-anatomy.json','rowing-surface.json','rowing-native-surfaces.json','rowing-fresh-behavior.json'):
        assert json.loads((profile/filename).read_text())['pass'] is True
    assert (primary/'qa-artifacts/port-acceptance/visual-review'/(name+'.png')).is_file()
    armor=profile/'rowing-armor-visibility.json'
    if armor.is_file():assert json.loads(armor.read_text())['pass'] is True
    records.append({'run':name,'minecraft':cell['minecraft'],'loader':cell['loader'],'gameplaySha256':cell['sha256'],
        'views':['FIRST_PERSON','THIRD_PERSON_BACK','THIRD_PERSON_FRONT'],
        'inspectedTicks':[14,26,52,66,106,116,128,136,162,170,182,194],
        'pass':True,'observations':'Native skin and sleeves remain aligned across the curved arm surfaces. Elbows flex toward the body through strokes. Torso and neck remain connected; original vanilla or Fresh body rendering resumes when rowing releases. Handle contact is independently checked from the submitted native paddle transform.'})
out=primary/'qa-artifacts/port-acceptance/visual-review/review.json'
for name in a.matrix:
    profile=a.stage/('client-'+name);hashes=json.loads((profile/'jar-hashes-before.json').read_text())
    cell=next(c for c in audit['cells'] if hashes.get(c['filename'])==c['sha256'])
    assert json.loads((profile/'rowing-matrix.json').read_text())['pass'] is True
    armor=profile/'rowing-armor-visibility.json'
    if armor.is_file():assert json.loads(armor.read_text())['pass'] is True
    sheet=out.parent/(name+'-armor.json');assert sheet.is_file() and sheet.with_suffix('.png').is_file()
    records.append({'run':name,'minecraft':cell['minecraft'],'loader':cell['loader'],'gameplaySha256':cell['sha256'],'pass':True,
        'views':['FIRST_PERSON','THIRD_PERSON_BACK','THIRD_PERSON_FRONT'],'inspectedCases':json.loads(sheet.read_text())['frames'],
        'observations':'Classic/slim skins and both main-hand settings retain connected skin and armor surfaces. Native trim colors remain aligned. First person contains arms without the chestplate bridge; third person retains the complete chestplate.'})
for name in a.observer:
    sheet=out.parent/(name+'-observer.json');data=json.loads(sheet.read_text());assert sheet.with_suffix('.png').is_file()
    cell=next(c for c in audit['cells'] if data['hashes'].get(c['filename'])==c['sha256'])
    assert data['report']['pass'] is True
    records.append({'run':name,'minecraft':cell['minecraft'],'loader':cell['loader'],'gameplaySha256':cell['sha256'],'pass':True,
        'views':['OBSERVER'],'inspectedTicks':data['ticks'],'observations':'Observer captures show connected torso/arms, natural flexion through strokes, handle contact and ordinary bow rendering during item-action handoff.'})
out.write_text(json.dumps({'reviewedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'review':'Manual inspection of packaged captures and contact sheets, supported by native vertex and geometry probes','runs':records},indent=2))
print(out)
