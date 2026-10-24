// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.containers.slots.SlotRange;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerAdvancedInfoPanel extends ContainerBase<TileEntityInfoPanel> {

  public ContainerAdvancedInfoPanel(
      int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityInfoPanel) getBlockEntity(inventory, data));
  }

  public ContainerAdvancedInfoPanel(int windowId, Inventory inventory, TileEntityInfoPanel panel) {
    super(
        panel,
        ModContainerTypes.info_panel_advanced.get(),
        windowId,
        ModItems.info_panel_advanced.get(),
        ContainerLevelAccess.create(panel.getLevel(), panel.getBlockPos()));
    addSlot(
        new SlotCard(panel, 0, 8, 24 + 18) {
          @SuppressWarnings("resource")
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerAdvancedInfoPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(
        new SlotCard(panel, 1, 8 + 18, 24 + 18) {
          @SuppressWarnings("resource")
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerAdvancedInfoPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(
        new SlotCard(panel, 2, 8 + 36, 24 + 18) {
          @SuppressWarnings("resource")
          @Override
          public void setChanged() {
            if (panel.getLevel().isClientSide()) ContainerAdvancedInfoPanel.this.broadcastChanges();
          }
          ;
        });
    addSlot(new SlotRange(panel, 3, 8 + 54, 24 + 18));
    // inventory
    addPlayerInventorySlots(inventory, 223);
  }
}
