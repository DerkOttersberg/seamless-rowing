package qa.rowing;
import net.minecraft.client.Minecraft;
public final class FirstPersonOwnership {
    public static int nativeBodyQueries,invalidBodyTypes,extraOwnedVanillaArms;
    public static void write(Minecraft mc){
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-first-person-ownership.json"),
            "{\"pass\":"+(invalidBodyTypes==0&&extraOwnedVanillaArms==0)+",\"nativeHandsBodyQueries\":"+nativeBodyQueries+",\"unexpectedBodyTypes\":"+invalidBodyTypes+",\"extraOwnedVanillaArms\":"+extraOwnedVanillaArms+"}");}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}
