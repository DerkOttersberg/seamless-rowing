package io.github.derkottersberg.seamlessrowing.integration;
import net.minecraft.client.player.AbstractClientPlayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
/** Read-only ownership check: active combat/emote layers take precedence over rowing. */
public final class PlayerAnimatorAdapter {
    private PlayerAnimatorAdapter(){}
    public static boolean active(AbstractClientPlayer player){return PlayerAnimationAccess.getPlayerAnimLayer(player).isActive();}
}
