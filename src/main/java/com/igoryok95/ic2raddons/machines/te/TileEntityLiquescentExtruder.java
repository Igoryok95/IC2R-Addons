package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLiquescentExtruder extends TileEntityHeatingMachine {
  public TileEntityLiquescentExtruder(BlockPos pos, BlockState state) {
    super(
        AdvMachinesBlocks.BE_LIQUESCENT_EXTRUDER.get(),
        pos,
        state,
        3,
        Recipes.metalformerExtruding,
        1,
        24);
  }

  @Override
  public int getHeat() {
    return (this.heat * 100 + 5000) / 10000;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return Ic2SoundEvents.MACHINE_EXTRACTOR_OPERATE;
  }
}
