package com.igoryok95.ic2raddons.solar.recipe;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class MTRecipe implements Recipe<SingleRecipeInput> {
  public final Ingredient input;
  public final ItemStack output;
  public final int totalEU;

  public MTRecipe(Ingredient input, ItemStack output, int energy) {
    this.input = input;
    this.output = output.copy();
    this.totalEU = energy;
  }

  public boolean matches(ItemStack stack) {
    return !stack.isEmpty() && input.test(stack);
  }

  public boolean matches(SingleRecipeInput in, Level level) {
    return matches(in.item());
  }

  public ItemStack assemble(SingleRecipeInput in, HolderLookup.Provider registries) {
    return output.copy();
  }

  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return output.copy();
  }

  public boolean isSpecial() {
    return true;
  }

  public boolean canCraftInDimensions(int w, int h) {
    return true;
  }

  public RecipeSerializer<?> getSerializer() {
    return MolecularTransformerRecipes.SERIALIZER.get();
  }

  public RecipeType<?> getType() {
    return MolecularTransformerRecipes.TYPE.get();
  }

  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, input);
  }

  public static final class Serializer implements RecipeSerializer<MTRecipe> {
    private static final MapCodec<MTRecipe> CODEC =
        RecordCodecBuilder.mapCodec(
            i ->
                i.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(r -> r.input),
                        ItemStack.STRICT_CODEC.fieldOf("output").forGetter(r -> r.output),
                        Codec.intRange(1, Integer.MAX_VALUE)
                            .fieldOf("energy")
                            .forGetter(r -> r.totalEU))
                    .apply(i, MTRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, MTRecipe> STREAM =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.input,
            ItemStack.STREAM_CODEC,
            r -> r.output,
            ByteBufCodecs.VAR_INT,
            r -> r.totalEU,
            MTRecipe::new);

    public MapCodec<MTRecipe> codec() {
      return CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, MTRecipe> streamCodec() {
      return STREAM;
    }
  }
}
