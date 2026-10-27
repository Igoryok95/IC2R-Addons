package com.igoryok95.ic2raddons.equipment;

import ic2.core.IC2;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Server-originated translatable chat, matching IC2R's tool mode notifications. */
public final class ToolMessages {
  private ToolMessages() {}

  public static void setting(Player player, String format, Component value) {
    if (player != null && !player.level().isClientSide)
      IC2.sideProxy.messagePlayer(player, Component.translatable(format, value));
  }

  public static void mode(Player player, String mode) {
    setting(player, "item.ic2.mining_laser.tooltip.mode", Component.translatable(mode));
  }

  public static void toggle(Player player, String format, boolean enabled) {
    setting(player, format, Component.translatable(enabled ? "ic2r_addons.on" : "ic2r_addons.off"));
  }
}
