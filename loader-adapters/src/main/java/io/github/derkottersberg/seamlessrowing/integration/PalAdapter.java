package io.github.derkottersberg.seamlessrowing.integration;

import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;

public final class PalAdapter {
    private PalAdapter() {}
    public static boolean active(AbstractClientPlayer player) {
        // An active PAL layer owns the rig, including emote and combat layers.
        return PlayerAnimationAccess.getPlayerAnimManager(player).isActive();
    }
}
