// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.crossmod;

import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CrossModBase {

  public boolean isElectricItem(ItemStack stack) {
    return false;
  }

  public double dischargeItem(ItemStack stack, int needed, int tier) {
    return 0;
  }

  public CompoundTag getEnergyData(BlockEntity te) {
    return null;
  }

  public CompoundTag getCardData(BlockEntity te) {
    return null;
  }

  public CompoundTag getInventoryData(BlockEntity te) {
    return null;
  }

  public int getReactorHeat(Level level, BlockPos pos) {
    return -1;
  }

  public List<FluidInfo> getAllTanks(BlockEntity te) {
    return null;
  }

  public void registerItems(RegisterEvent.RegisterHelper<Item> event) {}

  public void addKitsToCreativeTab(CreativeModeTab.Output output) {}

  public void addCardsToCreativeTab(CreativeModeTab.Output output) {}

  public void updateEnergyNet(BlockEntity te, boolean isAdd) {}
}
