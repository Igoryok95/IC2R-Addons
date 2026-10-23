package com.chocohead.advsolar.compat;

import com.chocohead.advsolar.AdvSolarBlocks;
import com.chocohead.advsolar.AdvancedSolarPanels;
import com.chocohead.advsolar.recipe.MTRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A JEI recipe category for the Molecular Transformer: input item → output item, plus the EU cost.
 */
public class MolecularTransformerCategory implements IRecipeCategory<MTRecipe> {
  public static final RecipeType<MTRecipe> TYPE =
      RecipeType.create(AdvancedSolarPanels.MODID, "molecular_transformer", MTRecipe.class);

  private static final int WIDTH = 132;
  private static final int HEIGHT = 46;

  private final IDrawable background;
  private final IDrawable icon;

  public MolecularTransformerCategory(IGuiHelper guiHelper) {
    this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
    this.icon =
        guiHelper.createDrawableItemStack(
            new ItemStack(AdvSolarBlocks.MOLECULAR_TRANSFORMER.get()));
  }

  @Override
  public RecipeType<MTRecipe> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("block.advanced_solar_panels.molecular_transformer");
  }

  @Override
  public IDrawable getBackground() {
    return this.background;
  }

  @Override
  public int getWidth() {
    return WIDTH;
  }

  @Override
  public int getHeight() {
    return HEIGHT;
  }

  @Override
  public IDrawable getIcon() {
    return this.icon;
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, MTRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(recipe.input);
    builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 21, 5).addItemStack(recipe.output);
  }

  @Override
  public void draw(
      MTRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
    String energy =
        String.format(
            "%s %,d %s",
            Component.translatable("advanced_solar_panels.gui.energyPerOperation").getString(),
            recipe.totalEU,
            Component.translatable("ic2.generic.text.EU").getString());
    var font = Minecraft.getInstance().font;
    int x = (WIDTH - font.width(energy)) / 2;
    graphics.drawString(font, energy, x, HEIGHT - 10, 0xFF555555, false);
  }
}
