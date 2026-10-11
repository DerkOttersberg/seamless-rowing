package qa.rowing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
public final class HeadCompileProbe {
    public static int owned,unowned;
    public static void inspect(ModelPart part){
        var mc=Minecraft.getInstance();if(mc.player==null||!mc.options.getCameraType().isFirstPerson()||mc.player.getVehicle()==null)return;
        if(((ModelPartAccess)(Object)part).rowingCubes().stream().noneMatch(c->c.maxX-c.minX==8&&c.maxY-c.minY==8&&c.maxZ-c.minZ==8))return;
        var pose=io.github.derkottersberg.seamlessrowing.client.RowingClient.capture(mc.player,1,true);
        if(pose==null){if(unowned++==0){var context=RenderContext.current();System.out.println("HEAD_UNOWNED part="+part+" context="+(context!=null)+" hands="+(context!=null&&context.rowingHandsOnly()));new Throwable("Native unowned first-person head").printStackTrace(System.out);}return;}
        if(owned++==0){var context=RenderContext.current();System.out.println("HEAD_COMPILE part="+part+" context="+(context!=null)+" hands="+(context!=null&&context.rowingHandsOnly()));new Throwable("Native first-person head compile").printStackTrace(System.out);}
    }
    public static void write(Minecraft mc){
        try{java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("rowing-head-compiles.json"),"{\"ownedHeadCompiles\":"+owned+",\"unownedHeadCompiles\":"+unowned+"}");}catch(Exception failure){throw new RuntimeException(failure);}
    }
}
