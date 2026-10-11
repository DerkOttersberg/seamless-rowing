package qa.rowing;
import net.minecraft.client.Minecraft;
public final class LocalHeadDraws {
    public static int ownedHeads,unownedHeads,handsHeads;
    public static void write(Minecraft mc){
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-local-heads.json"),"{\"ownedHeadDrawCalls\":"+ownedHeads+",\"ownedHandsHeadDrawCalls\":"+handsHeads+",\"unownedHeadDrawCalls\":"+unownedHeads+"}");}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}

