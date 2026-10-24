// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.init.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ECCreativeTab {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnergyControl.MODID);
  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
      CREATIVE_TABS.register(
          "control",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.ic2r_addons.control"))
                  .withTabsBefore(CreativeModeTabs.COMBAT)
                  .icon(() -> new ItemStack(ModItems.kit_energy.get()))
                  .displayItems(
                      (parameters, output) -> {
                        addBlock(output, ModItems.white_lamp);
                        addBlock(output, ModItems.orange_lamp);
                        addBlock(output, ModItems.howler_alarm);
                        addBlock(output, ModItems.industrial_alarm);
                        addBlock(output, ModItems.thermal_monitor);
                        addBlock(output, ModItems.range_trigger);
                        addBlock(output, ModItems.remote_thermo);
                        addBlock(output, ModItems.info_panel);
                        addBlock(output, ModItems.info_panel_extender);
                        addBlock(output, ModItems.info_panel_advanced);
                        addBlock(output, ModItems.info_panel_advanced_extender);
                        addBlock(output, ModItems.holo_panel);
                        addBlock(output, ModItems.holo_panel_extender);
                        addBlock(output, ModItems.average_counter);
                        addBlock(output, ModItems.energy_counter);
                        addBlock(output, ModItems.kit_assembler);
                        addBlock(output, ModItems.timer);
                        addItem(output, ModItems.thermometer);
                        addItem(output, ModItems.thermometer_digital);
                        addItem(output, ModItems.kit_counter);
                        addItem(output, ModItems.card_counter);
                        addItem(output, ModItems.kit_energy);
                        addItem(output, ModItems.kit_inventory);
                        addItem(output, ModItems.kit_liquid);
                        addItem(output, ModItems.kit_liquid_advanced);
                        addItem(output, ModItems.kit_redstone);
                        addItem(output, ModItems.kit_toggle);
                        CrossModLoader.addKitsToCreativeTab(output);
                        addItem(output, ModItems.card_holder);
                        addItem(output, ModItems.card_energy);
                        addItem(output, ModItems.card_energy_array);
                        addItem(output, ModItems.card_inventory);
                        addItem(output, ModItems.card_liquid);
                        addItem(output, ModItems.card_liquid_advanced);
                        addItem(output, ModItems.card_liquid_array);
                        addItem(output, ModItems.card_redstone);
                        addItem(output, ModItems.card_text);
                        addItem(output, ModItems.card_time);
                        addItem(output, ModItems.card_toggle);
                        CrossModLoader.addCardsToCreativeTab(output);
                        addItem(output, ModItems.upgrade_range);
                        addItem(output, ModItems.upgrade_color);
                        addItem(output, ModItems.upgrade_touch);
                        addItem(output, ModItems.upgrade_web);
                        addItem(output, ModItems.portable_panel);
                        addItem(output, ModItems.machine_casing);
                        addItem(output, ModItems.basic_circuit);
                        addItem(output, ModItems.advanced_circuit);
                        addItem(output, ModItems.radio_transmitter);
                        addItem(output, ModItems.strong_string);
                      })
                  .build());

  private static void addItem(CreativeModeTab.Output output, Supplier<? extends ItemLike> item) {
    output.accept(item.get());
  }

  private static void addBlock(CreativeModeTab.Output output, Supplier<? extends ItemLike> block) {
    output.accept(block.get());
  }
}
