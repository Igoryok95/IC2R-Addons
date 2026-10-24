package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySingularityCompressor extends TileEntityHeatingMachine {
  public TileEntitySingularityCompressor(BlockPos pos, BlockState state) {
    super(AdvMachinesBlocks.BE_SINGULARITY_COMPRESSOR.get(), pos, state, 1, Recipes.compressor);
  }

  @Override
  public int getHeat() {
    return this.heat * 9;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return Ic2SoundEvents.MACHINE_COMPRESSOR_OPERATE;
  }
}
