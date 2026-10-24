package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.Recipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityWaterJetCutter extends TileEntityHeatingWaterMachine {
  public TileEntityWaterJetCutter(BlockPos pos, BlockState state) {
    super(
        AdvMachinesBlocks.BE_WATER_JET_CUTTER.get(),
        pos,
        state,
        1,
        Recipes.metalformerCutting,
        1,
        24,
        500);
  }

  @Override
  protected int getIdleWaterUse() {
    return (int) (this.heat / 10000.0 * 2.0 + this.getLevel().random.nextDouble());
  }
}
