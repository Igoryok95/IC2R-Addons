package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class TileEntitySpectralSolar extends TileEntitySolarPanel {
  public TileEntitySpectralSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_SPECTRAL_SOLAR_PANEL.get(), pos, state, SolarPanelType.SPECTRAL);
  }
}
