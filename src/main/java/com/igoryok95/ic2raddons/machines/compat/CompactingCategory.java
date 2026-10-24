package com.igoryok95.ic2raddons.machines.compat;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.ref.Ic2Items;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CompactingCategory implements IRecipeCategory<ItemStack> {
  public static final RecipeType<ItemStack> TYPE =
      RecipeType.create(IC2RAddons.MODID, "compacting_recycler", ItemStack.class);
  private final IDrawable background;
  private final IDrawable icon;
  private final IDrawable arrow;

  public CompactingCategory(IGuiHelper helper) {
    background = helper.createBlankDrawable(132, 48);
    icon =
        helper.createDrawableItemStack(new ItemStack(AdvMachinesBlocks.COMPACTING_RECYCLER.get()));
    arrow = helper.getSlotDrawable();
  }

  @Override
  public RecipeType<ItemStack> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("block.ic2r_addons.compacting_recycler");
  }

  @Override
  public IDrawable getBackground() {
    return background;
  }

  @Override
  public IDrawable getIcon() {
    return icon;
  }

  @Override
  public int getWidth() {
    return 132;
  }

  @Override
  public int getHeight() {
    return 48;
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ItemStack input, IFocusGroup focuses) {
    builder
        .addSlot(RecipeIngredientRole.INPUT, 10, 5)
        .setBackground(arrow, -1, -1)
        .addItemStack(input);
    builder
        .addSlot(RecipeIngredientRole.OUTPUT, 106, 5)
        .setBackground(arrow, -1, -1)
        .addItemStack(new ItemStack(Ic2Items.SCRAP_BOX));
  }

  @Override
  public void draw(ItemStack input, IRecipeSlotsView slots, GuiGraphics g, double x, double y) {
    g.fill(42, 11, 80, 15, 0xff777777);
    g.fill(78, 8, 81, 18, 0xff777777);
    g.drawString(
        Minecraft.getInstance().font,
        Component.translatable("jei.ic2r_addons.machines.compacting"),
        5,
        34,
        0xff555555,
        false);
  }
}
