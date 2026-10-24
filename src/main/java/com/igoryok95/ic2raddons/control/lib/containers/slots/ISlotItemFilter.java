// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.lib.containers.slots;

import net.minecraft.world.item.ItemStack;

public interface ISlotItemFilter {

  boolean isItemValid(int slotIndex, ItemStack stack);
}
