"""Prepare a genuine packaged Fabric client from official manifests and owned fixtures."""
from pathlib import Path
import concurrent.futures
import hashlib
import json
import runpy
import shutil
import urllib.request
import uuid
import zipfile
import sys

repo = Path(__file__).resolve().parents[1]
workspace = repo.parent
stage = Path(r'D:/SeamlessRowingPortsQA-20261011')
assert json.loads((stage/'ownership.json').read_text(encoding='utf-8-sig'))['root'].replace('\\','/') == str(stage).replace('\\','/')
headers = {'User-Agent': 'SeamlessRowing local packaged QA'}
def request(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=120) as response:
        return response.read()
def downloaded(path, url, expected=None, algorithm='sha1'):
    path.parent.mkdir(parents=True, exist_ok=True)
    if not path.exists() or expected and hashlib.new(algorithm,path.read_bytes()).hexdigest()!=expected:
        contents=request(url)
        if expected and hashlib.new(algorithm,contents).hexdigest()!=expected: raise ValueError('Hash mismatch: '+url)
        path.write_bytes(contents)
    return path
info=json.loads((Path.home()/'.gradle/caches/fabric-loom/26.3/mojang_minecraft_info.json').read_text())
loader=json.loads(request('https://meta.fabricmc.net/v2/versions/loader/26.3/0.19.5/profile/json'))
(stage/'fabric-profile.json').write_text(json.dumps(loader,indent=2),encoding='utf-8')
def linux(library):
    if not library.get('rules'): return True
    result=False
    for rule in library['rules']:
        match=not rule.get('os') or rule['os'].get('name','linux')=='linux' and rule['os'].get('arch','x86_64') in ('x86_64','amd64')
        if match and not rule.get('features'): result=rule['action']=='allow'
    return result
artifacts=[l['downloads']['artifact'] for l in info['libraries'] if linux(l) and 'artifact' in l.get('downloads',{})]
def lib(a):return downloaded(stage/'libraries'/a['path'],a['url'],a.get('sha1'))
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool: jars=list(pool.map(lib,artifacts))
for entry in loader['libraries']:
    group,name,version=entry['name'].split(':')
    path='/'.join((group.replace('.','/'),name,version,name+'-'+version+'.jar'))
    jars.append(downloaded(stage/'libraries'/path,entry['url'].rstrip('/')+'/'+path))
client=downloaded(stage/'minecraft-client.jar',info['downloads']['client']['url'],info['downloads']['client']['sha1'])
native=stage/'natives';native.mkdir(exist_ok=True)
for jar in jars:
    with zipfile.ZipFile(jar) as archive:
        for name in archive.namelist():
            if name.endswith('.so') and not any(x in name for x in ('aarch64','arm32','riscv')):
                (native/Path(name).name).write_bytes(archive.read(name))
assets=Path.home()/'.gradle/caches/fabric-loom/assets'
asset_index=info['assetIndex']['id']
if not (assets/'indexes'/(asset_index+'.json')).is_file():asset_index='26.3-'+asset_index
assert (assets/'indexes'/(asset_index+'.json')).is_file()
# Java is installed from the owned distro's signature-verified Ubuntu repository.
# Its exact package and runtime versions are recorded by the setup command.
assert (stage/'java-runtime.txt').is_file(), 'Install and record the owned Java 25 runtime first'
print('Verified native runtime and libraries',len(jars),flush=True)

helper=runpy.run_path(str(workspace/'enable-world-commands.py'));read=helper['read_level'];write=helper['write_level'];Tag=helper['Tag']
fixture=workspace/'playtest-profiles-26.3/fabric/saves/suite-world'
fixture_target=stage/'fixture/rowing-world'
if not fixture_target.exists():
    (fixture_target/'data/minecraft').mkdir(parents=True)
    n,r=read(fixture/'level.dat');d=r.value['Data'].value
    d['allowCommands']=Tag(1,1);d['GameType']=Tag(3,1);d['initialized']=Tag(1,1)
    d['spawn']=Tag(10,{'pos':Tag(11,[0,-57,0]),'pitch':Tag(5,40.0),'dimension':Tag(8,'minecraft:overworld'),'yaw':Tag(5,0.0)})
    d['DataPacks']=Tag(10,{'Enabled':Tag(9,(8,['vanilla','fabric-convention-tags-v2'])),'Disabled':Tag(9,(8,[]))})
    write(fixture_target/'level.dat',n,r)
    for filename in ('world_gen_settings.dat','game_rules.dat','weather.dat','world_clocks.dat'):
        n,r=read(fixture/'data/minecraft'/filename);d=r.value['data'].value
        if filename=='world_gen_settings.dat':
            d['generate_structures']=Tag(1,0)
            d['dimensions'].value['minecraft:overworld'].value['generator']=Tag(10,{
                'type':Tag(8,'minecraft:flat'),'settings':Tag(10,{'biome':Tag(8,'minecraft:plains'),'features':Tag(1,0),'lakes':Tag(1,0),'structure_overrides':Tag(9,(8,[])),
                'layers':Tag(9,(10,[{'height':Tag(3,h),'block':Tag(8,b)} for h,b in [(1,'minecraft:bedrock'),(2,'minecraft:dirt'),(1,'minecraft:sand'),(2,'minecraft:water')]]))})})
        if filename=='game_rules.dat':
            for key in ('minecraft:spawn_mobs','minecraft:advance_weather'):d[key]=Tag(1,0)
        write(fixture_target/'data/minecraft'/filename,n,r)

def unix(path):
    p=str(path).replace('\\','/');return '/mnt/'+p[0].lower()+p[2:]
def profile(name, optional=()):
    target=stage/('client-'+name)
    if target.exists():raise ValueError('Use a new run name')
    (target/'mods').mkdir(parents=True);(target/'resourcepacks').mkdir()
    shutil.copytree(fixture_target,target/'saves/rowing-world')
    version=next(line.split('=',1)[1] for line in (repo/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
    for jar in (repo/('fabric/build/libs/seamless-rowing-'+version+'-fabric.jar'),repo/'qa/build/libs/seamless-rowing-qa.jar',workspace/'playtest-profiles-26.3/dogs-production-manual-20261005/mods/fabric-api-0.161.0+26.3.jar'):
        shutil.copy2(jar,target/'mods')
    resolved=json.loads((repo/'qa-artifacts/fresh-compat-26.3/dependencies/resolved.json').read_text())
    for item in optional:shutil.copy2(repo/'qa-artifacts/fresh-compat-26.3/dependencies'/resolved[item]['file'],target/'mods')
    pack=repo/'qa-artifacts/fresh-compat-26.3/dependencies/FA+Player-v1.1.zip'
    shutil.copy2(pack,target/'resourcepacks')
    (target/'options.txt').write_text('renderDistance:4\nsimulationDistance:5\nmaxFps:30\nvsync:false\npauseOnLostFocus:false\nguiScale:2\nfov:0.0\ncloudStatus:0\nparticles:2\nresourcePacks:["vanilla","file/FA+Player-v1.1.zip"]\n',encoding='utf-8')
    cp=':'.join(unix(p) for p in [client]+jars)
    command=['/opt/rowing-java-test/bin/java','-Xmx768M','-XX:ActiveProcessorCount=2','-Djava.awt.headless=false','-Dorg.lwjgl.librarypath='+unix(native),'-cp',cp,loader['mainClass'],'--username','RowQA','--uuid',uuid.uuid3(uuid.NAMESPACE_DNS,'RowQA').hex,'--accessToken','FabricMC','--version','26.3','--versionType','release','--gameDir',unix(target),'--assetsDir',unix(assets),'--assetIndex',asset_index,'--width','1280','--height','720','--graphicsBackend','vulkan','--quickPlaySingleplayer','rowing-world']
    (stage/('client-command-'+name+'.json')).write_text(json.dumps(command,indent=2),encoding='utf-8')
    (stage/('run_client_'+name+'.py')).write_text('import json,os\nfrom pathlib import Path\na=json.loads(Path("'+unix(stage/('client-command-'+name+'.json'))+'").read_text())\nos.chdir(a[a.index("--gameDir")+1])\nos.execv(a[0],a)\n',encoding='utf-8')
    (target/'jar-hashes-before.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (target/'mods').glob('*.jar')},indent=2),encoding='utf-8')
    print('Prepared',name,flush=True)
profile(sys.argv[1] if len(sys.argv)>1 else 'fresh1',tuple(sys.argv[2:]) or ('entity-model-features','entitytexturefeatures'))
