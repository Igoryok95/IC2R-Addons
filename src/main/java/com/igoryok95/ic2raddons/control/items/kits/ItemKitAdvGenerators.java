// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.kits;

import com.igoryok95.ic2raddons.control.api.ItemStackHelper;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.crossmod.ModIDs;
import com.igoryok95.ic2raddons.control.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemKitAdvGenerators extends ItemKitMain {

  public ItemKitAdvGenerators(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack getSensorCard(
      ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
    BlockEntity te = level.getBlockEntity(pos);
    CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.ADV_GENERATORS).getCardData(te);
    if (tag != null) {
      ItemStack newCard = new ItemStack(ModItems.card_adv_generators);
      ItemStackHelper.setCoordinates(newCard, pos);
      return newCard;
    }
    return ItemStack.EMPTY;
  }
}
