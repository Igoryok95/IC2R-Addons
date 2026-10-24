package com.igoryok95.ic2raddons.equipment.items;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemTooltipHandler;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class ElectricUtility extends Item implements IElectricItem {
  private final int capacity, transfer, tier;

  public ElectricUtility(int capacity, int transfer, int tier) {
    super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    this.capacity = capacity;
    this.transfer = transfer;
    this.tier = tier;
  }

  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }

  public double getMaxCharge(ItemStack stack) {
    return capacity;
  }

  public double getTransferLimit(ItemStack stack) {
    return transfer;
  }

  public int getTier(ItemStack stack) {
    return tier;
  }

  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    ElectricItemTooltipHandler.addTooltip(stack, tooltip);
  }

  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  public int getBarWidth(ItemStack stack) {
    return (int) Math.round(13 * ElectricItem.manager.getCharge(stack) / capacity);
  }

  public int getBarColor(ItemStack stack) {
    return 0x48c8ff;
  }
}
