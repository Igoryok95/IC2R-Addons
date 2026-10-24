// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPortablePanel extends Item {

  public ItemPortablePanel(Item.Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!player.isShiftKeyDown() && !level.isClientSide() && stack.getCount() == 1)
      player.openMenu(
          new InventoryPortablePanel(stack, hand),
          buf -> buf.writeBoolean(hand == InteractionHand.OFF_HAND));
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
  }
}
