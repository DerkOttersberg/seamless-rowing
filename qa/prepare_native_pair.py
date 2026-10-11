#!/usr/bin/env python3
"""Prepare two genuine packaged clients against an official vanilla dedicated server."""
from pathlib import Path
import argparse,hashlib,json,re,shutil,subprocess,sys,uuid
from prepare_native_runtime import ROOT,fetch,sha
QA=Path(__file__).resolve().parent
p=argparse.ArgumentParser()
for arg in ('repo','loader','name','fixture'):p.add_argument('--'+arg,required=True)
p.add_argument('--extra-mods');p.add_argument('--fresh-pack');p.add_argument('--reconnect-from');p.add_argument('--backend');p.add_argument('--loader-pin');p.add_argument('--vanilla-observer',action='store_true')
a=p.parse_args()
if not re.fullmatch('[a-zA-Z0-9-]+',a.name):raise ValueError('Invalid pair name')
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
pair=ROOT/a.name;pair.mkdir(exist_ok=False);profiles={}
for role,user in [('rower','RowQA'),('observer','ObserverQA')]:
    run=a.name+'-'+role
    cmd=['python3',str(QA/'prepare_native_runtime.py'),'--repo',a.repo,'--loader',a.loader,'--name',run,'--fixture',a.fixture]
    for key in ('extra_mods','fresh_pack','backend','loader_pin'):
        if getattr(a,key):cmd+=['--'+key.replace('_','-'),getattr(a,key)]
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
world=Path(a.fixture)
if a.reconnect_from:
    original=(ROOT/a.reconnect_from).resolve()
    if original.parent!=ROOT or not (original/'pair-results.json').is_file():raise ValueError('Unverified reconnect source')
    world=original/'server/rowing-world'
shutil.copytree(world,server/'rowing-world')
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25961\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nlevel-name=rowing-world\ngamemode=creative\ndifficulty=peaceful\nview-distance=4\nsimulation-distance=4\nmax-players=2\nspawn-protection=0\nenable-rcon=true\nrcon.port=25962\nrcon.password=owned-rowing-loopback-test\nsync-chunk-writes=false\n')
(pair/'metadata.json').write_text(json.dumps({'minecraft':mc,'loader':a.loader,'java':pins['java'],'profiles':profiles,'serverSha256':sha(server/'server.jar'),'reconnectFrom':a.reconnect_from,'vanillaObserver':a.vanilla_observer,'runtimeLoader':a.loader_pin or pins['fabric-loader' if a.loader=='fabric' else a.loader]},indent=2))
print('Prepared genuine native pair',pair,flush=True)
