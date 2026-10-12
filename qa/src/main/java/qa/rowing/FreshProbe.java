package qa.rowing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import io.github.derkottersberg.seamlessrowing.client.RowingState;

import traben.entity_model_features.models.IEMFModel;
import java.nio.file.Files;
import java.util.Map;

/** Native model inspection: a loaded custom pack must recover, not just coexist disabled. */
public final class FreshProbe {
    public static boolean customVariant(PlayerModel model) {
        if(!((Object)model instanceof IEMFModel emf))return false;
        var root=emf.emf$getEMFRootModel();
        return root!=null&&root.containsCustomModel&&root.currentModelVariant!=0;
    }
    private static int rowing,wrongRowingVariant,customOutside,vanillaOutside;
    public static boolean rowingCoverageReady(){return rowing>=15;}
    public static boolean restoredCoverageReady(){return customOutside>=15;}
    public static void inspect(PlayerModel model,io.github.derkottersberg.seamlessrowing.client.RenderContext state) {
        if(!((Object)model instanceof IEMFModel emf))return;
        var root=emf.emf$getEMFRootModel();
        if(root==null||!root.containsCustomModel)return;
        if(state!=null&&state.rowingPose()!=null) {
            rowing++;if(root.currentModelVariant!=0)wrongRowingVariant++;
        } else if(root.currentModelVariant!=0)customOutside++;
        else vanillaOutside++;
    }
    public static void write(Minecraft mc) {
        if(rowing+customOutside+vanillaOutside==0)return;
        var result=Map.of("rowingModelInspections",rowing,"wrongRowingVariants",wrongRowingVariant,
                "customModelOutsideRowing",customOutside,"vanillaOutsideRowing",vanillaOutside,
                "pass",rowing>10&&customOutside>10&&wrongRowingVariant==0);
        try {Files.writeString(mc.gameDirectory.toPath().resolve("rowing-fresh-model.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}
        catch(java.io.IOException failed){throw new IllegalStateException(failed);}
    }
}
