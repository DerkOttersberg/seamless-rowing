package qa.rowing;
import net.minecraft.client.Minecraft;
/** Wall-clock cadence from genuine rendered world frames, excluding loading screens. */
public final class FrameCadence {
    private static long first,last,window;
    private static int frames,inWindow,maxWindowFrames;
    private static double maxWindowFps;
    public static void frame(Minecraft mc){
        if(mc.level==null||mc.player==null||mc.player.getVehicle()==null)return;
        long now=System.nanoTime();
        if(first==0){first=now;window=now;}
        frames++;inWindow++;last=now;
        if(now-window>=1_000_000_000L){maxWindowFrames=Math.max(maxWindowFrames,inWindow);maxWindowFps=Math.max(maxWindowFps,(inWindow-1)*1_000_000_000d/(now-window));window=now;inWindow=0;}
    }
    public static void write(Minecraft mc){
        double seconds=(last-first)/1_000_000_000d;
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-frame-cadence.json"),
            new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(java.util.Map.of("worldFrames",frames,"measuredSeconds",seconds,"averageFps",seconds>0?(frames-1)/seconds:0,"maximumFramesInOneSecondWindow",maxWindowFrames,"maximumMeasuredWindowFps",maxWindowFps)));}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}
