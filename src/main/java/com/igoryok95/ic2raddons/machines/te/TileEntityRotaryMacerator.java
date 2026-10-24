package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TileEntityRotaryMacerator extends TileEntityHeatingMachine {
  public TileEntityRotaryMacerator(BlockPos pos, BlockState state) {
    super(AdvMachinesBlocks.BE_ROTARY_MACERATOR.get(), pos, state, 2, Recipes.macerator);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    Level world = this.getLevel();
    if (this.getActive() && world.random.nextInt(8) == 0) {
      for (int i = 0; i < 4; i++) {
        double x = this.worldPosition.getX() + 0.5 + world.random.nextFloat() * 0.6 - 0.3;
        double y = this.worldPosition.getY() + 1 + world.random.nextFloat() * 0.2 - 0.1;
        double z = this.worldPosition.getZ() + 0.5 + world.random.nextFloat() * 0.6 - 0.3;
        world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
      }
    }
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return Ic2SoundEvents.MACHINE_MACERATOR_OPERATE;
  }
}
