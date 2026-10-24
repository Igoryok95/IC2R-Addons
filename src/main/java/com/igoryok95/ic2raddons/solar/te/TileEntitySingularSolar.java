package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class TileEntitySingularSolar extends TileEntitySolarPanel {
  public TileEntitySingularSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_SINGULAR_SOLAR_PANEL.get(), pos, state, SolarPanelType.SINGULAR);
  }
}
