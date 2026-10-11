"""Create two new QA clients and a vanilla server, using only owned fixture copies."""
from pathlib import Path
import json, shutil, subprocess, sys, uuid, hashlib
root=Path(r'D:\SeamlessRowingQA-20261011')
repo=Path(__file__).resolve().parents[1]
name=sys.argv[1] if len(sys.argv)>1 else 'pair1'
if not name.isalnum(): raise ValueError('Invalid pair ID')
pair=root/name
if pair.exists(): raise ValueError('Use a fresh pair ID')
pair.mkdir()
for role,user in [('rower','RowQA'),('observer','ObserverQA')]:
    run=name+role
    subprocess.run([sys.executable,str(repo/'qa/prepare_preview.py'),run],check=True)
    profile=root/('client-'+run)
    cmd=json.loads((root/('client-command-'+run+'.json')).read_text())
    cmd[cmd.index('--username')+1]=user
    cmd[cmd.index('--uuid')+1]=uuid.uuid3(uuid.NAMESPACE_DNS,'rowing-qa-'+user).hex
    p=cmd.index('--quickPlaySingleplayer');cmd[p:p+2]=['--quickPlayMultiplayer','127.0.0.1:25961']
    cmd.insert(1,'-Dqa.rowing.role='+role)
    (pair/(role+'.json')).write_text(json.dumps(cmd))
    # The marker belongs to this pair; one parent root can host only one current pair.
server=pair/'server';server.mkdir()
shutil.copytree(root/'client-r9/saves/rowing-world',server/'rowing-world')
shutil.copy2(Path(r'D:\SeamlessDogsProduction-20261006\fabric-26.3-server-1\server.jar'),server/'server.jar')
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25961\nonline-mode=false\nenforce-secure-profile=false\nwhite-list=false\nlevel-name=rowing-world\ngamemode=creative\ndifficulty=peaceful\nview-distance=4\nsimulation-distance=4\nmax-players=2\nspawn-protection=0\nenable-rcon=true\nrcon.port=25962\nrcon.password=owned-rowing-loopback-test\nsync-chunk-writes=false\n')
(pair/'metadata.json').write_text(json.dumps({'profiles':{r:'client-'+name+r for r in ('rower','observer')},'serverSha256':hashlib.sha256((server/'server.jar').read_bytes()).hexdigest()},indent=2))
print(pair)
