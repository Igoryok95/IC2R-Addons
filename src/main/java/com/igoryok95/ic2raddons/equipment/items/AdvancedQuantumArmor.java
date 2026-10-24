package com.igoryok95.ic2raddons.equipment.items;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public final class AdvancedQuantumArmor extends ItemArmorQuantumSuit {
  public AdvancedQuantumArmor(EquipmentSlot slot) {
    super(
        com.igoryok95.ic2raddons.equipment.GraviArmorMaterials.QUANTUM,
        slot,
        new Properties().stacksTo(1).rarity(Rarity.EPIC));
    if (slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET)
      throw new IllegalArgumentException("slot");
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    return 100_000_000;
  }

  @Override
  public double getTransferLimit(ItemStack stack) {
    return 50_000;
  }

  @Override
  public boolean absorbFall(ItemStack stack, float distance) {
    double cost = Math.max((int) distance - 10, 0) * (getEnergyPerDamage() / 2.0);
    if (cost > ElectricItem.manager.getCharge(stack)) return false;
    ElectricItem.manager.discharge(stack, cost, Integer.MAX_VALUE, true, false, false);
    return true;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(
        Component.translatable(
            getEquipmentSlot() == EquipmentSlot.LEGS
                ? "tooltip.ic2r_addons.quantum_legs"
                : "tooltip.ic2r_addons.quantum_fall"));
  }
}
