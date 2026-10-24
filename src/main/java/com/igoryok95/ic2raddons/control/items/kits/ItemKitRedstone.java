// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.kits;

import com.igoryok95.ic2raddons.control.api.ItemStackHelper;
import com.igoryok95.ic2raddons.control.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemKitRedstone extends ItemKitMain {

  public ItemKitRedstone(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack getSensorCard(
      ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
    BlockState state = level.getBlockState(pos);
    Block block = state.getBlock();
    if (block != Blocks.AIR) {
      ItemStack newCard = new ItemStack(ModItems.card_redstone.get());
      ItemStackHelper.setCoordinates(newCard, pos);
      return newCard;
    }
    return ItemStack.EMPTY;
  }
}
