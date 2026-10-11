package io.github.derkottersberg.seamlessrowing.integration;
/** Reads the existing public action state; no input, inventory or networking changes. */
public final class SeamlessThrowAdapter { private SeamlessThrowAdapter() {} public static boolean active(net.minecraft.client.player.AbstractClientPlayer player) { return io.github.derkottersberg.swordthrow.client.SwordThrowClient.poseStateFor(player.getId())!=null; } }
