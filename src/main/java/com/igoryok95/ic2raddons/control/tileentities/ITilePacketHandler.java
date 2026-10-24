// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

import net.minecraft.nbt.CompoundTag;

public interface ITilePacketHandler {

  void onServerMessageReceived(CompoundTag tag);

  void onClientMessageReceived(CompoundTag tag);
}
