"""Bounded real two-client run, called inside the private-display wrapper."""
from pathlib import Path
import hashlib, json, os, socket, struct, subprocess, sys, time
root=Path('/mnt/d/SeamlessRowingQA-20261011')
name=sys.argv[1]
if not name.isalnum() or not os.environ.get('DISPLAY','').startswith(':') or os.environ.get('DISPLAY')==':0' or os.environ.get('WAYLAND_DISPLAY'): raise RuntimeError('Isolation required')
pair=root/name
metadata=json.loads((pair/'metadata.json').read_text())
before={}
for role in ('rower','observer'):
    profile=root/metadata['profiles'][role]
    before[role]={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (profile/'mods').glob('*.jar')}
    (profile/'jar-hashes-final.json').write_text(json.dumps(before[role],indent=2))
marker=root/'mp-ready'
if marker.exists(): marker.unlink()
done=root/'mp-rower-done'
if done.exists():done.unlink()
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
try:
    path=pair/'server-console.log';out=path.open('w');logs.append(out)
    server=subprocess.Popen(['/opt/rowing-java-test/bin/java','-Xmx768M','-XX:ActiveProcessorCount=2','-jar','server.jar','--safeMode','nogui'],cwd=pair/'server',stdout=out,stderr=subprocess.STDOUT);processes.append(server)
    wait(server,path,'Done (')
    clients=[]
    for role in ('rower','observer'):
        cmd=json.loads((pair/(role+'.json')).read_text());path=pair/(role+'-console.log');out=path.open('w');logs.append(out)
        display=111 if role=='rower' else 112
        child=subprocess.Popen(['xvfb-run','-n',str(display),'-s','-screen 0 1280x720x24 -nolisten tcp -noreset']+cmd,cwd=cmd[cmd.index('--gameDir')+1],stdout=out,stderr=subprocess.STDOUT);processes.append(child);clients.append(child)
    wait(server,pair/'server-console.log','ObserverQA joined the game')
    wait(server,pair/'server-console.log','RowQA joined the game')
    metadata=json.loads((pair/'metadata.json').read_text())
    deadline=time.monotonic()+120
    while not all((root/metadata['profiles'][role]/'mp-client-ready').exists() for role in ('rower','observer')):
        if time.monotonic()>deadline or any(p.poll() is not None for p in clients):raise RuntimeError('Native client world not ready')
        time.sleep(.5)
    for cmd in ['kill @e[type=!minecraft:player]','time set 6000','weather clear','gamemode creative RowQA','gamemode spectator ObserverQA','tp RowQA 0 -57 0 0 40','tp ObserverQA 5 -55 9','summon minecraft:oak_boat 0 -58.1 0 {Tags:["rowingQA"]}','ride RowQA mount @e[tag=rowingQA,limit=1]','item replace entity RowQA weapon.mainhand with minecraft:bow','give RowQA minecraft:arrow 64']:
        print(cmd,':',rcon(cmd),flush=True)
    marker.write_text(name)
    deadline=time.monotonic()+480
    while any(p.poll() is None for p in clients):
        if server.poll() is not None or time.monotonic()>deadline: raise RuntimeError('Pair timed out')
        time.sleep(1)
    results={}
    metadata=json.loads((pair/'metadata.json').read_text())
    for role,p in zip(('rower','observer'),clients):
        file=root/metadata['profiles'][role]/'rowing-multiplayer.json'
        if p.returncode or not file.exists(): raise RuntimeError(role+' did not finish')
        results[role]=json.loads(file.read_text())
        actual={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (root/metadata['profiles'][role]/'mods').glob('*.jar')}
        if actual!=before[role]:raise RuntimeError('Client JARs changed during test: '+role)
    (pair/'pair-results.json').write_text(json.dumps({'serverWithoutMods':True,'results':results,'pass':all(r['pass'] for r in results.values())},indent=2))
    print('ROWING_PAIR_COMPLETE',results,flush=True)
    print(rcon('save-all flush'));print(rcon('stop'));server.wait(timeout=90)
finally:
    for p in processes:
        if p.poll() is None: p.terminate()
    for p in processes:
        try:p.wait(timeout=20)
        except subprocess.TimeoutExpired:p.kill();p.wait(timeout=10)
    for log in logs:log.close()
    if marker.exists():marker.unlink()
    if done.exists():done.unlink()
