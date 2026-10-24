package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import ic2.api.recipe.*;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCompactingRecycler extends TileEntityHeatingMachine {
  public TileEntityCompactingRecycler(BlockPos pos, BlockState state) {
    super(
        AdvMachinesBlocks.BE_COMPACTING_RECYCLER.get(),
        pos,
        state,
        1,
        level -> new CompactingRecipes());
  }

  @Override
  protected int getSpeedFactor() {
    return 12;
  }

  @Override
  public int getHeat() {
    return heat * 9;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return Ic2SoundEvents.MACHINE_RECYCLER_OPERATE;
  }

  @Override
  protected Collection<ItemStack> adjustOutput(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe,
      Collection<ItemStack> output) {
    if (!inputSlot.get().is(Ic2Items.SCRAP)
        && level.random.nextInt(TileEntityRecycler.recycleChance()) != 0) return List.of();
    return output;
  }

  private static final class CompactingRecipes
      implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(
        ItemStack input, boolean acceptTest) {
      if (input.isEmpty()) return null;
      if (input.is(Ic2Items.SCRAP)) {
        if (!acceptTest && input.getCount() < 9) return null;
        var recipe =
            new MachineRecipe<IRecipeInput, Collection<ItemStack>>(
                Recipes.inputFactory.forStack(new ItemStack(Ic2Items.SCRAP), 9),
                List.of(new ItemStack(Ic2Items.SCRAP_BOX)));
        return recipe.getResult(
            input.getCount() > 9 ? input.copyWithCount(input.getCount() - 9) : ItemStack.EMPTY);
      }
      return Recipes.recycler.apply(input, acceptTest);
    }

    @Override
    public boolean isIterable() {
      return false;
    }

    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      return List.of();
    }
  }
}
