// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.containers.slots.SlotRange;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityRemoteThermalMonitor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerRemoteThermalMonitor extends ContainerBase<TileEntityRemoteThermalMonitor> {

  public ContainerRemoteThermalMonitor(
      int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityRemoteThermalMonitor) getBlockEntity(inventory, data));
  }

  public ContainerRemoteThermalMonitor(
      int windowId, Inventory inventory, TileEntityRemoteThermalMonitor te) {
    super(
        te,
        ModContainerTypes.remote_thermo.get(),
        windowId,
        ModItems.remote_thermo.get(),
        ContainerLevelAccess.create(te.getLevel(), te.getBlockPos()));

    addSlot(new SlotCard(te, TileEntityRemoteThermalMonitor.SLOT_CARD, 9, 53));
    addSlot(new SlotRange(te, TileEntityRemoteThermalMonitor.SLOT_UPGRADE_RANGE, 27, 53));
    addPlayerInventorySlots(inventory, 180, 166);
  }
}
