package com.igoryok95.ic2raddons.equipment;

import com.igoryok95.ic2raddons.equipment.items.AdvancedChainsaw;
import com.igoryok95.ic2raddons.equipment.items.AdvancedNanoArmor;
import com.igoryok95.ic2raddons.equipment.items.AdvancedQuantumArmor;
import com.igoryok95.ic2raddons.equipment.items.GraviArmor;
import com.igoryok95.ic2raddons.equipment.items.GraviTool;
import com.igoryok95.ic2raddons.equipment.items.MiningTool;
import com.igoryok95.ic2raddons.equipment.items.Relocator;
import com.igoryok95.ic2raddons.equipment.items.UtilityItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GraviItems {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GraviSuite.ID);
  public static final DeferredItem<Item> SUPERCONDUCTOR_COVER = component("superconductor_cover");
  public static final DeferredItem<Item> SUPERCONDUCTOR = component("superconductor");
  public static final DeferredItem<Item> COOLING_CORE = component("cooling_core");
  public static final DeferredItem<Item> GRAVI_ENGINE = component("gravi_engine");
  public static final DeferredItem<Item> MAGNETRON = component("magnetron");
  public static final DeferredItem<Item> VAJRA_CORE = component("vajra_core");
  public static final DeferredItem<Item> ENGINE_BOOSTER = component("engine_booster");
  public static final DeferredItem<GraviArmor> ADVANCED_LAPPACK =
      ITEMS.register("advanced_lappack", () -> new GraviArmor(GraviArmor.Kind.ADVANCED_LAPPACK));
  public static final DeferredItem<GraviArmor> ULTIMATE_LAPPACK =
      ITEMS.register("ultimate_lappack", () -> new GraviArmor(GraviArmor.Kind.ULTIMATE_LAPPACK));
  public static final DeferredItem<GraviArmor> ADVANCED_JETPACK =
      ITEMS.register("advanced_jetpack", () -> new GraviArmor(GraviArmor.Kind.JETPACK));
  public static final DeferredItem<GraviArmor> ADVANCED_NANO =
      ITEMS.register("advanced_nano", () -> new GraviArmor(GraviArmor.Kind.NANO));
  public static final DeferredItem<GraviArmor> GRAVI_CHESTPLATE =
      ITEMS.register("advanced_quant", () -> new GraviArmor(GraviArmor.Kind.GRAVI));
  public static final DeferredItem<AdvancedNanoArmor> ADVANCED_NANO_LEGGINGS =
      ITEMS.register("advanced_nano_leggings", () -> new AdvancedNanoArmor(EquipmentSlot.LEGS));
  public static final DeferredItem<AdvancedNanoArmor> ADVANCED_NANO_BOOTS =
      ITEMS.register("advanced_nano_boots", () -> new AdvancedNanoArmor(EquipmentSlot.FEET));
  public static final DeferredItem<AdvancedQuantumArmor> ADVANCED_QUANTUM_LEGGINGS =
      ITEMS.register(
          "advanced_quantum_leggings", () -> new AdvancedQuantumArmor(EquipmentSlot.LEGS));
  public static final DeferredItem<AdvancedQuantumArmor> ADVANCED_QUANTUM_BOOTS =
      ITEMS.register("advanced_quantum_boots", () -> new AdvancedQuantumArmor(EquipmentSlot.FEET));
  public static final DeferredItem<MiningTool> ADVANCED_DRILL =
      ITEMS.register("advanced_diamond_drill", () -> new MiningTool(false));
  public static final DeferredItem<MiningTool> ADVANCED_IRIDIUM_DRILL =
      ITEMS.register("advanced_iridium_drill", () -> new MiningTool(false, true));
  public static final DeferredItem<MiningTool> VAJRA =
      ITEMS.register("vajra", () -> new MiningTool(true));
  public static final DeferredItem<AdvancedChainsaw> ADVANCED_CHAINSAW =
      ITEMS.register("advanced_chainsaw", AdvancedChainsaw::new);
  public static final DeferredItem<GraviTool> GRAVITOOL =
      ITEMS.register("gravitool", GraviTool::new);
  public static final DeferredItem<UtilityItem> MAGNET =
      ITEMS.register("magnet", () -> new UtilityItem(false));
  public static final DeferredItem<UtilityItem> VOIDER =
      ITEMS.register("voider", () -> new UtilityItem(true));
  public static final DeferredItem<Relocator> RELOCATOR =
      ITEMS.register("relocator", Relocator::new);

  private static DeferredItem<Item> component(String name) {
    return ITEMS.registerSimpleItem(name);
  }

  private GraviItems() {}
}
