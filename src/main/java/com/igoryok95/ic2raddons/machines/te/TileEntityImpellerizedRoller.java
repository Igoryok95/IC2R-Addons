package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.Recipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityImpellerizedRoller extends TileEntityHeatingMachine {
  public TileEntityImpellerizedRoller(BlockPos pos, BlockState state) {
    super(
        AdvMachinesBlocks.BE_IMPELLERIZED_ROLLER.get(),
        pos,
        state,
        1,
        Recipes.metalformerRolling,
        1,
        24);
  }
}
