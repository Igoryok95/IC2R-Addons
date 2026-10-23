package reforged.mods.gravisuite.items;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorNanoSuit;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public final class AdvancedNanoArmor extends ItemArmorNanoSuit {
  public AdvancedNanoArmor(EquipmentSlot slot) {
    super(
        reforged.mods.gravisuite.GraviArmorMaterials.NANO,
        slot,
        new Properties().stacksTo(1).rarity(Rarity.RARE));
    if (slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET)
      throw new IllegalArgumentException("slot");
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    return 8_000_000;
  }

  @Override
  public double getTransferLimit(ItemStack stack) {
    return 5_000;
  }

  @Override
  public boolean absorbFall(ItemStack stack, float distance) {
    double cost = Math.max((int) distance - 3, 0) * (double) getEnergyPerDamage();
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
                ? "tooltip.gravisuite.nano_sprint"
                : "tooltip.gravisuite.nano_fall"));
  }
}
