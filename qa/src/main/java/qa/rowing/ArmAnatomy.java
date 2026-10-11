package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.ArmRigModel;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import java.nio.file.Files;
import java.util.*;

public final class ArmAnatomy {
    private static int samples,reverse,sideways,invertedHinge;
    private static int submittedSamples,submittedReverse,submittedSideways,submittedInvertedHinge;
    private static double maxSideways,maxLengthError,minElbowZ=Double.POSITIVE_INFINITY;
    private static final Map<String,Integer> views=new TreeMap<>();
    public static void measure(ArmRigModel.Bone bone,boolean left) {
        Vector3f lower=new Vector3f(0,6,0).rotate(bone.lower());
        Vector3f upper=new Vector3f(0,6,0).rotate(bone.upper());
        if(new Vector3f(1,0,0).rotate(bone.upper()).x<-.001) invertedHinge++;
        samples++;if(lower.z>.001) reverse++;if(Math.abs(lower.x)>.001) sideways++;
        maxSideways=Math.max(maxSideways,Math.abs(lower.x));
        maxLengthError=Math.max(maxLengthError,Math.max(Math.abs(upper.length()-6),Math.abs(lower.length()-6)));
        minElbowZ=Math.min(minElbowZ,upper.z);
        views.merge(Minecraft.getInstance().options.getCameraType().name()+"/"+(left?"left":"right"),1,Integer::sum);
    }
    public static void write(Minecraft mc) {
        Map<String,Object> report=new LinkedHashMap<>();
        report.put("samples",samples);report.put("reverseFlexionSamples",reverse);report.put("sidewaysHingeSamples",sideways);
        report.put("invertedHingeSamples",invertedHinge);
        report.put("maximumSidewaysHingePixels",maxSideways);report.put("maximumLimbLengthErrorPixels",maxLengthError);
        if(samples>0) report.put("minimumElbowBehindShoulderPixels",minElbowZ);
        report.put("submittedSamples",submittedSamples);report.put("submittedReverseFlexionSamples",submittedReverse);
        report.put("submittedSidewaysHingeSamples",submittedSideways);
        report.put("submittedInvertedHingeSamples",submittedInvertedHinge);
        report.put("views",views);report.put("pass",samples>0&&reverse==0&&sideways==0&&invertedHinge==0&&maxLengthError<.001&&submittedSamples>0&&submittedReverse==0&&submittedSideways==0&&submittedInvertedHinge==0);
        try { Files.writeString(mc.gameDirectory.toPath().resolve("rowing-anatomy.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch(Exception failure) { throw new RuntimeException(failure); }
    }
    public static void submitted(net.minecraft.client.model.geom.ModelPart upper,net.minecraft.client.model.geom.ModelPart lower) {
        var upperStack=new com.mojang.blaze3d.vertex.PoseStack();upper.translateAndRotate(upperStack);
        if(upperStack.last().pose().transformDirection(new Vector3f(1,0,0)).x<-.001) submittedInvertedHinge++;
        var stack=new com.mojang.blaze3d.vertex.PoseStack();lower.translateAndRotate(stack);
        Vector3f tip=stack.last().pose().transformPosition(new Vector3f(0,6f/16,0));
        Vector3f origin=stack.last().pose().transformPosition(new Vector3f());
        tip.sub(origin).mul(16);submittedSamples++;
        if(tip.z>.001) submittedReverse++;if(Math.abs(tip.x)>.001) submittedSideways++;
    }
}
