// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityHoloPanelExtender extends TileEntityInfoPanelExtender {

  public TileEntityHoloPanelExtender(BlockPos pos, BlockState state) {
    super(ModTileEntityTypes.holo_panel_extender.get(), pos, state);
  }
}
