package com.igoryok95.ic2raddons.machines.te;

import ic2.api.recipe.*;
import ic2.core.block.comp.Energy;
import ic2.core.ref.Ic2Items;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;

/**
 * Prepares real processing inputs in the opt-in development client, without changing machine rules.
 */
public final class MachineClientFixtures {
  public static void prepare(TileEntityHeatingMachine machine) {
    if (!Boolean.getBoolean("ic2r_addons.machinesSmoke")
        && !Boolean.getBoolean("ic2r_addons.restorationSmoke")
        && !Boolean.getBoolean("ic2r_addons.warmupSmoke")
        && !Boolean.getBoolean("ic2r_addons.warmupLayoutSmoke"))
      throw new IllegalStateException("Development fixture is disabled");
    if (machine instanceof TileEntityHeatingWaterMachine water) {
      water.tank.fillMb(
          ic2.core.fluid.Ic2FluidStack.create(
              net.minecraft.world.level.material.Fluids.WATER, 8000),
          false);
      water.waterTickFinished();
    }
    if (machine instanceof TileEntityCompactingRecycler) {
      machine.inputSlot.put(new ItemStack(Ic2Items.SCRAP, 64));
      machine.outputSlot.put(new ItemStack(Ic2Items.SCRAP_BOX));
    } else {
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          getter =
              machine instanceof TileEntityRotaryMacerator
                  ? Recipes.macerator
                  : machine instanceof TileEntitySingularityCompressor
                      ? Recipes.compressor
                      : machine instanceof TileEntityCentrifugeExtractor
                          ? Recipes.extractor
                          : machine instanceof TileEntityLiquescentExtruder
                              ? Recipes.metalformerExtruding
                              : machine instanceof TileEntityImpellerizedRoller
                                  ? Recipes.metalformerRolling
                                  : machine instanceof TileEntityWaterJetCutter
                                      ? Recipes.metalformerCutting
                                      : Recipes.oreWashing;
      for (var recipe : getter.get(machine.getLevel()).getRecipes()) {
        if (recipe.getOutput().isEmpty() || recipe.getInput().getInputs().isEmpty()) continue;
        var input = recipe.getInput().getInputs().getFirst();
        machine.inputSlot.put(
            input.copyWithCount(Math.max(recipe.getInput().getAmount(), input.getMaxStackSize())));
        machine.outputSlot.add(ic2.core.util.StackUtil.copy(recipe.getOutput()));
        break;
      }
    }
    machine.dischargeSlot.put(new ItemStack(Ic2Items.RE_BATTERY));
    machine.getComponent(Energy.class).useEnergy(machine.getEnergy());
    machine.getComponent(Energy.class).addEnergy(5000);
    var saved = machine.saveWithFullMetadata(machine.getLevel().registryAccess());
    saved.putShort("heat", (short) 2400);
    saved.putInt("progress", 0);
    machine.loadWithComponents(saved, machine.getLevel().registryAccess());
  }

  private MachineClientFixtures() {}
}
