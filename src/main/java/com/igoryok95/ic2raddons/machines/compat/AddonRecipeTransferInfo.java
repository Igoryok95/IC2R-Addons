package com.igoryok95.ic2raddons.machines.compat;

import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.ref.Ic2ScreenHandlers;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public record AddonRecipeTransferInfo<R>(RecipeType<R> recipeType, Class<?> machine)
    implements IRecipeTransferInfo<DynamicContainer<?>, R> {
  @Override
  public Class<? extends DynamicContainer<?>> getContainerClass() {
    return (Class) DynamicContainer.class;
  }

  @Override
  public Optional<MenuType<DynamicContainer<?>>> getMenuType() {
    return Optional.of((MenuType) Ic2ScreenHandlers.DYNAMIC_BE);
  }

  @Override
  public RecipeType<R> getRecipeType() {
    return recipeType;
  }

  @Override
  public boolean canHandle(DynamicContainer<?> menu, R recipe) {
    return machine.isInstance(menu.base);
  }

  @Override
  public List<Slot> getRecipeSlots(DynamicContainer<?> menu, R recipe) {
    return AddonMachineSlots.inputs(menu);
  }

  @Override
  public List<Slot> getInventorySlots(DynamicContainer<?> menu, R recipe) {
    return AddonMachineSlots.inventory(menu);
  }
}
