// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.kits;

import com.igoryok95.ic2raddons.control.api.ItemStackHelper;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemKitLiquid extends ItemKitMain {

  public ItemKitLiquid(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack getSensorCard(
      ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
    List<FluidInfo> list = CrossModLoader.getAllTanks(level, pos);
    if (list != null && list.size() >= 1) {
      ItemStack card = new ItemStack(ModItems.card_liquid.get());
      ItemStackHelper.setCoordinates(card, pos);
      return card;
    }
    return ItemStack.EMPTY;
  }
}
