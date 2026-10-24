package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.SolarPanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityUltimateHybridSolar extends TileEntitySolarPanel {
  public TileEntityUltimateHybridSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_ULTIMATE_SOLAR_PANEL.get(), pos, state, SolarPanelType.ULTIMATE);
  }
}
