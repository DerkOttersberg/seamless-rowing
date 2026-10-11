package qa.rowing;

import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import java.util.*;
import java.nio.file.Files;

/** Inspects the actual submitted textured rings, independently of the skin-envelope equation. */
public final class SurfaceGeometry {
    private static int samples,brokenRings,invalidFaces;
    private static double wristError;
    private static int bodySamples,restoredBodies,failedBodyRestores;
    private static int restoredCustomBodies;
    private static double neckError,minNeckZ=Double.POSITIVE_INFINITY,maxNeckZ=Double.NEGATIVE_INFINITY;
    private static final Map<ModelPart,Boolean> skinBodies=new WeakHashMap<>();
    private static final Map<ModelPart,Boolean> relocatedHeads=new WeakHashMap<>();
    private static int inflatedEquipmentBodies;
    private static int hiddenBodySamples,relocatedHeadSamples,changedHiddenHeads;
    public static void source(net.minecraft.client.model.PlayerModel model) {
        var cubes=((ModelPartAccess)(Object)model.body).rowingCubes();
        if(cubes.size()!=1)return;
        float min=Float.POSITIVE_INFINITY,max=Float.NEGATIVE_INFINITY;
        for(var face:cubes.get(0).polygons)for(var v:face.vertices){min=Math.min(min,v.pos.y);max=Math.max(max,v.pos.y);}
        boolean skin=Math.abs(min)<.01&&Math.abs(max-12)<.01;
        if(!skin&&!skinBodies.containsKey(model.body))System.out.println("NATIVE_INFLATED_EQUIPMENT_BODY minY="+min+" maxY="+max);
        skinBodies.put(model.body,skin);
        relocatedHeads.put(model.body,Math.abs(model.head.y)>1000);
    }
    public static void body(net.minecraft.client.model.PlayerModel model,io.github.derkottersberg.seamlessrowing.client.RowingPose row) {
        var cubes=((ModelPartAccess)(Object)model.body).rowingCubes();
        if(row==null&&qa.rowing.QaPlatform.isModLoaded("entity_model_features")&&FreshProbe.customVariant(model)) {
            // EMF's custom variant attaches geometry to children of the native body slot.
            // The loaded, hash-verified Fresh rig leaves that slot empty.
            restoredCustomBodies++;if(!cubes.isEmpty())failedBodyRestores++;return;
        }
        if(!skinBodies.getOrDefault(model.body,true)){inflatedEquipmentBodies++;return;}
        if(row==null) {restoredBodies++;if(cubes.size()!=1)failedBodyRestores++;return;}
        if(Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && qa.rowing.QaPlatform.isModLoaded("firstperson")
                && (relocatedHeads.getOrDefault(model.body,false)||Math.abs(model.head.y)>1000)) {
            relocatedHeadSamples++;
            if(Math.abs(model.head.y)<1000)changedHiddenHeads++;
            return;
        }
        if(!model.body.visible||!model.head.visible){hiddenBodySamples++;return;}
        if(cubes.size()<2)return;
        var neck=new Vector3f(model.head.x-model.body.x,model.head.y-model.body.y,model.head.z-model.body.z);
        double nearest=Double.POSITIVE_INFINITY;
        for(var face:cubes.get(cubes.size()-1).polygons) if(face.vertices.length==4) {
            var center=new Vector3f();for(var v:face.vertices)center.add(v.pos.x,v.pos.y,v.pos.z);center.mul(.25f);
            nearest=Math.min(nearest,center.distance(neck));
        }
        bodySamples++;neckError=Math.max(neckError,nearest);
        if(nearest>.001)System.out.println("NATIVE_NECK_GAP case="+MatrixScenario.current()+" head="+neck+" body="+model.body.x+","+model.body.y+","+model.body.z+" gap="+nearest);
        minNeckZ=Math.min(minNeckZ,neck.z);maxNeckZ=Math.max(maxNeckZ,neck.z);
    }
    public static void measure(ModelPart upper,ModelPart lower,boolean firstPerson) {
        var part=firstPerson?upper.getChild("distal"):upper;
        var cubes=((ModelPartAccess)(Object)part).rowingCubes();
        if(cubes.isEmpty()) { invalidFaces++;return; }
        Set<String> previous=null;
        for(int i=0;i<cubes.size()-1;i++) {
            Set<String> ring=new HashSet<>();
            for(var face:cubes.get(i).polygons) {
                if(face.vertices.length==0)continue;
                if(face.vertices.length!=4 || !Float.isFinite(face.normal.x()+face.normal.y()+face.normal.z())) invalidFaces++;
                for(var v:face.vertices) {
                    if(!Float.isFinite(v.pos.x+v.pos.y+v.pos.z)) invalidFaces++;
                    ring.add(Math.round(v.pos.x*100000)+","+Math.round(v.pos.y*100000)+","+Math.round(v.pos.z*100000));
                }
            }
            if(previous!=null) {var shared=new HashSet<>(previous);shared.retainAll(ring);if(shared.size()!=4)brokenRings++;}
            previous=ring;
        }
        var marker=new com.mojang.blaze3d.vertex.PoseStack();lower.translateAndRotate(marker);
        var wrist=marker.last().pose().transformPosition(new Vector3f(0,6f/16,0)).mul(16);
        double nearest=Double.POSITIVE_INFINITY;
        for(var face:cubes.get(cubes.size()-1).polygons) if(face.vertices.length==4) {
            var center=new Vector3f();for(var v:face.vertices)center.add(v.pos.x,v.pos.y,v.pos.z);center.mul(.25f);
            nearest=Math.min(nearest,center.distance(wrist));
        }
        wristError=Math.max(wristError,nearest);samples++;
    }
    public static void write(Minecraft mc) {
        var report=new LinkedHashMap<String,Object>();report.put("submittedSkinSamples",samples);
        report.put("brokenSharedRings",brokenRings);report.put("invalidFaces",invalidFaces);
        report.put("maximumSkinWristToBonePixels",wristError);
        report.put("nativeBodySamples",bodySamples);report.put("restoredVanillaBodySamples",restoredBodies);
        report.put("restoredFreshBodySamples",restoredCustomBodies);
        report.put("inflatedEquipmentBodySamplesExcludedFromSkinNeckCheck",inflatedEquipmentBodies);
        report.put("hiddenBodySamples",hiddenBodySamples);report.put("firstPersonModelRelocatedHeads",relocatedHeadSamples);
        report.put("changedHiddenHeads",changedHiddenHeads);
        report.put("failedBodyRestores",failedBodyRestores);report.put("maximumNeckToTorsoPixels",neckError);
        if(bodySamples>0)report.put("neckForwardRangePixels",maxNeckZ-minNeckZ);
        report.put("pass",samples>0&&brokenRings==0&&invalidFaces==0&&wristError<.01&&bodySamples>0&&restoredBodies+restoredCustomBodies>0&&failedBodyRestores==0&&neckError<.001&&changedHiddenHeads==0);
        try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-surface.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}
