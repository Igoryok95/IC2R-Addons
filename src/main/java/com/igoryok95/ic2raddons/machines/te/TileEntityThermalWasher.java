package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.*;
import ic2.core.Ic2Explosion;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalWasher extends TileEntityHeatingWaterMachine {
  private int lastWaterAmount;

  public TileEntityThermalWasher(BlockPos pos, BlockState state) {
    super(AdvMachinesBlocks.BE_THERMAL_WASHER.get(), pos, state, 3, Recipes.oreWashing, 6, 48, 500);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    // Loading an already filled tank is not a refill and must never trigger thermal shock.
    lastWaterAmount = getWaterAmount();
  }

  public boolean isThermalShockPending() {
    return lastWaterAmount == 0 && getWaterAmount() > 0 && heat >= 5000;
  }

  @Override
  protected boolean checkThermalShock() {
    if (!isThermalShockPending()) return false;
    // A protection mod may cancel the explosion; the same refill must not fire every tick.
    lastWaterAmount = getWaterAmount();
    var explosion =
        new Ic2Explosion(level, null, worldPosition, 12.0F, 0.0F, Ic2Explosion.Type.Heat);
    explosion.doExplosion();
    return true;
  }

  @Override
  protected void waterTickFinished() {
    lastWaterAmount = getWaterAmount();
  }

  @Override
  public int getHeat() {
    return heat * 100 / MAX_HEAT;
  }

  @Override
  protected int getIdleWaterUse() {
    return heat * 5 / MAX_HEAT;
  }

  @Override
  protected int getOperationWaterUse(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    // Original Advanced Machines halves the base washing water requirement (1000 -> 500 mB).
    var metadata = recipe.recipe().getMetaData();
    return metadata != null && metadata.contains("amount")
        ? Math.max(1, metadata.getInt("amount") / 2)
        : activeWaterUse;
  }
}
