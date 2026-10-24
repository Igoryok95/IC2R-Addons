// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.kits;

import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemKitEnergy extends ItemKitMain {

  public ItemKitEnergy(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack getSensorCard(
      ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
    return CrossModLoader.getEnergyCard(level, pos);
  }
}
