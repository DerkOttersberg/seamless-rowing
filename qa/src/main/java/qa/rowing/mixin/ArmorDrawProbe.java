package qa.rowing.mixin;
import io.github.derkottersberg.seamlessrowing.client.RowingArmorModel;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RowingArmorModel.class,remap=false)
public class ArmorDrawProbe {
    @Inject(method="<init>",at=@At("RETURN"))
    private void register(CallbackInfo ci){qa.rowing.ArmorSubmission.register((HumanoidModel<?>)(Object)this);}
}
