package com.chocohead.advsolar.te;

import com.chocohead.advsolar.AdvSolarBlocks;
import com.chocohead.advsolar.SolarPanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityAdvancedSolar extends TileEntitySolarPanel {
  public TileEntityAdvancedSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_ADVANCED_SOLAR_PANEL.get(), pos, state, SolarPanelType.ADVANCED);
  }
}
