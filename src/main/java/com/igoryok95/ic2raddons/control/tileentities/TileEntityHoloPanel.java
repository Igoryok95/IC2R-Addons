// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

import com.igoryok95.ic2raddons.control.containers.ContainerHoloPanel;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityHoloPanel extends TileEntityInfoPanel {
  private static final byte SLOT_CARD = 0;
  private static final byte SLOT_UPGRADE_RANGE = 1;
  private static final byte SLOT_UPGRADE_POWER = 2;

  public TileEntityHoloPanel(BlockPos pos, BlockState state) {
    super(ModTileEntityTypes.holo_panel.get(), pos, state);
  }

  public int getPower() {
    ItemStack stack = getItem(SLOT_UPGRADE_POWER);
    if (stack.isEmpty()) return 1;
    return stack.getCount() + 1;
  }

  @Override
  public boolean isItemValid(int index, ItemStack stack) { // ISlotItemFilter
    switch (index) {
      case SLOT_CARD:
        return ItemCardMain.isCard(stack);
      case SLOT_UPGRADE_RANGE:
        return stack.getItem().equals(ModItems.upgrade_range.get());
      case SLOT_UPGRADE_POWER:
        return stack.getItem().equals(ModItems.advanced_circuit.get());
      default:
        return false;
    }
  }

  public AABB getRenderBoundingBox() { // forge only
    if (screen == null) return new AABB(worldPosition);
    return new AABB(
        screen.minX,
        screen.minY,
        screen.minZ,
        screen.maxX + 1,
        screen.maxY + getPower(),
        screen.maxZ + 1);
  }

  @Override
  public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
    return new ContainerHoloPanel(windowId, inventory, this);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable(ModItems.holo_panel.get().getDescriptionId());
  }
}
