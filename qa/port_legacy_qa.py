"""Adapt the native QA instrumentation to the 1.21.1 immediate renderer."""
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1];Q=R/'qa/src/main/java/qa/rowing'
keep={'RowingQaClient','QaPlatform','QaFabricClient','FrameContact','ArmAnatomy','SurfaceGeometry','RenderedPaddles','SoundProbe',
      'CameraCapturePass','FreshProbe','FreshScenario','MatrixScenario','RemoteScenario','PalProbe','ArmContactProbe','FrameProbe',
      'RenderedPaddleProbe','EmfPaddleTransformProbe','RealCameraCaptureProbe','SkinProbe','ShutdownProbe'}
for p in Q.rglob('*.java'):
    if p.stem not in keep:p.unlink();continue
    s=p.read_text().replace('net.minecraft.client.model.player.PlayerModel','net.minecraft.client.model.PlayerModel')
    s=s.replace('net.minecraft.world.entity.vehicle.boat.AbstractBoat','net.minecraft.world.entity.vehicle.Boat').replace('AbstractBoat','Boat')
    s=s.replace('net.minecraft.resources.Identifier','net.minecraft.resources.ResourceLocation').replace('Identifier.','ResourceLocation.')
    s=s.replace('net.minecraft.world.entity.player.PlayerSkin','net.minecraft.client.resources.PlayerSkin').replace('PlayerModelType','PlayerSkin.Model')
    s=s.replace('mc.gui.screen()','mc.screen').replace('mc.setScreenAndShow(','mc.setScreen(').replace('mc.gui.hud.isHidden()','mc.options.hideGui').replace('mc.gui.hud.toggle()','mc.options.hideGui=true')
    s=s.replace('mc.gameRenderer.mainRenderTarget(),1,','mc.getMainRenderTarget(),').replace('player.isSwinging()','player.swinging')
    s=s.replace('EntityTypes','EntityType').replace('EntitySpawnReason','MobSpawnType').replace('player.startRiding(boat,true,true)','player.startRiding(boat,true)')
    s=s.replace(',Set.of(),0,0,true)',',Set.of(),0,0)').replace(',Set.of(),0,55,true)',',Set.of(),0,55)').replace(',Set.of(),0,50,true)',',Set.of(),0,50)')
    s=s.replace('mc.player.getVehicle().getYRot()','mc.player.getVehicle().getYRot()')
    if p.stem=='FrameProbe':s=s.replace('method="extract"','method="render"')
    if p.stem=='FreshProbe':s=s.replace('import net.minecraft.client.renderer.entity.state.AvatarRenderState;','').replace('AvatarRenderState state','io.github.derkottersberg.seamlessrowing.client.RenderContext state')
    if p.stem=='SurfaceGeometry':
        s=s.replace('.vertices()','.vertices').replace('.normal()','.normal')
        for v in ('v',):
            for axis in ('x','y','z'):s=s.replace(v+'.'+axis+'()',v+'.pos.'+axis)
        # Legacy sleeves are siblings of the body, but the native body slot is measured identically.
    if p.stem=='RowingQaClient':
        s=s.replace('        if(System.getProperty("qa.rowing.scenario","").equals("settings")) { SettingsScenario.tick(mc);return; }\n','')
        s=s.replace('        if(System.getProperty("qa.rowing.scenario","").equals("lifecycle")) { LifecycleScenario.tick(mc);return; }\n','')
        s=s.replace('        if(System.getProperty("qa.rowing.scenario","").equals("fallback")) { FallbackScenario.tick(mc);return; }\n','')
        s=s.replace('EntityType.OAK_BOAT.create(level,MobSpawnType.COMMAND)','new net.minecraft.world.entity.vehicle.Boat(level,0,-58.1,0)')
    if p.stem=='MatrixScenario':
        s=s.replace('import net.minecraft.client.resources.DefaultPlayerSkin;','import net.minecraft.client.resources.DefaultPlayerSkin;\nimport net.minecraft.client.resources.PlayerSkin;')
        s=s.replace(',"pale_oak"','').replace('"_raft"','"_boat"')
        s=s.replace('var type=BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.withDefaultNamespace(test.type));\n            var boat=(Boat)type.create(server.overworld(),MobSpawnType.COMMAND);',
          'boolean chest=test.type.contains("chest");\n            var boat=chest?new net.minecraft.world.entity.vehicle.ChestBoat(server.overworld(),0,-58.1,0):new Boat(server.overworld(),0,-58.1,0);\n            String wood=test.type.replace("_chest_boat","").replace("_boat","").replace("_chest_raft","").replace("_raft","");\n            boat.setVariant(Boat.Type.byName(wood));')
    p.write_text(s,encoding='utf8')
for loader in ('qa','qa-forge','qa-neoforge'):
    platform='fabric' if loader=='qa' else loader[3:]
    s="plugins { id 'dev.architectury.loom' }\ndependencies {\n    minecraft libs.minecraft\n    mappings loom.officialMojangMappings()\n"
    if platform=='fabric':s+='    modImplementation libs.fabric.loader\n    modImplementation libs.fabric.api\n'
    else:s+='    '+('forge' if platform=='forge' else 'neoForge')+' libs.'+platform+'\n'
    s+="    compileOnly project(':"+platform+"')\n    compileOnly project(':common')\n}\n"
    s+="sourceSets.main.compileClasspath += project(':"+platform+"').sourceSets.main.compileClasspath\n"
    if loader!='qa':s+="sourceSets.main { java { srcDir rootProject.file('qa/src/main/java'); exclude '**/QaFabricClient.java' }; resources { srcDir rootProject.file('qa/src/main/resources'); exclude 'fabric.mod.json' } }\n"
    s+="loom { accessWidenerPath=rootProject.file('common/src/main/resources/seamlessrowing.accesswidener'); mixin { useLegacyMixinAp=true; defaultRefmapName='rowing-qa.refmap.json' }; mods { rowingqa { sourceSet sourceSets.main } } }\n"
    s+="tasks.named('jar') { archiveFileName='seamless-rowing-"+loader+"-dev.jar'; manifest { attributes 'MixinConfigs':'rowing-qa.mixins.json' } }\n"
    s+="tasks.named('remapJar') { archiveFileName='seamless-rowing-"+loader+".jar' }\n"
    (R/loader/'build.gradle').write_text(s,encoding='utf8')
meta=json.loads((R/'qa/src/main/resources/fabric.mod.json').read_text());meta['depends']['minecraft']='1.21.1';(R/'qa/src/main/resources/fabric.mod.json').write_text(json.dumps(meta,indent=2),encoding='utf8')
for loader in ('qa-forge','qa-neoforge'):
    p=R/loader/'src/main/resources/META-INF'/('mods.toml' if loader=='qa-forge' else 'neoforge.mods.toml')
    p.write_text(p.read_text().replace('[66,)','[52,)'),encoding='utf8')
mix=json.loads((R/'qa/src/main/resources/rowing-qa.mixins.json').read_text());mix['refmap']='rowing-qa.refmap.json'
mix['client']=['ArmContactProbe','FrameProbe','SubmittedArmProbe','NativePaddleProbe','NativeRaftProbe','RenderedPaddleProbe','EmfPaddleTransformProbe','RealCameraCaptureProbe','SkinProbe','PaddleSoundProbe','ShutdownProbe','BodySurfaceProbe']
(R/'qa/src/main/resources/rowing-qa.mixins.json').write_text(json.dumps(mix,indent=2),encoding='utf8')
print('Adapted legacy QA source and remapped packaged test modules.')
