"""One-time native 1.20.1 scaffold migration from the 1.21.1 version branch."""
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1]
matrix=json.loads((R.parent/'seamless-rowing/qa-artifacts/integration-matrix/resolved.json').read_text())
p=R/'gradle/libs.versions.toml';s=p.read_text()
for key,value in {'minecraft':'1.20.1','java':'17','fabric-api':'0.92.12+1.20.1','forge':'1.20.1-47.4.26','modmenu':'7.2.2'}.items():s=re.sub(r'^'+key+r' = ".*"',key+' = "'+value+'"',s,flags=re.M)
p.write_text(s)
p=R/'gradle.properties';p.write_text(p.read_text().replace('mc1.21.1','mc1.20.1'))
p=R/'settings.gradle';p.write_text(p.read_text().replace(", 'neoforge'",'').replace(", 'qa-neoforge'",''))
p=R/'build.gradle';s=p.read_text().replace(", ':neoforge:remapJar'",'').replace("['fabric','forge','neoforge']","['fabric','forge']").replace(", ':neoforge:check'",'').replace(", ':neoforge:assemble'",'');p.write_text(s)
for loader in ('fabric','forge'):
    p=R/loader/'build.gradle';s=p.read_text()
    s=re.sub(r"\s*modCompileOnly\('maven.modrinth:player-animation-library:.*?\) \{ transitive=false \}",'',s)
    def version(m):
        project=m[1];cell=matrix[project]['cells'].get('1.20.1/'+loader)
        if not cell:raise ValueError('Missing native API '+project+'/'+loader)
        return "maven.modrinth:"+project+":"+cell['id']
    s=re.sub(r'maven.modrinth:([^:\s\']+):[^\']+',version,s);p.write_text(s)
for p in R.rglob('*.java'):
    if any(part in ('.gradle','build') for part in p.parts):continue
    s=p.read_text();s=s.replace('.getFirst()','.get(0)').replace('ResourceLocation.fromNamespaceAndPath(', 'new ResourceLocation(').replace('ResourceLocation.parse(', 'new ResourceLocation(')
    s=re.sub(r'(\w+)\.getLast\(\)',r'\1.get(\1.size()-1)',s)
    s=s.replace('player.getSkin().texture()','player.getSkinTextureLocation()')
    p.write_text(s)
for path in ('common/src/main/resources/seamlessrowing.mixins.json','fabric/src/main/resources/seamlessrowing.compat.mixins.json','forge/src/main/resources/seamlessrowing.compat.mixins.json','qa/src/main/resources/rowing-qa.mixins.json'):
    p=R/path;m=json.loads(p.read_text());m['compatibilityLevel']='JAVA_17';p.write_text(json.dumps(m,indent=2))
for path in ('common/src/main/resources/pack.mcmeta','qa/src/main/resources/pack.mcmeta'):
    p=R/path;m=json.loads(p.read_text());m['pack']['pack_format']=15;p.write_text(json.dumps(m,indent=2))
p=R/'fabric/src/main/resources/fabric.mod.json';m=json.loads(p.read_text());m['depends']['minecraft']='1.20.1';m['depends']['java']='>=17';m['depends']['fabric-api']='>=0.92.12';p.write_text(json.dumps(m,indent=2))
p=R/'qa/src/main/resources/fabric.mod.json';m=json.loads(p.read_text());m['depends']['minecraft']='1.20.1';p.write_text(json.dumps(m,indent=2))
for path in ('forge/src/main/resources/META-INF/mods.toml','qa-forge/src/main/resources/META-INF/mods.toml'):
    p=R/path;p.write_text(p.read_text().replace('[52,)','[47,)').replace('[1.21.1]','[1.20.1]'))
p=R/'loader-adapters/src/main/java/io/github/derkottersberg/seamlessrowing/integration/IntegrationServices.java';s=p.read_text()
s=re.sub(r'            if\(isModLoaded\("player_animation_library"\)\) \{.*?\n            \}', '',s,flags=re.S)
s=s.replace('2.0.4+1.21.1','1.0.2-rc1+1.20').replace('pal&&PalAdapter.active(player)||','');p.write_text(s)
(p.parent/'PalAdapter.java').unlink()
p=R/'qa/src/main/java/qa/rowing/RemoteScenario.java';s=p.read_text();s=re.sub(r'            if\(qa.rowing.QaPlatform.isModLoaded\("player_animation_library"\)\) \{.*?\n            \}', '',s,flags=re.S);p.write_text(s)
(p.parent/'PalProbe.java').unlink()
for path in ('forge/src/main/java/io/github/derkottersberg/seamlessrowing/forge/RowingForge.java','qa-forge/src/main/java/qa/rowing/platform/forge/QaForge.java'):
    p=R/path;s=p.read_text().replace('(FMLJavaModLoadingContext context)','()');s=s.replace('RowingForgeClient.initialize(context)','RowingForgeClient.initialize(FMLJavaModLoadingContext.get())');p.write_text(s)
print('Adapted 1.20.1 toolchain, metadata, native APIs and optional schemas.')
