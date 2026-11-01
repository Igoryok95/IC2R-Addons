package com.igoryok95.ic2raddons.equipment;

import com.igoryok95.ic2raddons.equipment.client.GraviClient;
import ic2.core.util.KeyboardClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Read rebound key names only while constructing client tooltips; no stored key labels. */
public final class EquipmentTooltips {
  public static Component hint(String key) {
    var options = Minecraft.getInstance().options;
    Component use = name(options.keyUse), sneak = name(options.keyShift);
    Component mode = name(KeyboardClient.modeSwitchKey), alt = name(KeyboardClient.altKey);
    return switch (key) {
      case "tooltip.ic2r_addons.engine_key" ->
          Component.translatable(key, name(GraviClient.ENGINE));
      case "tooltip.ic2r_addons.hover_keys" ->
          Component.translatable(key, name(options.keyJump), mode);
      case "tooltip.ic2r_addons.mining_keys" -> Component.translatable(key, mode, use, alt, use);
      case "tooltip.ic2r_addons.enchantment_keys" -> Component.translatable(key, sneak, mode, use);
      case "tooltip.ic2r_addons.mode_key" -> Component.translatable(key, mode, use);
      case "tooltip.ic2r_addons.voider_keys" -> Component.translatable(key, use, sneak);
      case "tooltip.ic2r_addons.magnet_keys" ->
          Component.translatable(key, use, name(GraviClient.MAGNET));
      case "tooltip.ic2r_addons.relocator_keys" -> Component.translatable(key, sneak, use, mode);
      case "tooltip.ic2r_addons.tree_keys" -> Component.translatable(key, sneak, mode, use);
      default -> throw new IllegalArgumentException("Unknown equipment hint: " + key);
    };
  }

  private static Component name(KeyMapping key) {
    return key.getTranslatedKeyMessage();
  }

  private EquipmentTooltips() {}
}
