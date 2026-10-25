package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import ic2.core.experimental.RestoredVillagers;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/** Addons expands native IC2 professions; the core never links addon classes or items. */
@EventBusSubscriber(modid = "ic2r_addons")
public final class VillagerCompatibility {
  @SubscribeEvent
  public static void trades(VillagerTradesEvent event) {
    var id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
    if (!id.getNamespace().equals("ic2")) return;
    var trades = event.getTrades();
    switch (id.getPath()) {
      case "electric" -> {
        trades
            .get(3)
            .add(
                RestoredVillagers.exchange(
                    8, Ic2Items.CIRCUIT, 2, ModItems.basic_circuit.get(), 1));
        trades
            .get(3)
            .add(
                RestoredVillagers.exchange(
                    8,
                    Ic2Items.STEEL_PLATE,
                    2,
                    com.igoryok95.ic2raddons.machines.AdvMachinesBlocks.SHARP_PLATE.get(),
                    1));
        trades
            .get(4)
            .add(
                RestoredVillagers.exchange(
                    16, Ic2Items.ADVANCED_CIRCUIT, 2, ModItems.advanced_circuit.get(), 1));
        trades
            .get(5)
            .add(
                RestoredVillagers.exchange(
                    32, Ic2Items.IRIDIUM, 1, AdvSolarBlocks.ITEM_ADVANCED_SOLAR_PANEL.get(), 1));
        trades
            .get(5)
            .add(
                RestoredVillagers.exchange(
                    16, Ic2Items.ADVANCED_CIRCUIT, 2, GraviItems.SUPERCONDUCTOR_COVER.get(), 1));
      }
      case "nuclear" -> {
        trades.get(2).add(RestoredVillagers.sell(4, ModItems.thermometer.get(), 1));
        trades
            .get(3)
            .add(
                RestoredVillagers.exchange(
                    8, Ic2Items.CIRCUIT, 1, ModItems.thermal_monitor_item.get(), 1));
        trades
            .get(4)
            .add(
                RestoredVillagers.exchange(
                    12, Ic2Items.ADVANCED_CIRCUIT, 1, ModItems.info_panel_item.get(), 1));
        trades
            .get(5)
            .add(
                RestoredVillagers.exchange(
                    16, Ic2Items.ADVANCED_CIRCUIT, 2, ModItems.portable_panel.get(), 1));
      }
      case "greg" -> {
        trades
            .get(4)
            .add(
                RestoredVillagers.exchange(
                    32, GraviItems.COOLING_CORE.get(), 2, GraviItems.ADVANCED_CHAINSAW.get(), 1));
        trades
            .get(5)
            .add(
                RestoredVillagers.exchange(
                    48, GraviItems.COOLING_CORE.get(), 2, GraviItems.ADVANCED_DRILL.get(), 1));
        trades
            .get(5)
            .add(
                RestoredVillagers.exchange(
                    48, GraviItems.ENGINE_BOOSTER.get(), 2, GraviItems.ADVANCED_LAPPACK.get(), 1));
      }
      default -> {}
    }
  }

  private VillagerCompatibility() {}
}
