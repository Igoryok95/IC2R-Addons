// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.containers.slots.SlotPower;
import com.igoryok95.ic2raddons.control.containers.slots.SlotRange;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerHoloPanel extends ContainerBase<TileEntityInfoPanel> {

  public ContainerHoloPanel(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityInfoPanel) getBlockEntity(inventory, data));
  }

  public ContainerHoloPanel(int windowId, Inventory inventory, TileEntityInfoPanel panel) {
    super(
        panel,
        ModContainerTypes.holo_panel.get(),
        windowId,
        ModItems.holo_panel.get(),
        ContainerLevelAccess.create(panel.getLevel(), panel.getBlockPos()));
    addSlot(
        new SlotCard(panel, 0, 8, 24 + 18) {
          @SuppressWarnings("resource")
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerHoloPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(new SlotRange(panel, 1, 8, 24 + 18 * 2));
    addSlot(new SlotPower(panel, 2, 8, 24 + 18 * 3));
    // inventory
    addPlayerInventorySlots(inventory, 201);
  }
}
