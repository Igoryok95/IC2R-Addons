// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.items.kits;

import com.igoryok95.ic2raddons.control.api.ItemStackHelper;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityEnergyCounter;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemKitCounter extends ItemKitMain {
  public ItemKitCounter(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack getSensorCard(
      ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
    if (!(level.getBlockEntity(pos) instanceof TileEntityEnergyCounter)) return ItemStack.EMPTY;
    var card = new ItemStack(ModItems.card_counter.get());
    ItemStackHelper.setCoordinates(card, pos);
    return card;
  }
}
