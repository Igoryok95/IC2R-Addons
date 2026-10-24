package com.igoryok95.ic2raddons.machines.te;

import ic2.api.recipe.*;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.*;
import ic2.core.network.GuiSynced;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityHeatingWaterMachine extends TileEntityHeatingMachine {
  protected final Fluids fluids = addComponent(new Fluids(this));
  @GuiSynced public final Fluids.InternalFluidTank tank;
  public final InvSlotConsumableLiquid fluidSlot;
  public final InvSlotOutput cellSlot;
  protected final int activeWaterUse;

  public TileEntityHeatingWaterMachine(
      BlockEntityType<? extends TileEntityHeatingWaterMachine> type,
      BlockPos pos,
      BlockState state,
      int outputs,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager,
      int idleEU,
      int activeEU,
      int activeWaterUse) {
    super(type, pos, state, outputs, manager, idleEU, activeEU);
    this.activeWaterUse = activeWaterUse;
    tank =
        fluids.addTankInsert(
            "tank", 8000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
    fluidSlot =
        new InvSlotConsumableLiquidByList(
            this, "fluid", 1, net.minecraft.world.level.material.Fluids.WATER);
    cellSlot = new InvSlotOutput(this, "cell", 1);
  }

  @Override
  protected void updateEntityServer() {
    if (getWaterAmount() < tank.getCapacity()) fluidSlot.processIntoTank(tank, cellSlot);
    if (checkThermalShock()) return;
    super.updateEntityServer();
    waterTickFinished();
  }

  protected boolean checkThermalShock() {
    return false;
  }

  protected void waterTickFinished() {}

  public int getWaterAmount() {
    return tank.getFluidAmount();
  }

  @Override
  protected boolean hasWater(int amount) {
    return amount <= getWaterAmount();
  }

  @Override
  protected void consumeWater(int amount) {
    // External draining is disabled; internal process consumption must bypass that restriction.
    if (amount > 0) tank.drainMbUnchecked(amount, false);
  }

  @Override
  protected int getOperationWaterUse(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    return activeWaterUse;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    Set<UpgradableProperty> result = super.getUpgradableProperties();
    result.add(UpgradableProperty.FluidConsuming);
    return result;
  }
}
