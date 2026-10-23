package com.chocohead.advsolar.te;

import com.chocohead.advsolar.AdvSolarBlocks;
import com.chocohead.advsolar.SolarPanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityHybridSolar extends TileEntitySolarPanel {
  public TileEntityHybridSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_HYBRID_SOLAR_PANEL.get(), pos, state, SolarPanelType.HYBRID);
  }
}
