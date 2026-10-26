package com.igoryok95.ic2raddons.machines.compat;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.ref.Ic2Items;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import mezz.jei.api.*;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@JeiPlugin
public final class MachinesJeiPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return IC2RAddons.id("advanced_machines");
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
    catalyst(r, AdvMachinesBlocks.ROTARY_MACERATOR.get(), "macerator");
    catalyst(r, AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(), "compressor");
    catalyst(r, AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(), "extractor");
    catalyst(r, AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(), "metal_former_extruding");
    catalyst(r, AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(), "metal_former_rolling");
    catalyst(r, AdvMachinesBlocks.WATER_JET_CUTTER.get(), "metal_former_cutting");
    catalyst(r, AdvMachinesBlocks.THERMAL_WASHER.get(), "ore_washer");
    r.addRecipeCatalyst(
        new ItemStack(AdvMachinesBlocks.COMPACTING_RECYCLER.get()), CompactingCategory.TYPE);
  }

  private static void catalyst(IRecipeCatalystRegistration r, Block block, String category) {
    r.addRecipeCatalyst(
        new ItemStack(block), RecipeType.create("ic2", category, IORecipeWrapper.class));
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
        new AddonRecipeTransferInfo<>(
            CompactingCategory.TYPE,
            com.igoryok95.ic2raddons.machines.te.TileEntityCompactingRecycler.class));
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration r) {
    r.addRecipeCategories(new CompactingCategory(r.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration r) {
    r.addRecipes(CompactingCategory.TYPE, java.util.List.of(new ItemStack(Ic2Items.SCRAP, 9)));
    r.addItemStackInfo(
        new ItemStack(AdvMachinesBlocks.COMPACTING_RECYCLER.get()),
        Component.translatable("tooltip.ic2r_addons.machines.recycler"));
    r.addItemStackInfo(
        new ItemStack(AdvMachinesBlocks.THERMAL_WASHER.get()),
        Component.translatable("tooltip.ic2r_addons.machines.thermal_shock"));
  }
}
