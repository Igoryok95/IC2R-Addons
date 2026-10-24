// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.crossmod.jei;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.gui.GuiKitAssembler;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.recipes.KitAssemblerRecipeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class CrossJEI implements IModPlugin {
  private static final ResourceLocation id =
      ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "jei_plugin");

  @Override
  public ResourceLocation getPluginUid() {
    return id;
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registry) {
    IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
    registry.addRecipeCategories(new IRecipeCategory[] {new KitAssemblerRecipeCategory(guiHelper)});
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registry) {
    registry.addRecipeClickArea(
        GuiKitAssembler.class, 87, 35, 22, 15, KitAssemblerRecipeCategory.recipeType);
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
    registry.addRecipeTransferHandler(
        new KitAssemblerTransferHandler(registry.getTransferHelper()),
        KitAssemblerRecipeCategory.recipeType);
  }

  @Override
  public void registerRecipes(IRecipeRegistration registry) {
    registry.addRecipes(
        KitAssemblerRecipeCategory.recipeType,
        KitAssemblerRecipeType.getRecipes(Minecraft.getInstance().level));
    registerItem(registry, ModItems.white_lamp.get(), "control.ic2r_addons.jei.blockLightWhite");
    registerItem(registry, ModItems.orange_lamp.get(), "control.ic2r_addons.jei.blockLightOrange");
    registerItem(registry, ModItems.howler_alarm.get(), "control.ic2r_addons.jei.blockHowlerAlarm");
    registerItem(
        registry, ModItems.industrial_alarm.get(), "control.ic2r_addons.jei.blockIndustrialAlarm");
    registerItem(
        registry, ModItems.thermal_monitor.get(), "control.ic2r_addons.jei.blockThermalMonitor");
    registerItem(registry, ModItems.info_panel.get(), "control.ic2r_addons.jei.blockInfoPanel");
    registerItem(
        registry,
        ModItems.info_panel_extender.get(),
        "control.ic2r_addons.jei.blockInfoPanelExtender");
    registerItem(
        registry,
        ModItems.info_panel_advanced.get(),
        "control.ic2r_addons.jei.blockInfoPanelAdvanced");
    registerItem(
        registry,
        ModItems.info_panel_advanced_extender.get(),
        "control.ic2r_addons.jei.blockInfoPanelAdvancedExtender");
    registerItem(registry, ModItems.holo_panel.get(), "control.ic2r_addons.jei.blockHoloPanel");
    registerItem(
        registry, ModItems.kit_assembler.get(), "control.ic2r_addons.jei.blockKitAssembler");

    registerItem(registry, ModItems.upgrade_color.get(), "control.ic2r_addons.jei.upgradeColor");
    registerItem(registry, ModItems.upgrade_range.get(), "control.ic2r_addons.jei.upgradeRange");
    registerItem(registry, ModItems.upgrade_touch.get(), "control.ic2r_addons.jei.upgradeTouch");
    registerItem(registry, ModItems.upgrade_web.get(), "control.ic2r_addons.jei.upgradeWeb");

    registerItem(registry, ModItems.kit_app_eng, "control.ic2r_addons.jei.kitAppEng");
    registerItem(registry, ModItems.kit_energy.get(), "control.ic2r_addons.jei.kitEnergy");
    registerItem(registry, ModItems.kit_liquid.get(), "control.ic2r_addons.jei.kitLiquid");
    registerItem(
        registry, ModItems.kit_liquid_advanced.get(), "control.ic2r_addons.jei.kitLiquidAdv");
    registerItem(registry, ModItems.kit_toggle.get(), "control.ic2r_addons.jei.kitToggle");

    registerItem(registry, ModItems.card_energy.get(), "control.ic2r_addons.jei.cards");
    registerItem(registry, ModItems.card_inventory.get(), "control.ic2r_addons.jei.cards");
    registerItem(registry, ModItems.card_liquid_advanced.get(), "control.ic2r_addons.jei.cards");
    registerItem(registry, ModItems.card_liquid.get(), "control.ic2r_addons.jei.cards");
    registerItem(registry, ModItems.card_redstone.get(), "control.ic2r_addons.jei.cards");
    registerItem(registry, ModItems.card_toggle.get(), "control.ic2r_addons.jei.cards");

    registerItem(registry, ModItems.card_holder.get(), "control.ic2r_addons.jei.itemCardHolder");
    registerItem(
        registry, ModItems.portable_panel.get(), "control.ic2r_addons.jei.itemPortablePanel");
  }

  private void registerItem(IRecipeRegistration registry, ItemLike item, String name) {
    if (item != null)
      registry.addIngredientInfo(
          new ItemStack(item), VanillaTypes.ITEM_STACK, Component.literal(I18n.get(name)));
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
    registry.addRecipeCatalyst(
        new ItemStack(ModItems.kit_assembler.get()), KitAssemblerRecipeCategory.recipeType);
  }
}
