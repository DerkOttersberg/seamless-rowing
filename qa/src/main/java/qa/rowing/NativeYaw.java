package qa.rowing;
import net.minecraft.world.entity.vehicle.Boat;
public final class NativeYaw {
    private static int boundaryFrames;
    private static float maximumDifference;
    public static void observe(Boat boat,float yaw,float partial){
        if(!System.getProperty("qa.rowing.scenario","").equals("lifecycle")||Math.abs(boat.yRotO-boat.getYRot())<180)return;
        boundaryFrames++;maximumDifference=Math.max(maximumDifference,Math.abs(yaw-net.minecraft.util.Mth.lerp(partial,boat.yRotO,boat.getYRot())));
    }
    public static int frames(){return boundaryFrames;}
    public static float maximumDifference(){return maximumDifference;}
}
