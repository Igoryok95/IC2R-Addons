package com.igoryok95.ic2raddons.solar;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Original ASP/GraviSuite rarities, applied after inherited IC2R constructors set their defaults.
 */
public final class ItemPresentation {
  private ItemPresentation() {}

  public static void apply(ModifyDefaultComponentsEvent event) {
    rarity(
        event,
        Rarity.UNCOMMON,
        "advanced_solar_panel",
        "advanced_solar_helmet",
        "rotary_macerator",
        "singularity_compressor",
        "centrifuge_extractor",
        "compacting_recycler",
        "liquescent_extruder",
        "impellerized_roller",
        "water_jet_cutter",
        "thermal_washer",
        "advanced_jetpack",
        "advanced_nano",
        "advanced_nano_leggings",
        "advanced_nano_boots",
        "advanced_chainsaw",
        "gravitool");
    rarity(
        event,
        Rarity.RARE,
        "hybrid_solar_panel",
        "hybrid_solar_helmet",
        "molecular_transformer",
        "advanced_diamond_drill");
    rarity(
        event,
        Rarity.EPIC,
        "ultimate_solar_panel",
        "quantum_solar_panel",
        "ultimate_solar_helmet",
        "quantum_solar_helmet",
        "quantum_generator",
        "advanced_lappack",
        "ultimate_lappack",
        "advanced_quant",
        "advanced_quantum_leggings",
        "advanced_quantum_boots",
        "vajra",
        "advanced_iridium_drill");
  }

  private static void rarity(ModifyDefaultComponentsEvent event, Rarity rarity, String... names) {
    for (String name : names) {
      var id = IC2RAddons.id(name);
      if (!BuiltInRegistries.ITEM.containsKey(id))
        throw new IllegalStateException("Missing item " + id);
      event.modify(
          BuiltInRegistries.ITEM.get(id), patch -> patch.set(DataComponents.RARITY, rarity));
    }
  }
}
