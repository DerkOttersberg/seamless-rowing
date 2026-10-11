package qa.rowing;
import net.minecraft.client.Minecraft;
import java.nio.file.*;
/** Keeps a genuine stroke running while the second packaged client starts and late-tracks it. */
public final class MultiplayerMarkers {
    private static Path root(Minecraft mc){return mc.gameDirectory.toPath().getParent();}
    public static boolean hold(Minecraft mc,String role,int tick){
        boolean held=Boolean.getBoolean("qa.rowing.lateTracking")&&role.equals("rower")&&tick==60&&!Files.exists(root(mc).resolve("mp-observer-ready"));
        if(held){mc.options.keyUp.setDown(false);mc.options.keyLeft.setDown(true);}
        return held;
    }
    public static void joined(Minecraft mc,String role){
        if(Boolean.getBoolean("qa.rowing.lateTracking")&&role.equals("observer"))write(root(mc).resolve("mp-observer-ready"));
    }
    public static void established(Minecraft mc,String role,int owned){
        if(Boolean.getBoolean("qa.rowing.lateTracking")&&role.equals("rower")&&owned>=10&&FrameContact.samples()>0)
            write(root(mc).resolve("mp-rowing-started"));
    }
    private static void write(Path path){try{if(!Files.exists(path))Files.writeString(path,"native-world-ready");}catch(java.io.IOException failed){throw new IllegalStateException(failed);}}
}
