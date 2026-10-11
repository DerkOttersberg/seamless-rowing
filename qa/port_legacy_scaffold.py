"""One-time 1.21.1 scaffold migration; native rendering implementations are reviewed separately."""
from pathlib import Path
import json,re,subprocess
R=Path(__file__).resolve().parents[1]
assert subprocess.check_output(['git','branch','--show-current'],cwd=R,text=True).strip()=='1.21.1'
def write(path,s):
    p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf8')
def change(path,replacements):
    s=(R/path).read_text()
    for a,b in replacements:s=s.replace(a,b)
    write(path,s)
change('gradle/libs.versions.toml',[('"26.3"','"1.21.1"'),('java = "25"','java = "21"'),('0.161.0+26.3','0.116.17+1.21.1'),
       ('26.3-66.0.9','1.21.1-52.1.16'),('26.3.0.48-beta','21.1.255'),('21.0.0','11.0.5'),('dev.architectury.loom-no-remap','dev.architectury.loom')])
change('gradle.properties',[('mc26.3','mc1.21.1')])
common=(R/'common/build.gradle').read_text().replace('loom-no-remap','loom').replace('minecraft libs.minecraft','minecraft libs.minecraft\n    mappings loom.officialMojangMappings()')
write('common/build.gradle',common+"\nloom { accessWidenerPath=rootProject.file('common/src/main/resources/seamlessrowing.accesswidener') }\n")
matrix=json.loads((R.parent/'seamless-rowing/qa-artifacts/integration-matrix/resolved.json').read_text())
for loader in ('fabric','forge','neoforge'):
    dependencies='    minecraft libs.minecraft\n    mappings loom.officialMojangMappings()\n'
    dependencies+=('    modImplementation libs.fabric.loader\n    modImplementation libs.fabric.api\n    modCompileOnly libs.modmenu\n' if loader=='fabric' else '    '+('forge' if loader=='forge' else 'neoForge')+' libs.'+loader+'\n')
    for project in ('entity-model-features','entitytexturefeatures','first-person-model','not-enough-animations','real-camera','player-animation-library','playeranimator'):
        cell=matrix[project]['cells'].get('1.21.1/'+loader)
        if cell:dependencies+="    modCompileOnly('maven.modrinth:"+project+':'+cell['id']+"') { transitive=false }\n"
    s="plugins { id 'dev.architectury.loom' }\nsourceSets.main {\n    java.srcDir rootProject.file('common/src/main/java')\n    java.srcDir rootProject.file('loader-adapters/src/main/java')\n    resources.srcDir rootProject.file('common/src/main/resources')\n}\ndependencies {\n"+dependencies+"}\n"
    s+="loom {\n    accessWidenerPath=rootProject.file('common/src/main/resources/seamlessrowing.accesswidener')\n    mixin { useLegacyMixinAp=true; defaultRefmapName='seamlessrowing.refmap.json' }\n    mods { seamlessrowing { sourceSet sourceSets.main } }\n}\n"
    if loader!='fabric':s+="loom."+('forge' if loader=='forge' else 'neoForge')+".convertAccessWideners(tasks.named('remapJar'))\n"
    metadata='fabric.mod.json' if loader=='fabric' else 'META-INF/'+('mods.toml' if loader=='forge' else 'neoforge.mods.toml')
    s+="processResources { inputs.property 'version',project.version; filesMatching('"+metadata+"') { expand version:project.version } }\n"
    s+="tasks.named('jar') {\n    from rootProject.file('LICENSE')\n    duplicatesStrategy=DuplicatesStrategy.EXCLUDE\n    manifest { attributes 'MixinConfigs':'seamlessrowing.mixins.json,seamlessrowing.compat.mixins.json' }\n    archiveFileName=\"${rootProject.archives_base_name}-${project.version}-"+loader+"-dev.jar\"\n}\n"
    s+="tasks.named('remapJar') { archiveFileName=\"${rootProject.archives_base_name}-${project.version}-"+loader+".jar\" }\n"
    s+="tasks.named('remapSourcesJar') { archiveFileName=\"${rootProject.archives_base_name}-${project.version}-"+loader+"-sources.jar\" }\n"
    write(loader+'/build.gradle',s)
change('build.gradle',[("dependsOn ':fabric:jar', ':forge:jar', ':neoforge:jar'","dependsOn ':fabric:remapJar', ':forge:remapJar', ':neoforge:remapJar'"),("tasks.named('jar').get().archiveFile","tasks.named('remapJar').get().archiveFile")])
metadata=json.loads((R/'fabric/src/main/resources/fabric.mod.json').read_text())
metadata['depends'].update({'minecraft':'1.21.1','java':'>=21','fabric-api':'>=0.116.17 <0.117.0'})
metadata['suggests']['modmenu']='>=11.0.5 <12.0.0';metadata['accessWidener']='seamlessrowing.accesswidener'
write('fabric/src/main/resources/fabric.mod.json',json.dumps(metadata,indent=2))
for loader in ('forge','neoforge'):
    p=loader+'/src/main/resources/META-INF/'+('mods.toml' if loader=='forge' else 'neoforge.mods.toml')
    change(p,[('[66,)','[52,)'),('[66.0.9,67)','[52.1.16,53)'),('[26.3.0.48-beta,26.4)','[21.1.255,22)'),('[26.3]','[1.21.1]'),('[25,)','[21,)')])
    if loader=='neoforge':write(p,(R/p).read_text()+'\n[[accessTransformers]]\nfile="META-INF/accesstransformer.cfg"\n')
write('common/src/main/resources/seamlessrowing.accesswidener',
    'accessWidener v2 named\naccessible class net/minecraft/client/model/geom/ModelPart$Polygon\naccessible class net/minecraft/client/model/geom/ModelPart$Vertex\naccessible field net/minecraft/client/model/geom/ModelPart$Cube polygons [Lnet/minecraft/client/model/geom/ModelPart$Polygon;\n')
for tree in ('common/src/main/java','loader-adapters/src/main/java','fabric/src/main/java'):
    for p in (R/tree).rglob('*.java'):
        s=p.read_text().replace('net.minecraft.client.model.player.PlayerModel','net.minecraft.client.model.PlayerModel')
        s=s.replace('net.minecraft.world.entity.vehicle.boat.AbstractBoat','net.minecraft.world.entity.vehicle.Boat')
        s=s.replace('AbstractBoat','Boat').replace('net.minecraft.resources.Identifier','net.minecraft.resources.ResourceLocation').replace('Identifier.','ResourceLocation.')
        s=s.replace('mc.gui.screen()','mc.screen').replace('mc.setScreenAndShow(','mc.setScreen(').replace('minecraft.setScreenAndShow(','minecraft.setScreen(')
        s=s.replace('player.isSwinging()','player.swinging').replace('player.getCurrentSwing().hand()','player.swingingArm')
        s=s.replace('player.getYRot(partial)','player.getViewYRot(partial)').replace('boat.getYRot(partial)','net.minecraft.util.Mth.rotLerp(partial,boat.yRotO,boat.getYRot())')
        s=s.replace('stack.rotateDegrees(Axis.YP,180-net.minecraft.util.Mth.rotLerp(partial,boat.yRotO,boat.getYRot()));','stack.mulPose(Axis.YP.rotationDegrees(180-net.minecraft.util.Mth.rotLerp(partial,boat.yRotO,boat.getYRot())));')
        s=s.replace('stack.rotateDegrees(Axis.XP,(float)Math.sin(hurt)*hurt*damage/10*boat.getHurtDir());','stack.mulPose(Axis.XP.rotationDegrees((float)Math.sin(hurt)*hurt*damage/10*boat.getHurtDir()));')
        s=s.replace('stack.rotate(new Quaternionf()','stack.mulPose(new Quaternionf()').replace('stack.rotateDegrees(Axis.YP,90);','stack.mulPose(Axis.YP.rotationDegrees(90));')
        s=s.replace('.getFirst()','.get(0)').replace('EntityRenderer<?,?>','EntityRenderer<?>')
        p.write_text(s,encoding='utf8')
for name in ('ArmMesh','TexturedSurface','TorsoSurface'):
    p=R/'common/src/main/java/io/github/derkottersberg/seamlessrowing/client'/f'{name}.java'
    s=p.read_text()
    s=s.replace('.vertices()','.vertices').replace('.normal()','.normal')
    for v in ('v','a','b'):
        for axis in ('x','y','z'):s=s.replace(v+'.'+axis+'()',v+'.pos.'+axis)
        for axis in ('u','v'):s=s.replace(v+'.'+axis+'()',v+'.'+axis)
    s=re.sub(r'v\[(\d)\]\.([xyz])\(\)',r'v[\1].pos.\2',s)
    s=s.replace('input.get(0).y()','input.get(0).pos.y').replace('new ModelPart.Polygon(','NativeMesh.face(')
    p.write_text(s,encoding='utf8')
for p in (R/'common/src/main/java/io/github/derkottersberg/seamlessrowing/mixin').glob('*.java'):
    if p.stem!='ModelPartAccess':p.unlink()
for name in ('HandsState','BoatState','RowingArmorModel'):
    (R/'common/src/main/java/io/github/derkottersberg/seamlessrowing/client'/f'{name}.java').unlink()
for p in ('common/src/main/resources/pack.mcmeta','qa/src/main/resources/pack.mcmeta'):
    write(p,'{"pack":{"pack_format":34,"description":"Seamless Rowing client resources"}}\n')
print('Legacy scaffold migrated; add native immediate rendering adapters before compiling.')
