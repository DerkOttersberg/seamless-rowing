package qa.rowing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import java.nio.file.Files;
/** Native visibility assertions after model animation hooks. */
public final class PaddleVisibility {
    private static int samples,hidden;
    public static void inspect(boolean required,ModelPart left,ModelPart right){
        if(required){samples++;if(!left.visible||!right.visible)hidden++;}
    }
    public static void write(Minecraft mc){
        if(samples==0)return;
        try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-paddle-visibility.json"),"{\"pass\":"+ (hidden==0)+",\"nativeSamples\":"+samples+",\"hiddenPaddles\":"+hidden+"}");}
        catch(java.io.IOException error){throw new IllegalStateException(error);}
    }
}
