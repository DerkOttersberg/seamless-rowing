"""Create new loader-neutral metadata-only water fixtures from the matching owned test template."""
from pathlib import Path
import copy,json,runpy,sys

workspace=Path(__file__).resolve().parents[2]
stage=Path('D:/SeamlessRowingPortsQA-20261011')
assert json.loads((stage/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
mc=sys.argv[1]
templates={'26.3':workspace/'playtest-profiles-26.3/fabric/saves/suite-world',
           '26.2':workspace/'playtest-profiles/fabric/saves/suite-world',
           '1.21.1':workspace/'.ports/dogs-multiversion/mc1.21.1/seamless-dogs/fabric/build/run/gameTest/world',
           '1.20.1':workspace/'.ports/dogs-multiversion/mc1.20.1/seamless-dogs/fabric/build/run/gameTest/world'}
if mc not in templates:raise ValueError('A native template must be verified for this version first')
source=templates[mc];target=stage/'fixtures'/mc/'rowing-world'
if target.exists():raise ValueError('Fixture already exists')
target.mkdir(parents=True)
h=runpy.run_path(str(workspace/'enable-world-commands.py'));read,write,Tag=h['read_level'],h['write_level'],h['Tag']
n,r=read(source/'level.dat');d=r.value['Data'].value
assert d['Version'].value['Name'].value==mc
d.pop('Player',None);d['allowCommands']=Tag(1,1);d['GameType']=Tag(3,1);d['initialized']=Tag(1,1)
d['spawn']=Tag(10,{'pos':Tag(11,[0,-57,0]),'pitch':Tag(5,40.0),'dimension':Tag(8,'minecraft:overworld'),'yaw':Tag(5,0.0)})
if not mc.startswith('26.'):
    for key,value in {'SpawnX':0,'SpawnY':-57,'SpawnZ':0}.items():d[key]=Tag(3,value)
    d['SpawnAngle']=Tag(5,0.0)
    for key in ('doMobSpawning','doDaylightCycle','doWeatherCycle'):d['GameRules'].value[key]=Tag(8,'false')
d['DataPacks']=Tag(10,{'Enabled':Tag(9,(8,['vanilla'])),'Disabled':Tag(9,(8,[]))})
water=Tag(10,{'type':Tag(8,'minecraft:flat'),'settings':Tag(10,{'biome':Tag(8,'minecraft:plains'),'features':Tag(1,0),'lakes':Tag(1,0),
             'structure_overrides':Tag(9,(8,[])),'layers':Tag(9,(10,[{'height':Tag(3,height),'block':Tag(8,block)} for height,block in
             [(1,'minecraft:bedrock'),(2,'minecraft:dirt'),(1,'minecraft:sand'),(2,'minecraft:water')]]))})})
if 'WorldGenSettings' in d:
    d['WorldGenSettings'].value['generate_features']=Tag(1,0)
    d['WorldGenSettings'].value['dimensions'].value['minecraft:overworld'].value['generator']=water
write(target/'level.dat',n,r)
if (source/'data/minecraft/world_gen_settings.dat').exists():
    (target/'data/minecraft').mkdir(parents=True)
    for filename in ('world_gen_settings.dat','game_rules.dat','weather.dat','world_clocks.dat'):
        if not (source/'data/minecraft'/filename).exists():continue
        n,r=read(source/'data/minecraft'/filename);d=r.value['data'].value
        if filename=='world_gen_settings.dat':
            d['generate_structures']=Tag(1,0);d['dimensions'].value['minecraft:overworld'].value['generator']=copy.deepcopy(water)
        if filename=='game_rules.dat':
            for key in ('minecraft:spawn_mobs','minecraft:advance_weather'):d[key]=Tag(1,0)
        write(target/'data/minecraft'/filename,n,r)
print(target)
