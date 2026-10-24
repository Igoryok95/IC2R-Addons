package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class TileEntityLightAbsorbingSolar extends TileEntitySolarPanel {
  public TileEntityLightAbsorbingSolar(BlockPos pos, BlockState state) {
    super(
        AdvSolarBlocks.BE_LIGHT_ABSORBING_SOLAR_PANEL.get(),
        pos,
        state,
        SolarPanelType.LIGHT_ABSORBING);
  }
}
