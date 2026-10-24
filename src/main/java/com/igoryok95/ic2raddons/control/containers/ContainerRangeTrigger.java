// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.containers.slots.SlotRange;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityRangeTrigger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerRangeTrigger extends ContainerBase<TileEntityRangeTrigger> {

  public ContainerRangeTrigger(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityRangeTrigger) getBlockEntity(inventory, data));
  }

  public ContainerRangeTrigger(int windowId, Inventory inventory, TileEntityRangeTrigger te) {
    super(
        te,
        ModContainerTypes.range_trigger.get(),
        windowId,
        ModItems.range_trigger.get(),
        ContainerLevelAccess.create(te.getLevel(), te.getBlockPos()));

    addSlot(new SlotCard(te, 0, 8, 21));
    addSlot(new SlotRange(te, 1, 8, 39));
    addPlayerInventorySlots(inventory, 190);
  }
}
