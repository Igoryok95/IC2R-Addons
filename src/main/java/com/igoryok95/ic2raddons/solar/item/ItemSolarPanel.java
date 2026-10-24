package com.igoryok95.ic2raddons.solar.item;

import com.igoryok95.ic2raddons.solar.SolarPanelType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemSolarPanel extends BlockItem {
  private final SolarPanelType type;

  public ItemSolarPanel(Block block, SolarPanelType type) {
    super(block, new Properties());
    this.type = type;
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack stack,
      TooltipContext context,
      @NotNull List<Component> tooltip,
      @NotNull TooltipFlag flag) {
    tooltip.add(
        Component.translatable("ic2r_addons.tooltip.generation.day", this.type.dayPower)
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable("ic2r_addons.tooltip.generation.night", this.type.nightPower)
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable(
                "ic2.item.tooltip.Output", this.type.getMaxOutput() * this.type.getPacketCount())
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable(
                "tooltip.ic2r_addons.solar.packets",
                this.type.getMaxOutput(),
                this.type.getPacketCount())
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable("ic2.item.tooltip.Capacity", this.type.maxStorage)
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable("ic2.item.tooltip.power_tier", this.type.tier)
            .withStyle(ChatFormatting.GRAY));
  }
}
