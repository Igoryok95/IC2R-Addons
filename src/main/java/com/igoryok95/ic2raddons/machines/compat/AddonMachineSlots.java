package com.igoryok95.ic2raddons.machines.compat;

import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.world.inventory.Slot;

public final class AddonMachineSlots {
  public static List<Slot> inputs(DynamicContainer<?> menu) {
    return menu.slots.stream()
        .filter(
            slot -> slot instanceof SlotInvSlot inventory && inventory.invSlot.name.equals("input"))
        .toList();
  }

  public static List<Slot> inventory(DynamicContainer<?> menu) {
    return menu.slots.stream()
        .filter(
            slot ->
                slot.container == menu.getPlayer().getInventory()
                    && slot.getContainerSlot() >= 0
                    && slot.getContainerSlot() < 36)
        .toList();
  }

  private AddonMachineSlots() {}
}
