package qa.rowing;
import net.minecraft.client.Minecraft;
public final class SurfaceSubmission {
    public static long vertices,invalid,torsoVertices;
    public static boolean torso;
    public static void vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz){
        vertices++;
        if(torso)torsoVertices++;
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z)||!Float.isFinite(u)||!Float.isFinite(v)||!Float.isFinite(nx)||!Float.isFinite(ny)||!Float.isFinite(nz))invalid++;
        if(u<0||u>1||v<0||v>1)invalid++;
    }
    public static void write(Minecraft mc){
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-native-surfaces.json"),
            "{\"directPolygonVertices\":"+vertices+",\"directTorsoVertices\":"+torsoVertices+",\"invalidVertices\":"+invalid+",\"pass\":"+(vertices>100&&invalid==0)+"}");}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}


