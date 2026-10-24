// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers.slots;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotFilter;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;

public class SlotPower extends SlotFilter {

  public SlotPower(Container inventory, int slotIndex, int x, int y) {
    super(inventory, slotIndex, x, y);
  }

  @Override
  public int getMaxStackSize() {
    return 16;
  }

  @Override
  public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
    return Pair.of(
        InventoryMenu.BLOCK_ATLAS,
        ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "slots/slot_power"));
  }
}
