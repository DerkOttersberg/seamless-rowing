package qa.rowing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import java.util.*;
public final class SkinSourceProbe {
    public static boolean written;
    public static void inspect(PlayerModel model){
        if(written)return;written=true;var data=new LinkedHashMap<String,Object>();
        var mc=Minecraft.getInstance();data.put("skin",mc.player.getSkin().texture().toString());
        var parts=new ModelPart[]{model.head,model.body,model.leftArm,model.rightArm,model.leftSleeve,model.rightSleeve};
        var names=new String[]{"head","body","leftArm","rightArm","leftSleeve","rightSleeve"};
        for(int i=0;i<parts.length;i++){
            var part=parts[i];var cubes=new ArrayList<Object>();
            for(var cube:((ModelPartAccess)(Object)part).rowingCubes()){
                float minU=1,minV=1,maxU=0,maxV=0,minY=Float.POSITIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY;
                for(var face:cube.polygons)for(var v:face.vertices){minU=Math.min(minU,v.u);maxU=Math.max(maxU,v.u);minV=Math.min(minV,v.v);maxV=Math.max(maxV,v.v);minY=Math.min(minY,v.pos.y);maxY=Math.max(maxY,v.pos.y);}
                cubes.add(Map.of("bounds",List.of(cube.minX,cube.minY,cube.minZ,cube.maxX,cube.maxY,cube.maxZ),"uv",List.of(minU,minV,maxU,maxV),"actualY",List.of(minY,maxY)));
            }
            data.put(names[i],Map.of("class",part.getClass().getName(),"description",part.toString(),"cubes",cubes));
        }
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-skin-sources.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(data));}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}
