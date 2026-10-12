#!/usr/bin/env python3
"""Prepare two genuine packaged clients against an official vanilla dedicated server."""
from pathlib import Path
import argparse,hashlib,json,re,shutil,subprocess,sys,uuid
from prepare_native_runtime import ROOT,fetch,sha
QA=Path(__file__).resolve().parent
p=argparse.ArgumentParser()
for arg in ('repo','loader','name','fixture'):p.add_argument('--'+arg,required=True)
p.add_argument('--extra-mods');p.add_argument('--fresh-pack');p.add_argument('--reconnect-from');p.add_argument('--backend');p.add_argument('--loader-pin');p.add_argument('--server-mods',type=Path);p.add_argument('--vanilla-observer',action='store_true')
p.add_argument('--width',type=int,default=640);p.add_argument('--height',type=int,default=360)
a=p.parse_args()
if not re.fullmatch('[a-zA-Z0-9-]+',a.name):raise ValueError('Invalid pair name')
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
pair=ROOT/a.name;pair.mkdir(exist_ok=False);profiles={}
for role,user in [('rower','RowQA'),('observer','ObserverQA')]:
    run=a.name+'-'+role
    observer_vanilla=role=='observer' and a.vanilla_observer
    cmd=['python3',str(QA/'prepare_native_runtime.py'),'--repo',a.repo,'--loader','vanilla' if observer_vanilla else a.loader,'--name',run,'--fixture',a.fixture,'--width',str(a.width),'--height',str(a.height)]
    for key in ('extra_mods','fresh_pack','backend','loader_pin'):
        if getattr(a,key) and not observer_vanilla:cmd+=['--'+key.replace('_','-'),getattr(a,key)]
    subprocess.run(cmd,check=True,timeout=900)
    profile=ROOT/('client-'+run);profiles[role]=profile.name
    if role=='observer' and a.vanilla_observer:
        for jar in (profile/'mods').glob('seamless-rowing*.jar'):jar.unlink()
    cmd=json.loads((ROOT/('client-command-'+run+'.json')).read_text())
    cmd[cmd.index('--username')+1]=user
    identifier=bytearray(hashlib.md5(('OfflinePlayer:'+user).encode()).digest());identifier[6]=identifier[6]&15|48;identifier[8]=identifier[8]&63|128
    cmd[cmd.index('--uuid')+1]=uuid.UUID(bytes=bytes(identifier)).hex
    pos=cmd.index('--quickPlaySingleplayer');cmd[pos:pos+2]=['--quickPlayMultiplayer','127.0.0.1:25961']
    cmd.insert(1,'-Dqa.rowing.role='+role);cmd.insert(1,'-Dqa.rowing.lateTracking=true')
    (pair/(role+'.json')).write_text(json.dumps(cmd,indent=2))
snapshots=[{p.name:sha(p) for p in (ROOT/profile/'mods').glob('*.jar')} for profile in profiles.values()]
if not a.vanilla_observer and snapshots[0]!=snapshots[1]:raise RuntimeError('Pair preparation raced an artifact change; use a new frozen run')
if a.vanilla_observer and any('seamless-rowing' in name for name in snapshots[1]):raise RuntimeError('Mixed observer still has Rowing')
import tomllib
pins=tomllib.loads((Path(a.repo)/'gradle/libs.versions.toml').read_text())['versions'];mc=pins['minecraft']
info=json.loads((Path('/mnt/c/Users/derko/.gradle/caches/fabric-loom')/mc/'mojang_minecraft_info.json').read_text())
download=info['downloads']['server'];native=fetch(download['url'],ROOT/'runtimes'/('server-'+mc+'.jar'),download['sha1'])
server=pair/'server';server.mkdir();shutil.copy2(native,server/'server.jar')
server_args=None;server_mod_hashes={}
if a.server_mods:
    if a.loader!='neoforge' or not mc.startswith('26.'):raise ValueError('This explicit server fixture is for modern NeoForge')
    inputs=json.loads((a.server_mods/'manifest.json').read_text())
    if inputs['minecraft']!=mc or inputs['loader']!=a.loader:raise ValueError('Server integration matrix mismatch')
    pin=a.loader_pin or pins['neoforge']
    installer=Path('/opt/seamless-rowing-ports-qa/runtimes')/(mc+'-neoforge-'+pin)/'installer.jar'
    if not installer.is_file():raise ValueError('Expected official client installer runtime first')
    java='/usr/lib/jvm/java-'+pins['java']+'-openjdk-amd64/bin/java'
    with (server/'installer.log').open('w') as log:
        subprocess.run([java,'-Djava.awt.headless=true','-Xmx768M','-XX:ActiveProcessorCount=2','-jar',str(installer),'--installServer',str(server)],stdout=log,stderr=subprocess.STDOUT,check=True,timeout=900)
    arguments=list(server.glob('libraries/net/neoforged/neoforge/'+pin+'/unix_args.txt'))
    if len(arguments)!=1:raise ValueError('Missing official NeoForge server arguments')
    server_args=['@'+str(arguments[0].relative_to(server)),'--safeMode','nogui']
    (server/'mods').mkdir(exist_ok=True)
    for filename,expected in inputs['jars'].items():
        source=a.server_mods/filename
        if source.name!=filename or sha(source)!=expected:raise ValueError('Server integration checksum mismatch')
        shutil.copy2(source,server/'mods'/filename);server_mod_hashes[filename]=expected
world=Path(a.fixture)
if a.reconnect_from:
    original=(ROOT/a.reconnect_from).resolve()
    if original.parent!=ROOT or not (original/'pair-results.json').is_file():raise ValueError('Unverified reconnect source')
    evidence=json.loads((original/'pair-results.json').read_text())
    current=snapshots[0]
    product=next(filename for filename in current if filename.startswith('seamless-rowing-0.1.0+mc'))
    if evidence.get('pass') is not True or evidence.get('minecraft')!=mc or evidence.get('loader')!=a.loader or evidence.get('hashes',{}).get('rower',{}).get(product)!=current[product]:
        raise ValueError('Reconnect requires a passing baseline for the same current distribution')
    world=original/'server/rowing-world'
shutil.copytree(world,server/'rowing-world')
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25961\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nlevel-name=rowing-world\ngamemode=creative\ndifficulty=peaceful\nview-distance=4\nsimulation-distance=4\nmax-players=2\nspawn-protection=0\nenable-rcon=true\nrcon.port=25962\nrcon.password=owned-rowing-loopback-test\nsync-chunk-writes=false\n')
(pair/'metadata.json').write_text(json.dumps({'minecraft':mc,'loader':a.loader,'java':pins['java'],'profiles':profiles,'clientResolution':[a.width,a.height],'serverSha256':sha(server/'server.jar'),'serverArguments':server_args,'serverModHashes':server_mod_hashes,'reconnectFrom':a.reconnect_from,'vanillaObserver':a.vanilla_observer,'runtimeLoader':a.loader_pin or pins['fabric-loader' if a.loader=='fabric' else a.loader]},indent=2))
print('Prepared genuine native pair',pair,flush=True)
