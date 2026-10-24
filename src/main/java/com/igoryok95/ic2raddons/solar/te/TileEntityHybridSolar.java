package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.SolarPanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityHybridSolar extends TileEntitySolarPanel {
  public TileEntityHybridSolar(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_HYBRID_SOLAR_PANEL.get(), pos, state, SolarPanelType.HYBRID);
  }
}
