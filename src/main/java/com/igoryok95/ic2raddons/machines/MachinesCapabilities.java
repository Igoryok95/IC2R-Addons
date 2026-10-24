package com.igoryok95.ic2raddons.machines;

import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingWaterMachine;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.wrapper.*;

/** IC2 registers capabilities for its own types only; addon types need explicit registration. */
public final class MachinesCapabilities {
  public static void register(RegisterCapabilitiesEvent event) {
    for (var type : AdvMachinesBlocks.BLOCK_ENTITIES.getEntries()) {
      event.registerBlockEntity(
          Capabilities.ItemHandler.BLOCK,
          type.get(),
          (be, side) ->
              be instanceof WorldlyContainer inventory
                  ? side == null ? new InvWrapper(inventory) : new SidedInvWrapper(inventory, side)
                  : null);
      event.registerBlockEntity(
          Capabilities.FluidHandler.BLOCK,
          type.get(),
          (be, side) ->
              be instanceof TileEntityHeatingWaterMachine water ? new WaterHandler(water) : null);
    }
  }

  private record WaterHandler(TileEntityHeatingWaterMachine machine) implements IFluidHandler {
    @Override
    public int getTanks() {
      return 1;
    }

    @Override
    public int getTankCapacity(int tank) {
      return tank == 0 ? machine.tank.getCapacity() : 0;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
      return tank == 0 && machine.getWaterAmount() > 0
          ? new FluidStack(Fluids.WATER, machine.getWaterAmount())
          : FluidStack.EMPTY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
      return tank == 0 && stack.is(Fluids.WATER);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      if (resource.isEmpty() || !isFluidValid(0, resource)) return 0;
      int filled =
          machine.tank.fillMb(
              Ic2FluidStack.create(Fluids.WATER, resource.getAmount()), action.simulate());
      if (filled > 0 && action.execute()) machine.setChanged();
      return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int amount, FluidAction action) {
      return FluidStack.EMPTY;
    }
  }

  private MachinesCapabilities() {}
}
