package com.chocohead.advsolar.te;

import com.chocohead.advsolar.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class TileEntityPhotonicSolar extends TileEntitySolarPanel {
  public TileEntityPhotonicSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_PHOTONIC_SOLAR_PANEL.get(), pos, state, SolarPanelType.PHOTONIC);
  }
}
