package qa.rowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.*;
@Mixin(value=HumanoidArmorLayer.class,priority=1100)
public class ArmorModelProbe {
    @Unique private final Set<String> qa$seen=new HashSet<>();
    @Inject(method="getArmorModel",at=@At("RETURN"))
    private void source(EquipmentSlot slot,CallbackInfoReturnable<HumanoidModel<?>> ci){
        var context=RenderContext.current();if(context==null||context.rowingPose()==null)return;
        var model=ci.getReturnValue();String key=model.getClass().getName()+"/"+context.rowingHandsOnly()+"/"+slot;
        if(!qa$seen.add(key))return;
        var part=model.leftArm;var cubes=((ModelPartAccess)(Object)part).rowingCubes();
        System.out.println("NATIVE_ARMOR_MODEL "+key+" supported="+ArmMesh.supported(part)+" children="+((ModelPartAccess)(Object)part).rowingChildren().keySet()+" bodyVisible="+model.body.visible+" cubes="+cubes.size());
        for(var cube:cubes)System.out.println("NATIVE_ARMOR_BOUNDS x="+cube.minX+","+cube.maxX+" y="+cube.minY+","+cube.maxY+" z="+cube.minZ+","+cube.maxZ);
    }
}
