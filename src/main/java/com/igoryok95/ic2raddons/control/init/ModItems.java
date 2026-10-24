// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.init;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.blocks.*;
import com.igoryok95.ic2raddons.control.items.*;
import com.igoryok95.ic2raddons.control.items.cards.*;
import com.igoryok95.ic2raddons.control.items.kits.*;
import com.igoryok95.ic2raddons.control.recipes.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(EnergyControl.MODID);
  public static final DeferredBlock<Block> energy_counter =
      BLOCKS.registerBlock("energy_counter", EnergyCounter::new);
  public static final DeferredBlock<Block> average_counter =
      BLOCKS.registerBlock("average_counter", EnergyCounter::new);
  public static final DeferredBlock<Block> white_lamp =
      BLOCKS.registerBlock("white_lamp", properties -> new BlockLight(properties, MapColor.SNOW));
  public static final DeferredBlock<Block> orange_lamp =
      BLOCKS.registerBlock(
          "orange_lamp", properties -> new BlockLight(properties, MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> howler_alarm =
      BLOCKS.registerBlock("howler_alarm", HowlerAlarm::new);
  public static final DeferredBlock<Block> industrial_alarm =
      BLOCKS.registerBlock("industrial_alarm", IndustrialAlarm::new);
  public static final DeferredBlock<Block> thermal_monitor =
      BLOCKS.registerBlock("thermal_monitor", ThermalMonitor::new);
  public static final DeferredBlock<Block> range_trigger =
      BLOCKS.registerBlock("range_trigger", RangeTrigger::new);
  public static final DeferredBlock<Block> remote_thermo =
      BLOCKS.registerBlock("remote_thermo", RemoteThermalMonitor::new);
  public static final DeferredBlock<Block> info_panel =
      BLOCKS.registerBlock("info_panel", InfoPanel::new);
  public static final DeferredBlock<Block> info_panel_extender =
      BLOCKS.registerBlock("info_panel_extender", InfoPanelExtender::new);
  public static final DeferredBlock<Block> info_panel_advanced =
      BLOCKS.registerBlock("info_panel_advanced", AdvancedInfoPanel::new);
  public static final DeferredBlock<Block> info_panel_advanced_extender =
      BLOCKS.registerBlock("info_panel_advanced_extender", AdvancedInfoPanelExtender::new);
  public static final DeferredBlock<Block> holo_panel =
      BLOCKS.registerBlock("holo_panel", HoloPanel::new);
  public static final DeferredBlock<Block> holo_panel_extender =
      BLOCKS.registerBlock("holo_panel_extender", HoloPanelExtender::new);
  public static final DeferredBlock<Block> kit_assembler =
      BLOCKS.registerBlock("kit_assembler", KitAssembler::new);
  public static final DeferredBlock<Block> timer = BLOCKS.registerBlock("timer", TimerBlock::new);

  public static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(EnergyControl.MODID);
  public static final DeferredItem<BlockItem> energy_counter_item =
      ITEMS.registerSimpleBlockItem(energy_counter);
  public static final DeferredItem<BlockItem> average_counter_item =
      ITEMS.registerSimpleBlockItem(average_counter);
  public static final DeferredItem<BlockItem> white_lamp_item =
      ITEMS.registerSimpleBlockItem(white_lamp);
  public static final DeferredItem<BlockItem> orange_lamp_item =
      ITEMS.registerSimpleBlockItem(orange_lamp);
  public static final DeferredItem<BlockItem> howler_alarm_item =
      ITEMS.registerSimpleBlockItem(howler_alarm);
  public static final DeferredItem<BlockItem> industrial_alarm_item =
      ITEMS.registerSimpleBlockItem(industrial_alarm);
  public static final DeferredItem<BlockItem> thermal_monitor_item =
      ITEMS.registerSimpleBlockItem(thermal_monitor);
  public static final DeferredItem<BlockItem> info_panel_item =
      ITEMS.registerSimpleBlockItem(info_panel);
  public static final DeferredItem<BlockItem> info_panel_extender_item =
      ITEMS.registerSimpleBlockItem(info_panel_extender);
  public static final DeferredItem<BlockItem> info_panel_advanced_item =
      ITEMS.registerSimpleBlockItem(info_panel_advanced);
  public static final DeferredItem<BlockItem> info_panel_advanced_extender_item =
      ITEMS.registerSimpleBlockItem(info_panel_advanced_extender);
  public static final DeferredItem<BlockItem> holo_panel_item =
      ITEMS.registerSimpleBlockItem(holo_panel);
  public static final DeferredItem<BlockItem> holo_panel_extender_item =
      ITEMS.registerSimpleBlockItem(holo_panel_extender);
  public static final DeferredItem<BlockItem> range_trigger_item =
      ITEMS.registerSimpleBlockItem(range_trigger);
  public static final DeferredItem<BlockItem> remote_thermo_item =
      ITEMS.registerSimpleBlockItem(remote_thermo);
  public static final DeferredItem<BlockItem> kit_assembler_item =
      ITEMS.registerSimpleBlockItem(kit_assembler);
  public static final DeferredItem<BlockItem> timer_item = ITEMS.registerSimpleBlockItem(timer);

  public static final DeferredItem<Item> thermometer =
      ITEMS.registerItem("thermometer", ItemThermometer::new);
  public static final DeferredItem<Item> thermometer_digital =
      ITEMS.registerItem("thermometer_digital", ItemThermometer.Digital::new);
  public static final DeferredItem<Item> card_counter =
      ITEMS.registerItem("card_counter", ItemCardCounter::new);
  public static final DeferredItem<Item> kit_counter =
      ITEMS.registerItem("kit_counter", ItemKitCounter::new);
  public static final DeferredItem<Item> kit_energy =
      ITEMS.registerItem("kit_energy", ItemKitEnergy::new);
  public static final DeferredItem<Item> kit_inventory =
      ITEMS.registerItem("kit_inventory", ItemKitInventory::new);
  public static final DeferredItem<Item> kit_liquid =
      ITEMS.registerItem("kit_liquid", ItemKitLiquid::new);
  public static final DeferredItem<Item> kit_liquid_advanced =
      ITEMS.registerItem("kit_liquid_advanced", ItemKitLiquidAdvanced::new);
  public static final DeferredItem<Item> kit_redstone =
      ITEMS.registerItem("kit_redstone", ItemKitRedstone::new);
  public static final DeferredItem<Item> kit_toggle =
      ITEMS.registerItem("kit_toggle", ItemKitToggle::new);
  public static Item kit_adv_generators;
  public static Item kit_app_eng;
  public static Item kit_ic2;
  public static Item kit_immersive_engineering;
  public static Item kit_industrial_reborn;
  public static Item kit_mekanism;
  public static Item kit_thermal_expansion;
  public static final DeferredItem<Item> card_holder =
      ITEMS.registerItem("card_holder", ItemCardHolder::new);
  public static final DeferredItem<Item> card_energy =
      ITEMS.registerItem("card_energy", ItemCardEnergy::new);
  public static final DeferredItem<Item> card_energy_array =
      ITEMS.registerItem("card_energy_array", ItemCardEnergyArray::new);
  public static final DeferredItem<Item> card_inventory =
      ITEMS.registerItem("card_inventory", ItemCardInventory::new);
  public static final DeferredItem<Item> card_liquid =
      ITEMS.registerItem("card_liquid", ItemCardLiquid::new);
  public static final DeferredItem<Item> card_liquid_advanced =
      ITEMS.registerItem("card_liquid_advanced", ItemCardLiquidAdvanced::new);
  public static final DeferredItem<Item> card_liquid_array =
      ITEMS.registerItem("card_liquid_array", ItemCardLiquidArray::new);
  public static final DeferredItem<Item> card_redstone =
      ITEMS.registerItem("card_redstone", ItemCardRedstone::new);
  public static final DeferredItem<Item> card_text =
      ITEMS.registerItem("card_text", ItemCardText::new);
  public static final DeferredItem<Item> card_time =
      ITEMS.registerItem("card_time", ItemCardTime::new);
  public static final DeferredItem<Item> card_toggle =
      ITEMS.registerItem("card_toggle", ItemCardToggle::new);
  public static Item card_adv_generators;
  public static Item card_app_eng;
  public static Item card_app_eng_inv;
  public static Item card_ic2;
  public static Item card_immersive_engineering;
  public static Item card_industrial_reborn;
  public static Item card_mekanism;
  public static Item card_thermal_expansion;
  public static final DeferredItem<Item> upgrade_range = ITEMS.registerSimpleItem("upgrade_range");
  public static final DeferredItem<Item> upgrade_color = ITEMS.registerSimpleItem("upgrade_color");
  public static final DeferredItem<Item> upgrade_touch = ITEMS.registerSimpleItem("upgrade_touch");
  public static final DeferredItem<Item> upgrade_web = ITEMS.registerSimpleItem("upgrade_web");
  public static final DeferredItem<Item> portable_panel =
      ITEMS.registerItem("portable_panel", ItemPortablePanel::new);
  public static final java.util.function.Supplier<Item> machine_casing =
      () -> ic2.core.ref.Ic2Items.MACHINE;
  public static final java.util.function.Supplier<Item> basic_circuit =
      () -> ic2.core.ref.Ic2Items.CIRCUIT;
  public static final java.util.function.Supplier<Item> advanced_circuit =
      () -> ic2.core.ref.Ic2Items.ADVANCED_CIRCUIT;
  public static final DeferredItem<Item> radio_transmitter =
      ITEMS.registerSimpleItem("radio_transmitter");
  public static final DeferredItem<Item> strong_string = ITEMS.registerSimpleItem("strong_string");

  public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, EnergyControl.MODID);
  public static final DeferredHolder<RecipeType<?>, KitAssemblerRecipeType> KIT_ASSEMBLER_TYPE =
      RECIPE_TYPES.register("kit_assembler", () -> KitAssemblerRecipeType.TYPE);

  public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, EnergyControl.MODID);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StorageArrayRecipe>>
      ARRAY_SERIALIZER = RECIPE_SERIALIZERS.register("array", ArrayRecipeSerializer::create);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KitAssemblerRecipe>>
      KIT_ASSEMBLER_SERIALIZER =
          RECIPE_SERIALIZERS.register("kit_assembler", () -> KitAssemblerRecipe.SERIALIZER);
}
