package qa.rowing.mixin;

import qa.rowing.FrameContact;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class FrameProbe {
    @Inject(method="render",at=@At("HEAD"))
    private void frame(DeltaTracker tracker,boolean active,CallbackInfo ci) { FrameContact.begin(); if(System.getProperty("qa.rowing.scenario","").equals("edge"))qa.rowing.EdgeScenario.renderCleanup(net.minecraft.client.Minecraft.getInstance()); }
}

