"""Add an original neutral authoring rig; runtime arms use native-paddle IK."""
from pathlib import Path
import json, uuid
path=Path(__file__).with_name('rowing.bbmodel')
model=json.loads(path.read_text())
body=model['outliner'][0]
def identifier(name): return str(uuid.uuid5(uuid.NAMESPACE_URL,'seamlessrowing.authoring/'+name))
def cube(name,a,b):
    key=identifier(name)
    model['elements'].append({'name':name,'type':'cube','uuid':key,'from':a,'to':b,'origin':[0,12,0], 'rotation':[0,0,0], 'color':0,'faces':{side:{'uv':[0,0,4,4]} for side in ['north','south','east','west','up','down']}})
    return key
model['elements']=[];body['children']=[]
body['children'].append(cube('torso',[-4,12,-2],[4,24,2]))
body['children'].append(cube('head',[-4,24,-4],[4,32,4]))
for side,x in [('left',6),('right',-6)]:
    lower={'name':side+'_forearm','uuid':identifier(side+'_forearm'),'origin':[x,16,0],'children':[cube(side+'_forearm_mesh',[x-2,10,-2],[x+2,16,2])]}
    arm={'name':side+'_upper_arm','uuid':identifier(side+'_upper_arm'),'origin':[x,22,0],'children':[cube(side+'_upper_arm_mesh',[x-2,16,-2],[x+2,22,2]),lower]}
    body['children'].append(arm)
    body['children'].append(cube(side+'_seated_leg',[x/3-2,8,-8],[x/3+2,12,4]))
model['display']={}
path.write_text(json.dumps(model,indent=2)+'\n')
