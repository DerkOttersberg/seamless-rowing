"""Refresh only never-launched owned profiles while the launch barrier is present."""
from pathlib import Path
import hashlib,json,shutil,subprocess,time
ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
BASE=Path('/mnt/c/Users/derko/Desktop/minecraft')
assert (ROOT/'artifacts-building').is_file()
assert json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))['distro']=='SeamlessRowingPortsQA-20261011'
processes=subprocess.check_output(['ps','-eo','args'],text=True)
changes=[]
for profile in ROOT.glob('client-*'):
    if not profile.is_dir() or (profile/'logs/latest.log').exists() or str(profile) in processes:continue
    entry={'profile':profile.name,'changes':[]}
    for product in (profile/'mods').glob('seamless-rowing-0.1.0+mc*.jar'):
        mc=product.name.split('+mc')[1].rsplit('-',1)[0];loader=product.name.rsplit('-',1)[1][:-4]
        repo=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc))
        source=repo/loader/'build/libs'/product.name;target=product.resolve()
        assert target.is_relative_to(Path('/opt/seamless-rowing-ports-qa/inputs')) or target.is_relative_to(ROOT)
        old=hashlib.sha256(target.read_bytes()).hexdigest();new=hashlib.sha256(source.read_bytes()).hexdigest()
        if old!=new:shutil.copy2(source,target);entry['changes'].append({'productJar':product.name,'old':old,'new':new})
    for pack in (profile/'resourcepacks').glob('*.zip'):
        if pack.is_symlink():
            source=pack.resolve();assert source.is_relative_to(Path('/opt/seamless-rowing-ports-qa/inputs'))
            data=source.read_bytes();pack.unlink();pack.write_bytes(data)
            entry['changes'].append({'resourcePackOrdinaryFile':pack.name,'sha256':hashlib.sha256(data).hexdigest()})
    for jar in (profile/'mods').glob('seamless-rowing-qa*.jar'):
        product=next((profile/'mods').glob('seamless-rowing-0.1.0+mc*.jar'),None)
        if not product:continue
        mc=product.name.split('+mc')[1].rsplit('-',1)[0];loader=product.name.rsplit('-',1)[1][:-4]
        repo=BASE/('seamless-rowing'+('' if mc=='26.3' else '-'+mc));module='qa' if loader=='fabric' else 'qa-'+loader
        source=repo/module/'build/libs'/jar.name
        target=jar.resolve();assert target.is_relative_to(Path('/opt/seamless-rowing-ports-qa/inputs')) or target.is_relative_to(ROOT)
        old=hashlib.sha256(target.read_bytes()).hexdigest();new=hashlib.sha256(source.read_bytes()).hexdigest()
        if old!=new:shutil.copy2(source,target);entry['changes'].append({'qaJar':jar.name,'old':old,'new':new})
    if entry['changes']:
        manifest=profile/'jar-hashes-before.json'
        if manifest.exists():manifest.write_text(json.dumps({j.name:hashlib.sha256(j.read_bytes()).hexdigest() for j in (profile/'mods').glob('*.jar')},indent=2))
        changes.append(entry)
(ROOT/('pending-artifact-repair-'+str(int(time.time()))+'.json')).write_text(json.dumps(changes,indent=2))
print('Repaired',len(changes),'never-launched profiles')
