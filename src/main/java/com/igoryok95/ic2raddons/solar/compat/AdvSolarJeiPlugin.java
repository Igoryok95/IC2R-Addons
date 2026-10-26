package com.igoryok95.ic2raddons.solar.compat;

import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import com.igoryok95.ic2raddons.solar.recipe.MolecularTransformerRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI integration. Registers a custom Molecular Transformer recipe category (the transformer is the
 * only machine here with its own recipe type rather than reusing an IC2 category), feeds it every
 * recipe from {@link MolecularTransformerRecipes}, and lists the transformer block as its catalyst.
 *
 * <p>Loaded only when JEI is present (JEI scans for {@link JeiPlugin}).
 */
@JeiPlugin
public class AdvSolarJeiPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return ResourceLocation.fromNamespaceAndPath(IC2RAddons.MODID, "jei");
  }

  @Override
  public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
    com.igoryok95.ic2raddons.machines.client.IntegrationClientSmoke.runtime(runtime);
    JeiSearchSmoke.verify(runtime);
  }

  @Override
  public void registerRecipeTransferHandlers(
      mezz.jei.api.registration.IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
        new com.igoryok95.ic2raddons.machines.compat.AddonRecipeTransferInfo<>(
            MolecularTransformerCategory.TYPE,
            com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer.class));
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(
        new MolecularTransformerCategory(registration.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    IC2RAddons.LOGGER.info("Registering Molecular Transformer JEI recipes");
    registration.addRecipes(
        MolecularTransformerCategory.TYPE,
        MolecularTransformerRecipes.all(
            java.util.Objects.requireNonNull(net.minecraft.client.Minecraft.getInstance().level)));
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(
        new ItemStack(AdvSolarBlocks.MOLECULAR_TRANSFORMER.get()),
        MolecularTransformerCategory.TYPE);
  }
}
