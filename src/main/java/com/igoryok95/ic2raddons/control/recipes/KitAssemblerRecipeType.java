// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.recipes;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityKitAssembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class KitAssemblerRecipeType implements RecipeType<KitAssemblerRecipe> {
  public static final KitAssemblerRecipeType TYPE = new KitAssemblerRecipeType();

  @Override
  public String toString() {
    return EnergyControl.MODID + ":kit_assembler";
  }

  public static List<KitAssemblerRecipe> getRecipes(Level level) {
    if (level == null) return Collections.emptyList();
    List<RecipeHolder<KitAssemblerRecipe>> holders =
        level.getRecipeManager().getAllRecipesFor(TYPE);
    List<KitAssemblerRecipe> result = new ArrayList<>(holders.size());
    for (RecipeHolder<KitAssemblerRecipe> holder : holders) result.add(holder.value());
    return result;
  }

  public static KitAssemblerRecipe findRecipe(Level level, TileEntityKitAssembler te) {
    for (KitAssemblerRecipe recipe : getRecipes(level)) {
      if (recipe.isSuitable(te)) return recipe;
    }
    return null;
  }
}
