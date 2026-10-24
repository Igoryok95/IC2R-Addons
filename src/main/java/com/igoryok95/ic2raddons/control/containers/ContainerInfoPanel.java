// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.containers.slots.SlotColor;
import com.igoryok95.ic2raddons.control.containers.slots.SlotRange;
import com.igoryok95.ic2raddons.control.containers.slots.SlotTouch;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerInfoPanel extends ContainerBase<TileEntityInfoPanel> {

  public ContainerInfoPanel(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityInfoPanel) getBlockEntity(inventory, data));
  }

  public ContainerInfoPanel(int windowId, Inventory inventory, TileEntityInfoPanel panel) {
    super(
        panel,
        ModContainerTypes.info_panel.get(),
        windowId,
        ModItems.info_panel.get(),
        ContainerLevelAccess.create(panel.getLevel(), panel.getBlockPos()));
    addSlot(
        new SlotCard(panel, 0, 8, 24 + 18) {
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerInfoPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(new SlotRange(panel, 1, 8, 24 + 18 * 2));
    addSlot(
        new SlotColor(panel, 2, 8, 24 + 18 * 3) {
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerInfoPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(new SlotTouch(panel, 3, 8, 24 + 18 * 4));
    // inventory
    addPlayerInventorySlots(inventory, 201);
  }
}
