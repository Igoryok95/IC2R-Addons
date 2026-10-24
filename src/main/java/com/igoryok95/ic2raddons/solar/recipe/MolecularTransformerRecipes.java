package com.igoryok95.ic2raddons.solar.recipe;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MolecularTransformerRecipes {
  public static final DeferredRegister<RecipeType<?>> TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, IC2RAddons.MODID);
  public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, IC2RAddons.MODID);
  public static final Supplier<RecipeType<MTRecipe>> TYPE =
      TYPES.register("molecular_transformer", () -> new RecipeType<MTRecipe>() {});
  public static final Supplier<RecipeSerializer<MTRecipe>> SERIALIZER =
      SERIALIZERS.register("molecular_transformer", MTRecipe.Serializer::new);

  public static MTRecipe find(ItemStack stack, Level level) {
    return level
        .getRecipeManager()
        .getRecipeFor(TYPE.get(), new SingleRecipeInput(stack), level)
        .map(RecipeHolder::value)
        .orElse(null);
  }

  public static List<MTRecipe> all(Level level) {
    return level.getRecipeManager().getAllRecipesFor(TYPE.get()).stream()
        .map(RecipeHolder::value)
        .toList();
  }

  private MolecularTransformerRecipes() {}
}
