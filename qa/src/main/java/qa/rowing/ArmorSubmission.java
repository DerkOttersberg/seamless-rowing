package qa.rowing;
import io.github.derkottersberg.seamlessrowing.client.RenderContext;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import java.nio.file.Files;
import java.util.Map;
/** Native armor draws must include hands without submitting head, body or legs in first person. */
public final class ArmorSubmission {
    private static final java.util.Map<ModelPart,HumanoidModel<?>> MODELS=new java.util.IdentityHashMap<>();
    private static int first,third,arms,body;
    public static void register(HumanoidModel<?> model){
        for(var part:java.util.List.of(model.head,model.hat,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg))register(part,model);
    }
    private static void register(ModelPart part,HumanoidModel<?> model){
        MODELS.put(part,model);((ModelPartAccess)(Object)part).rowingChildren().values().forEach(child->register(child,model));
    }
    public static void part(ModelPart part){
        var model=MODELS.get(part);var context=RenderContext.current();
        if(model==null||context==null||context.rowingPose()==null)return;
        if(part==model.body){if(context.rowingHandsOnly())first++;else third++;}
        if(!context.rowingHandsOnly()||!part.visible||part.skipDraw||((ModelPartAccess)(Object)part).rowingCubes().isEmpty())return;
        if(part==model.head||part==model.hat||part==model.body||part==model.leftLeg||part==model.rightLeg)body++;
        else if(descendant(model.leftArm,part)||descendant(model.rightArm,part))arms++;
    }
    private static boolean descendant(ModelPart root,ModelPart target){
        if(root==target)return true;
        return ((ModelPartAccess)(Object)root).rowingChildren().values().stream().anyMatch(child->descendant(child,target));
    }
    public static void write(Minecraft mc){
        String scenario=System.getProperty("qa.rowing.scenario","");
        if(first+third==0&&!scenario.equals("matrix")&&!scenario.equals("armor"))return;
        var result=Map.of("firstPersonWrappedDraws",first,"thirdPersonWrappedDraws",third,"firstPersonArmSurfaceDraws",arms,"firstPersonBodySurfaceDraws",body,"pass",first>0&&arms>0&&body==0);
        try{Files.writeString(mc.gameDirectory.toPath().resolve("rowing-armor-visibility.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}
        catch(java.io.IOException failure){throw new IllegalStateException(failure);}
    }
}
