"""Bounded real two-client run, called inside the private-display wrapper."""
from pathlib import Path
import hashlib, json, os, signal, socket, struct, subprocess, sys, time
root=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
name=sys.argv[1]
while (root/'artifacts-building').exists():time.sleep(.5)
if not name.replace('-','').isalnum() or not os.environ.get('DISPLAY','').startswith(':') or os.environ.get('DISPLAY')==':0' or os.environ.get('WAYLAND_DISPLAY'): raise RuntimeError('Isolation required')
pair=root/name
metadata=json.loads((pair/'metadata.json').read_text())
before={}
for role in ('rower','observer'):
    profile=root/metadata['profiles'][role]
    before[role]={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (profile/'mods').glob('*.jar')}
    (profile/'jar-hashes-final.json').write_text(json.dumps(before[role],indent=2))
marker=root/'mp-ready'
done=root/'mp-rower-done'
for filename in ('mp-ready','mp-rower-done','mp-rowing-started','mp-observer-ready','mp-passenger-request'):
    path=root/filename
    if path.exists():path.unlink()
def recv_exact(c,n):
    data=b''
    while len(data)<n:
        part=c.recv(n-len(data))
        if not part: raise RuntimeError('Closed RCON')
        data+=part
    return data
def rcon(command):
    with socket.create_connection(('127.0.0.1',25962),timeout=15) as c:
        c.settimeout(15)
        def send(i,k,s):
            b=struct.pack('<ii',i,k)+s.encode()+b'\0\0';c.sendall(struct.pack('<i',len(b))+b)
            length=struct.unpack('<i',recv_exact(c,4))[0]
            if not 10<=length<1048576: raise RuntimeError('Invalid packet')
            data=recv_exact(c,length)
            if struct.unpack('<i',data[:4])[0]!=i: raise RuntimeError('RCON rejected request')
            return data[8:-2].decode()
        send(100,3,'owned-rowing-loopback-test');return send(101,2,command)
def wait(p,log,mark,seconds=240):
    deadline=time.monotonic()+seconds
    while mark not in log.read_text(errors='replace'):
        if p.poll() is not None or time.monotonic()>deadline: raise RuntimeError('Missing '+mark+' in '+str(log))
        time.sleep(1)
processes=[];logs=[]
def terminated(signum,frame):raise SystemExit(128+signum)
signal.signal(signal.SIGTERM,terminated)
try:
    path=pair/'server-console.log';out=path.open('w');logs.append(out)
    server_args=metadata.get('serverArguments') or ['-jar','server.jar','--safeMode','nogui']
    server=subprocess.Popen(['/usr/lib/jvm/java-'+metadata['java']+'-openjdk-amd64/bin/java','-Xmx768M','-XX:ActiveProcessorCount=2']+server_args,cwd=pair/'server',stdout=out,stderr=subprocess.STDOUT,start_new_session=True);processes.append(server)
    wait(server,path,'Done (')
    clients=[]
    def launch(role):
        cmd=json.loads((pair/(role+'.json')).read_text());path=pair/(role+'-console.log');out=path.open('w');logs.append(out)
        child=subprocess.Popen(['xvfb-run','-a','-s','-screen 0 1280x720x24 -nolisten tcp -noreset']+cmd,cwd=cmd[cmd.index('--gameDir')+1],stdout=out,stderr=subprocess.STDOUT,start_new_session=True);processes.append(child);clients.append(child)
        return child
    rower=launch('rower')
    wait(rower,pair/'server-console.log','RowQA joined the game')
    deadline=time.monotonic()+120
    while not (root/metadata['profiles']['rower']/'mp-client-ready').exists():
        if time.monotonic()>deadline or rower.poll() is not None:raise RuntimeError('Native rower world not ready')
        time.sleep(.5)
    boat='minecraft:oak_boat' if metadata['minecraft'].startswith('26.') else 'minecraft:boat'
    for cmd in ['kill @e[type=!minecraft:player]','time set 6000','weather clear','gamemode creative RowQA','tp RowQA 0 -57 0 0 40','summon '+boat+' 0 -58.1 0 {Type:"oak",Tags:["rowingQA"]}','ride RowQA mount @e[tag=rowingQA,limit=1]','item replace entity RowQA weapon.mainhand with minecraft:bow','give RowQA minecraft:arrow 64']:
        print(cmd,':',rcon(cmd),flush=True)
    marker.write_text(name)
    deadline=time.monotonic()+120
    while not (root/'mp-rowing-started').exists():
        if time.monotonic()>deadline or rower.poll() is not None:raise RuntimeError('Rower never established a native stroke')
        time.sleep(.5)
    observer=launch('observer')
    wait(observer,pair/'server-console.log','ObserverQA joined the game')
    print(rcon('gamemode spectator ObserverQA'),flush=True)
    print(rcon('tp ObserverQA RowQA'),flush=True)
    mixed=metadata.get('vanillaObserver',False)
    if mixed:(root/'mp-observer-ready').write_text('native unmodified client joined')
    deadline=time.monotonic()+480
    passenger_mounted=False
    while any(p.poll() is None for p in (clients[:1] if mixed else clients)):
        if mixed and observer.poll() is not None:raise RuntimeError('Unmodified observer disconnected during rowing')
        if (root/'mp-passenger-request').exists() and not passenger_mounted:
            print(rcon('gamemode creative ObserverQA'),flush=True)
            print(rcon('ride ObserverQA mount @e[tag=rowingQA,limit=1]'),flush=True)
            passenger_mounted=True
        if server.poll() is not None or time.monotonic()>deadline: raise RuntimeError('Pair timed out')
        time.sleep(1)
    results={};geometry={}
    metadata=json.loads((pair/'metadata.json').read_text())
    mixed_mount=rcon('execute as ObserverQA on vehicle run data get entity @s UUID') if mixed else None
    for role,p in zip(('rower','observer'),clients):
        if role=='observer' and mixed:
            results[role]={'pass':observer.poll() is None and passenger_mounted and 'has the following entity data' in mixed_mount,'hasNoRowingOrQaMod':True,'nativeBoatMountResult':mixed_mount};geometry[role]={};continue
        file=root/metadata['profiles'][role]/'rowing-multiplayer.json'
        if p.returncode or not file.exists(): raise RuntimeError(role+' did not finish')
        results[role]=json.loads(file.read_text())
        geometry[role]={p.name:json.loads(p.read_text()) for p in (root/metadata['profiles'][role]).glob('rowing-*.json') if p.name!='rowing-multiplayer.json'}
        actual={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (root/metadata['profiles'][role]/'mods').glob('*.jar')}
        if actual!=before[role]:raise RuntimeError('Client JARs changed during test: '+role)
    passed=all(r['pass'] for r in results.values()) and all(geometry[role].get(report,{}).get('pass') is True for role in (('rower',) if mixed else results) for report in ('rowing-anatomy.json','rowing-surface.json','rowing-native-surfaces.json'))
    server_mods=metadata.get('serverModHashes',{})
    (pair/'pair-results.json').write_text(json.dumps({'serverWithoutMods':not bool(server_mods),'serverWithoutRowing':not any('seamless-rowing' in filename for filename in server_mods),'serverModHashes':server_mods,'lateTrackingDuringEstablishedStroke':True,'mixedClientInstallation':mixed,'minecraft':metadata['minecraft'],'loader':metadata['loader'],'reconnectFromSavedServer':metadata.get('reconnectFrom'),'hashes':before,'results':results,'geometry':geometry,'pass':passed},indent=2))
    print('ROWING_PAIR_COMPLETE',results,flush=True)
    print(rcon('save-all flush'));print(rcon('stop'));server.wait(timeout=90)
finally:
    for p in processes:
        try:os.killpg(p.pid,signal.SIGTERM)
        except ProcessLookupError:pass
    for p in processes:
        try:p.wait(timeout=20)
        except subprocess.TimeoutExpired:
            try:os.killpg(p.pid,signal.SIGKILL)
            except ProcessLookupError:pass
            p.wait(timeout=10)
    for log in logs:log.close()
    for filename in ('mp-ready','mp-rower-done','mp-rowing-started','mp-observer-ready','mp-passenger-request'):
        path=root/filename
        if path.exists():path.unlink()
