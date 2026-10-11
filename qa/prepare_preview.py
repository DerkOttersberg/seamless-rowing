"""Prepare an owned preview instance from a copied QA fixture, never a user world."""
from pathlib import Path
import json, shutil, sys, hashlib

stage = Path(r'D:\SeamlessRowingQA-20261011')
repo = Path(__file__).resolve().parents[1]
run = sys.argv[1] if len(sys.argv)>1 else 'r1'
if not run.isalnum(): raise ValueError('Invalid run ID')
client = stage / ('client-'+run)
client.mkdir(exist_ok=True)
(client/'config').mkdir(exist_ok=True)
fixture = Path(r'D:\SeamlessDogsProduction-20261006\fabric-26.3-single-3\client')
world = client / 'saves' / 'rowing-world'
if not world.exists():
    shutil.copytree(fixture / 'saves' / 'dogs-world', world)
mods = client / 'mods'
mods.mkdir(exist_ok=True)
shutil.copy2(fixture / 'mods' / 'fabric-api-0.161.0+26.3.jar', mods)
version=next(line.split('=',1)[1] for line in (repo/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
shutil.copy2(repo / ('fabric/build/libs/seamless-rowing-'+version+'-fabric.jar'), mods)
shutil.copy2(repo / 'qa/build/libs/seamless-rowing-qa.jar', mods)
(client / 'options.txt').write_text('renderDistance:4\nsimulationDistance:5\nmaxFps:30\nvsync:false\npauseOnLostFocus:false\nguiScale:2\nfov:0.0\ncloudStatus:0\nparticles:2\nshowSubtitles:true\n', encoding='utf-8')
command = json.loads((stage / 'client-command.json').read_text())
command = [arg.replace('/client-r1', '/client-'+run).replace('SeamlessDogsQA','SeamlessRowingQA') for arg in command]
if len(sys.argv)>2:
    command.insert(1,'-Dqa.rowing.scenario='+sys.argv[2])
(stage / ('client-command-'+run+'.json')).write_text(json.dumps(command,indent=2))
launcher = stage / ('run_client_'+run+'.py')
launcher.write_text('import json,os\nfrom pathlib import Path\na=json.loads(Path("/mnt/d/SeamlessRowingQA-20261011/client-command-'+run+'.json").read_text())\nos.chdir(a[a.index("--gameDir")+1])\nos.execv(a[0],a)\n',encoding='utf-8')
(client / 'jar-hashes.json').write_text(json.dumps({jar.name:hashlib.sha256(jar.read_bytes()).hexdigest() for jar in mods.glob('*.jar')},indent=2))
print(f'Prepared isolated packaged client: {client}')
