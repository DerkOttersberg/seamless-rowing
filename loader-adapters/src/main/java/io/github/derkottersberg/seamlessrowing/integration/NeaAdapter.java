package io.github.derkottersberg.seamlessrowing.integration;

import dev.tr7zw.notenoughanimations.api.*;
import dev.tr7zw.notenoughanimations.access.PlayerData;
import dev.tr7zw.notenoughanimations.versionless.animations.BodyPart;
import io.github.derkottersberg.seamlessrowing.client.*;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Minecraft;

/** Reserves only arms which rowing may own. Direct actions remain above priority 1501. */
public final class NeaAdapter extends BasicAnimation {
    public static void register() {
        NotEnoughAnimationsApi.registerAnimation(new NeaAdapter());
        NotEnoughAnimationsApi.refreshEnabledAnimations();
    }
    public boolean isEnabled() { return true; }
    public boolean isValid(AbstractClientPlayer player,PlayerData data) { return pose(player)!=null; }
    private static RowingPose pose(AbstractClientPlayer player) {
        return RowingClient.capture(player,1,Minecraft.getInstance().player==player && Minecraft.getInstance().options.getCameraType().isFirstPerson());
    }
    public BodyPart[] getBodyParts(AbstractClientPlayer player,PlayerData data) {
        RowingPose row=pose(player);
        if(row==null) return new BodyPart[0];
        if(row.ownLeft()&&row.ownRight()) return new BodyPart[]{BodyPart.LEFT_ARM,BodyPart.RIGHT_ARM};
        if(row.ownLeft()) return new BodyPart[]{BodyPart.LEFT_ARM};
        if(row.ownRight()) return new BodyPart[]{BodyPart.RIGHT_ARM};
        return new BodyPart[0];
    }
    public int getPriority(AbstractClientPlayer player,PlayerData data) { return 1501; }
    public void apply(AbstractClientPlayer player,PlayerData data,PlayerModel model,BodyPart part,float delta,float time) {
        ((AnimationOwnership)(Object)model).rowingGrant(part==BodyPart.LEFT_ARM);
    }
}
