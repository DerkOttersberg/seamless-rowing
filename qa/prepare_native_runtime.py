#!/usr/bin/env python3
"""Prepare exact packaged clients via official installers, in the owned disposable Linux stage."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import argparse,hashlib,json,os,re,shutil,subprocess,time,tomllib,urllib.request,uuid,zipfile

ROOT=Path('/mnt/d/SeamlessRowingPortsQA-20261011')
def sha(path,algorithm='sha256'):return hashlib.new(algorithm,path.read_bytes()).hexdigest()
def fetch(url,path,expected=None,algorithm='sha1'):
    if path.is_file() and (not expected or sha(path,algorithm)==expected):return path
    path.parent.mkdir(parents=True,exist_ok=True)
    request=urllib.request.Request(url,headers={'User-Agent':'SeamlessRowing isolated packaged verification'})
    with urllib.request.urlopen(request,timeout=120) as response:data=response.read()
    if expected and hashlib.new(algorithm,data).hexdigest()!=expected:raise ValueError('Checksum mismatch: '+url)
    temporary=path.with_suffix(path.suffix+'.download');temporary.write_bytes(data);temporary.replace(path)
    return path
def rules_allow(rules):
    if not rules:return True
    result=False
    for rule in rules:
        system=rule.get('os',{})
        if system.get('name','linux')!='linux':continue
        if 'arch' in system and not re.fullmatch(system['arch'],'x86_64'):continue
        if any(v!=(k=='has_custom_resolution') for k,v in rule.get('features',{}).items()):continue
        result=rule['action']=='allow'
    return result
def main():
    p=argparse.ArgumentParser();p.add_argument('--repo',type=Path,required=True);p.add_argument('--loader',choices=('fabric','forge','neoforge'),required=True)
    p.add_argument('--name',required=True);p.add_argument('--fixture',type=Path,required=True);p.add_argument('--extra-mods',type=Path)
    p.add_argument('--fresh-pack',type=Path);p.add_argument('--scenario',default='');p.add_argument('--realcamera',action='store_true')
    p.add_argument('--shader-pack',type=Path);p.add_argument('--backend',choices=('opengl','vulkan'));p.add_argument('--loader-pin');p.add_argument('--shader-profile',default='POTATO');p.add_argument('--width',type=int,default=1280);p.add_argument('--height',type=int,default=720)
    p.add_argument('--max-fps',type=int,default=30)
    args=p.parse_args();repo=args.repo.resolve();loader=args.loader
    if not 320<=args.width<=1920 or not 240<=args.height<=1080:raise ValueError('Invalid private-display resolution')
    if not 10<=args.max_fps<=240:raise ValueError('Invalid frame cap')
    if not re.fullmatch('[a-zA-Z0-9-]+',args.name):raise ValueError('Invalid run name')
    ownership=json.loads((ROOT/'ownership.json').read_text(encoding='utf-8-sig'))
    if ownership['distro']!='SeamlessRowingPortsQA-20261011':raise RuntimeError('Unowned test root')
    deadline=time.monotonic()+600
    while (ROOT/'artifacts-building').exists():
        if time.monotonic()>deadline:raise TimeoutError('Distribution artifact build did not finish')
        time.sleep(.5)
    pins=tomllib.loads((repo/'gradle/libs.versions.toml').read_text())['versions'];mc=pins['minecraft']
    if args.loader_pin:
        pattern=r'[0-9]+(?:\.[0-9]+)+' if loader=='fabric' else re.escape(mc)+(r'-[0-9]+(?:\.[0-9]+)+(?:-beta)?' if loader=='forge' else r'(?:\.[0-9]+)+(?:-beta)?')
        if not re.fullmatch(pattern,args.loader_pin):raise ValueError('Runtime loader pin does not match Minecraft line')
        pins['fabric-loader' if loader=='fabric' else loader]=args.loader_pin
    mod_version=next(l.split('=',1)[1] for l in (repo/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='))
    profile=ROOT/('client-'+args.name)
    if profile.exists():raise ValueError('Use a new run name')
    profile.mkdir();(profile/'mods').mkdir();(profile/'config').mkdir()
    gameplay=repo/loader/'build/libs'/f'seamless-rowing-{mod_version}-{loader}.jar'
    qa=repo/('qa' if loader=='fabric' else 'qa-'+loader)/'build/libs'/('seamless-rowing-qa.jar' if loader=='fabric' else 'seamless-rowing-qa-'+loader+'.jar')
    for jar in (gameplay,qa):shutil.copy2(jar,profile/'mods'/jar.name)
    if args.extra_mods:
        inputs=json.loads((args.extra_mods/'manifest.json').read_text())
        if inputs['minecraft']!=mc or inputs['loader']!=loader:raise ValueError('Integration matrix mismatch')
        for name,expected in inputs['jars'].items():
            jar=args.extra_mods/name
            if jar.name!=name or sha(jar)!=expected:raise ValueError('Integration hash mismatch')
            shutil.copy2(jar,profile/'mods'/name)
    shutil.copytree(args.fixture,profile/'saves/rowing-world')
    options=f'renderDistance:4\nsimulationDistance:5\nmaxFps:{args.max_fps}\nenableVsync:false\npauseOnLostFocus:false\nguiScale:2\nfov:0.0\ncloudStatus:0\nparticles:2\n'
    if args.fresh_pack:
        (profile/'resourcepacks').mkdir();shutil.copy2(args.fresh_pack,profile/'resourcepacks'/args.fresh_pack.name)
        options+='resourcePacks:'+json.dumps(['vanilla','file/'+args.fresh_pack.name])+'\n'
    (profile/'options.txt').write_text(options);(profile/'config/fml.toml').write_text('earlyWindowControl=false\n')
    if args.shader_pack:
        (profile/'shaderpacks').mkdir();shutil.copy2(args.shader_pack,profile/'shaderpacks'/args.shader_pack.name)
        with zipfile.ZipFile(args.shader_pack) as pack:
            properties=pack.read('shaders/shaders.properties').decode()
        prefix='profile.'+args.shader_profile+' '
        preset=next((line.strip().split('=',1)[1] for line in properties.splitlines() if line.strip().startswith(prefix)),None)
        if preset is None:raise ValueError('Shader preset not declared by pack')
        (profile/'shaderpacks'/(args.shader_pack.name+'.txt')).write_text('\n'.join(preset.split())+'\n')
        (profile/'config/iris.properties').write_text('enableShaders=true\nshaderPack='+args.shader_pack.name+'\n')
        (profile/'config/oculus.properties').write_text('enableShaders=true\nshaderPack='+args.shader_pack.name+'\n')
    info_path=Path('/mnt/c/Users/derko/.gradle/caches/fabric-loom')/mc/'mojang_minecraft_info.json'
    info=json.loads(info_path.read_text())
    previous=ROOT/'runtimes'/f'{mc}-{loader}-{pins["fabric-loader" if loader=="fabric" else loader]}'
    # Resource baking performs many small ZIP reads. Keep immutable inputs on Linux storage.
    runtime=Path('/opt/seamless-rowing-ports-qa/runtimes')/previous.name
    if not runtime.exists() and (previous/'installed-profile.json').is_file():
        for source in previous.rglob('*'):
            if source.is_file() and source.suffix in ('.jar','.json'):
                target=runtime/source.relative_to(previous);target.parent.mkdir(parents=True,exist_ok=True)
                shutil.copy2(source,target)
    runtime.mkdir(parents=True,exist_ok=True)
    (runtime/'launcher_profiles.json').write_text('{"profiles":{}}')
    version_dir=runtime/'versions'/mc;version_dir.mkdir(parents=True,exist_ok=True)
    (version_dir/(mc+'.json')).write_text(json.dumps(info))
    original=fetch(info['downloads']['client']['url'],version_dir/(mc+'.jar'),info['downloads']['client']['sha1'])
    java='/usr/lib/jvm/java-'+pins['java']+'-openjdk-amd64/bin/java'
    installed=runtime/'installed-profile.json'
    if installed.exists():modded=json.loads(installed.read_text())
    elif loader=='fabric':
        url=f'https://meta.fabricmc.net/v2/versions/loader/{mc}/{pins["fabric-loader"]}/profile/json'
        modded=json.loads(fetch(url,runtime/'fabric-profile.json').read_text());installed.write_text(json.dumps(modded))
    else:
        pin=pins[loader];domain,group=('https://maven.minecraftforge.net','net/minecraftforge/forge') if loader=='forge' else ('https://maven.neoforged.net/releases','net/neoforged/neoforge')
        installer=fetch(f'{domain}/{group}/{pin}/{loader}-{pin}-installer.jar',runtime/'installer.jar')
        with (runtime/'installer.log').open('w') as log:
            subprocess.run([java,'-Djava.awt.headless=true','-Xmx1G','-XX:ActiveProcessorCount=2','-jar',str(installer),'--installClient',str(runtime)],cwd=runtime,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=900)
        modded_path=next(path for path in runtime.glob('versions/*/*.json') if path.parent.name!=mc)
        modded=json.loads(modded_path.read_text());installed.write_text(json.dumps(modded))
    libraries={}
    for lib in info['libraries']+modded['libraries']:
        if rules_allow(lib.get('rules')):
            pieces=lib['name'].split(':');libraries[':'.join(pieces[:2]+pieces[3:])]=lib
    native=profile/'natives';native.mkdir()
    def library(lib):
        meta=lib.get('downloads',{}).get('artifact')
        if not meta:
            group,name,version,*classifier=lib['name'].split(':');suffix='-'+classifier[0] if classifier else ''
            path=f'{group.replace(".","/")}/{name}/{version}/{name}-{version}{suffix}.jar'
            meta={'path':path,'url':lib.get('url','https://libraries.minecraft.net/').rstrip('/')+'/'+path}
        path=fetch(meta.get('url',''),runtime/'libraries'/meta['path'],meta.get('sha1'))
        natives=lib.get('natives',{}).get('linux')
        if natives:
            n=lib['downloads']['classifiers'][natives.replace('${arch}','64')]
            npath=fetch(n['url'],runtime/'libraries'/n['path'],n.get('sha1'))
            extract(npath)
        extract(path);return str(path)
    def extract(path):
        with zipfile.ZipFile(path) as archive:
            for name in archive.namelist():
                if name.endswith('.so') and not any(a in name for a in ('aarch64','arm32','riscv')):(native/Path(name).name).write_bytes(archive.read(name))
    with ThreadPoolExecutor(max_workers=4) as pool:classpath=list(pool.map(library,libraries.values()))
    inherited=original
    if any('${version_name}.jar' in a for a in modded.get('arguments',{}).get('jvm',[]) if isinstance(a,str)):
        inherited=runtime/'versions'/modded['id']/(modded['id']+'.jar')
        if not inherited.is_file() or sha(inherited)!=sha(original):shutil.copy2(original,inherited)
    classpath.append(str(inherited))
    assets=Path('/mnt/c/Users/derko/.gradle/caches/fabric-loom/assets');index=info['assetIndex']['id']
    if not (assets/'indexes'/(index+'.json')).is_file():index=mc+'-'+index
    if not (assets/'indexes'/(index+'.json')).is_file():raise ValueError('Prepare official asset index first')
    if loader=='fabric' and not any((profile/'mods').glob('fabric-api-*.jar')):
        api=pins['fabric-api'];fetch(f'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{api}/fabric-api-{api}.jar',profile/'mods'/f'fabric-api-{api}.jar')
    materials=Path('/opt/seamless-rowing-ports-qa/inputs')/profile.name
    # Minecraft rejects external symlinks for resource packs. Keep that native
    # validation intact and leave resource-pack ZIPs as ordinary profile files.
    for folder in ('mods','shaderpacks'):
        if not (profile/folder).is_dir():continue
        for source in (profile/folder).iterdir():
            if source.suffix not in ('.jar','.zip'):continue
            target=materials/folder/source.name;target.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(source,target)
            if sha(source)!=sha(target):raise ValueError('Linux input copy changed bytes')
            source.unlink();source.symlink_to(target)
    offline=bytearray(hashlib.md5(b'OfflinePlayer:RowQA').digest());offline[6]=offline[6]&15|48;offline[8]=offline[8]&63|128
    repl={'auth_player_name':'RowQA','auth_uuid':uuid.UUID(bytes=bytes(offline)).hex,'auth_access_token':'0','auth_xuid':'0','clientid':'0','user_type':'legacy','user_properties':'{}',
          'version_name':modded['id'],'version_type':'release','game_directory':str(profile),'assets_root':str(assets),'assets_index_name':index,'natives_directory':str(native),
          'launcher_name':'SeamlessRowingQA','launcher_version':'1','classpath':':'.join(classpath),'classpath_separator':':','library_directory':str(runtime/'libraries'),'resolution_width':str(args.width),'resolution_height':str(args.height)}
    def expand(s):return re.sub(r'\$\{([^}]+)\}',lambda m:repl[m[1]],s)
    def arguments(data,kind):
        result=[]
        for arg in data.get('arguments',{}).get(kind,[]):
            if isinstance(arg,str):result.append(expand(arg))
            elif rules_allow(arg.get('rules')):result.extend(expand(s) for s in ([arg['value']] if isinstance(arg['value'],str) else arg['value']))
        return result
    jvm=arguments(info,'jvm')+arguments(modded,'jvm')
    if '-cp' not in jvm and '-classpath' not in jvm:jvm+=['-cp',':'.join(classpath)]
    command=[java,'-Xmx768M','-XX:ActiveProcessorCount=2','-Dqa.rowing.scenario='+args.scenario]
    if args.max_fps!=30:command+=['-Dqa.rowing.cadence=true']
    if args.shader_pack:command+=['-Dqa.rowing.shaderCoverage=true']
    if args.realcamera:command+=['-Dqa.rowing.realcamera=true']
    command+=jvm+[modded['mainClass']]+arguments(info,'game')+arguments(modded,'game')+['--quickPlaySingleplayer','rowing-world']
    if mc.startswith('26.'):command+=['--graphicsBackend',args.backend or 'vulkan']
    (ROOT/('client-command-'+args.name+'.json')).write_text(json.dumps(command,indent=2))
    run=ROOT/('run_client_'+args.name+'.py');run.write_text('import json,os,time\nfrom pathlib import Path\na=json.loads(Path("'+str(ROOT/('client-command-'+args.name+'.json'))+'").read_text())\nos.chdir(a[a.index("--gameDir")+1])\nwhile Path("'+str(ROOT/'artifacts-building')+'").exists():time.sleep(.5)\nos.execv(a[0],a)\n')
    hashes={jar.name:sha(jar) for jar in (profile/'mods').glob('*.jar')};(profile/'jar-hashes-before.json').write_text(json.dumps(hashes,indent=2))
    print('Prepared',mc,loader,profile,flush=True)
if __name__=='__main__':main()
