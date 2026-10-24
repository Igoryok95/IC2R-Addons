package com.igoryok95.ic2raddons.solar;

import com.igoryok95.ic2raddons.solar.item.ItemSolarHelmet;
import com.igoryok95.ic2raddons.solar.item.ItemSolarPanel;
import com.igoryok95.ic2raddons.solar.te.TileEntityAdvancedSolar;
import com.igoryok95.ic2raddons.solar.te.TileEntityHybridSolar;
import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import com.igoryok95.ic2raddons.solar.te.TileEntityQuantumGenerator;
import com.igoryok95.ic2raddons.solar.te.TileEntityQuantumSolar;
import com.igoryok95.ic2raddons.solar.te.TileEntityUltimateHybridSolar;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.Ic2TileEntityBlock.DefaultDrop;
import ic2.core.util.Util;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Forge registration for all Advanced Solar Panels content: the eight solar panels, the molecular
 * transformer and the quantum generator (each an {@link Ic2TileEntityBlock} with a matching {@link
 * BlockEntityType} and {@link BlockItem}), the crafting components, the four solar helmets, and a
 * creative tab.
 */
public final class AdvSolarBlocks {
  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, IC2RAddons.MODID);
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, IC2RAddons.MODID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IC2RAddons.MODID);
  public static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IC2RAddons.MODID);

  // --- Advanced Solar Panel ---
  public static final Supplier<Block> ADVANCED_SOLAR_PANEL =
      block("advanced_solar_panel", TileEntityAdvancedSolar.class, false);
  public static final Supplier<BlockEntityType<TileEntityAdvancedSolar>> BE_ADVANCED_SOLAR_PANEL =
      be("advanced_solar_panel", ADVANCED_SOLAR_PANEL, TileEntityAdvancedSolar::new);
  public static final Supplier<Item> ITEM_ADVANCED_SOLAR_PANEL =
      solarPanelItem("advanced_solar_panel", ADVANCED_SOLAR_PANEL, SolarPanelType.ADVANCED);

  // --- Hybrid Solar Panel ---
  public static final Supplier<Block> HYBRID_SOLAR_PANEL =
      block("hybrid_solar_panel", TileEntityHybridSolar.class, false);
  public static final Supplier<BlockEntityType<TileEntityHybridSolar>> BE_HYBRID_SOLAR_PANEL =
      be("hybrid_solar_panel", HYBRID_SOLAR_PANEL, TileEntityHybridSolar::new);
  public static final Supplier<Item> ITEM_HYBRID_SOLAR_PANEL =
      solarPanelItem("hybrid_solar_panel", HYBRID_SOLAR_PANEL, SolarPanelType.HYBRID);

  // --- Ultimate Hybrid Solar Panel ---
  public static final Supplier<Block> ULTIMATE_SOLAR_PANEL =
      block("ultimate_solar_panel", TileEntityUltimateHybridSolar.class, false);
  public static final Supplier<BlockEntityType<TileEntityUltimateHybridSolar>>
      BE_ULTIMATE_SOLAR_PANEL =
          be("ultimate_solar_panel", ULTIMATE_SOLAR_PANEL, TileEntityUltimateHybridSolar::new);
  public static final Supplier<Item> ITEM_ULTIMATE_SOLAR_PANEL =
      solarPanelItem("ultimate_solar_panel", ULTIMATE_SOLAR_PANEL, SolarPanelType.ULTIMATE);

  // --- Quantum Solar Panel ---
  public static final Supplier<Block> QUANTUM_SOLAR_PANEL =
      block("quantum_solar_panel", TileEntityQuantumSolar.class, false);
  public static final Supplier<BlockEntityType<TileEntityQuantumSolar>> BE_QUANTUM_SOLAR_PANEL =
      be("quantum_solar_panel", QUANTUM_SOLAR_PANEL, TileEntityQuantumSolar::new);
  public static final Supplier<Item> ITEM_QUANTUM_SOLAR_PANEL =
      solarPanelItem("quantum_solar_panel", QUANTUM_SOLAR_PANEL, SolarPanelType.QUANTUM);

  public static final Supplier<Block> SPECTRAL_SOLAR_PANEL =
      block(
          "spectral_solar_panel",
          com.igoryok95.ic2raddons.solar.te.TileEntitySpectralSolar.class,
          false);
  public static final Supplier<
          BlockEntityType<com.igoryok95.ic2raddons.solar.te.TileEntitySpectralSolar>>
      BE_SPECTRAL_SOLAR_PANEL =
          be(
              "spectral_solar_panel",
              SPECTRAL_SOLAR_PANEL,
              com.igoryok95.ic2raddons.solar.te.TileEntitySpectralSolar::new);
  public static final Supplier<Item> ITEM_SPECTRAL_SOLAR_PANEL =
      solarPanelItem("spectral_solar_panel", SPECTRAL_SOLAR_PANEL, SolarPanelType.SPECTRAL);
  public static final Supplier<Block> SINGULAR_SOLAR_PANEL =
      block(
          "singular_solar_panel",
          com.igoryok95.ic2raddons.solar.te.TileEntitySingularSolar.class,
          false);
  public static final Supplier<
          BlockEntityType<com.igoryok95.ic2raddons.solar.te.TileEntitySingularSolar>>
      BE_SINGULAR_SOLAR_PANEL =
          be(
              "singular_solar_panel",
              SINGULAR_SOLAR_PANEL,
              com.igoryok95.ic2raddons.solar.te.TileEntitySingularSolar::new);
  public static final Supplier<Item> ITEM_SINGULAR_SOLAR_PANEL =
      solarPanelItem("singular_solar_panel", SINGULAR_SOLAR_PANEL, SolarPanelType.SINGULAR);
  public static final Supplier<Block> LIGHT_ABSORBING_SOLAR_PANEL =
      block(
          "light_absorbing_solar_panel",
          com.igoryok95.ic2raddons.solar.te.TileEntityLightAbsorbingSolar.class,
          false);
  public static final Supplier<
          BlockEntityType<com.igoryok95.ic2raddons.solar.te.TileEntityLightAbsorbingSolar>>
      BE_LIGHT_ABSORBING_SOLAR_PANEL =
          be(
              "light_absorbing_solar_panel",
              LIGHT_ABSORBING_SOLAR_PANEL,
              com.igoryok95.ic2raddons.solar.te.TileEntityLightAbsorbingSolar::new);
  public static final Supplier<Item> ITEM_LIGHT_ABSORBING_SOLAR_PANEL =
      solarPanelItem(
          "light_absorbing_solar_panel",
          LIGHT_ABSORBING_SOLAR_PANEL,
          SolarPanelType.LIGHT_ABSORBING);
  public static final Supplier<Block> PHOTONIC_SOLAR_PANEL =
      block(
          "photonic_solar_panel",
          com.igoryok95.ic2raddons.solar.te.TileEntityPhotonicSolar.class,
          false);
  public static final Supplier<
          BlockEntityType<com.igoryok95.ic2raddons.solar.te.TileEntityPhotonicSolar>>
      BE_PHOTONIC_SOLAR_PANEL =
          be(
              "photonic_solar_panel",
              PHOTONIC_SOLAR_PANEL,
              com.igoryok95.ic2raddons.solar.te.TileEntityPhotonicSolar::new);
  public static final Supplier<Item> ITEM_PHOTONIC_SOLAR_PANEL =
      solarPanelItem("photonic_solar_panel", PHOTONIC_SOLAR_PANEL, SolarPanelType.PHOTONIC);

  // --- Molecular Transformer ---
  public static final Supplier<Block> MOLECULAR_TRANSFORMER =
      block("molecular_transformer", TileEntityMolecularTransformer.class, true);
  public static final Supplier<BlockEntityType<TileEntityMolecularTransformer>>
      BE_MOLECULAR_TRANSFORMER =
          be("molecular_transformer", MOLECULAR_TRANSFORMER, TileEntityMolecularTransformer::new);
  public static final Supplier<Item> ITEM_MOLECULAR_TRANSFORMER =
      item("molecular_transformer", MOLECULAR_TRANSFORMER);

  // --- Quantum Generator ---
  public static final Supplier<Block> QUANTUM_GENERATOR =
      block("quantum_generator", TileEntityQuantumGenerator.class, true);
  public static final Supplier<BlockEntityType<TileEntityQuantumGenerator>> BE_QUANTUM_GENERATOR =
      be("quantum_generator", QUANTUM_GENERATOR, TileEntityQuantumGenerator::new);
  public static final Supplier<Item> ITEM_QUANTUM_GENERATOR =
      item("quantum_generator", QUANTUM_GENERATOR);

  // --- Crafting components ---
  public static final String[] CRAFTING_ITEMS = {
    "sunnarium",
    "sunnarium_part",
    "sunnarium_alloy",
    "irradiant_uranium",
    "enriched_sunnarium",
    "enriched_sunnarium_alloy",
    "irradiant_glass_pane",
    "iridium_iron_plate",
    "reinforced_iridium_iron_plate",
    "irradiant_reinforced_plate",
    "iridium_ingot",
    "mt_core",
    "quantum_core",
  };
  public static final Map<String, Supplier<Item>> CRAFTING = new LinkedHashMap<>();

  static {
    for (String name : CRAFTING_ITEMS) {
      CRAFTING.put(name, ITEMS.register(name, () -> new Item(new Item.Properties())));
    }
  }

  // --- Solar helmets ---
  public static final Supplier<Item> ADVANCED_SOLAR_HELMET =
      ITEMS.register("advanced_solar_helmet", () -> new ItemSolarHelmet.Nano());
  public static final Supplier<Item> HYBRID_SOLAR_HELMET =
      ITEMS.register(
          "hybrid_solar_helmet", () -> new ItemSolarHelmet.Quantum(SolarPanelType.HYBRID));
  public static final Supplier<Item> ULTIMATE_SOLAR_HELMET =
      ITEMS.register(
          "ultimate_solar_helmet", () -> new ItemSolarHelmet.Quantum(SolarPanelType.ULTIMATE));

  public static final Supplier<Item> QUANTUM_SOLAR_HELMET =
      ITEMS.register(
          "quantum_solar_helmet", () -> new ItemSolarHelmet.Quantum(SolarPanelType.QUANTUM));

  // --- Creative tab ---
  public static final Supplier<CreativeModeTab> TAB =
      TABS.register(
          "main",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.ic2r_addons.technology"))
                  .icon(() -> new ItemStack(ADVANCED_SOLAR_PANEL.get()))
                  .displayItems(
                      (params, output) -> {
                        output.accept(ADVANCED_SOLAR_PANEL.get());
                        output.accept(HYBRID_SOLAR_PANEL.get());
                        output.accept(ULTIMATE_SOLAR_PANEL.get());
                        output.accept(QUANTUM_SOLAR_PANEL.get());
                        output.accept(SPECTRAL_SOLAR_PANEL.get());
                        output.accept(SINGULAR_SOLAR_PANEL.get());
                        output.accept(LIGHT_ABSORBING_SOLAR_PANEL.get());
                        output.accept(PHOTONIC_SOLAR_PANEL.get());
                        output.accept(MOLECULAR_TRANSFORMER.get());
                        output.accept(QUANTUM_GENERATOR.get());
                        for (Supplier<Item> machine :
                            com.igoryok95.ic2raddons.machines.AdvMachinesBlocks.ALL_ITEMS) {
                          output.accept(machine.get());
                        }
                        output.accept(ADVANCED_SOLAR_HELMET.get());
                        output.accept(HYBRID_SOLAR_HELMET.get());
                        output.accept(ULTIMATE_SOLAR_HELMET.get());
                        output.accept(QUANTUM_SOLAR_HELMET.get());
                        for (Supplier<Item> crafting : CRAFTING.values()) {
                          output.accept(crafting.get());
                        }
                      })
                  .build());

  private AdvSolarBlocks() {}

  private static BlockBehaviour.Properties machineProps() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_LIGHT_GRAY)
        .strength(3.0F, 15.0F)
        .requiresCorrectToolForDrops()
        .sound(SoundType.METAL);
  }

  private static Supplier<Block> block(
      String name, Class<? extends Ic2TileEntity> teClass, boolean canActive) {
    return BLOCKS.register(
        name,
        () ->
            Ic2TileEntityBlock.create(
                name.equals("molecular_transformer")
                    ? machineProps().noOcclusion()
                    : machineProps(),
                teClass,
                canActive,
                DefaultDrop.Self,
                Util.horizontalFacings,
                false));
  }

  private static <T extends Ic2TileEntity> Supplier<BlockEntityType<T>> be(
      String name, Supplier<Block> block, BlockEntitySupplier<T> factory) {
    return BLOCK_ENTITIES.register(
        name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
  }

  private static Supplier<Item> solarPanelItem(
      String name, Supplier<Block> block, SolarPanelType type) {
    return ITEMS.register(name, () -> new ItemSolarPanel(block.get(), type));
  }

  private static Supplier<Item> item(String name, Supplier<Block> block) {
    return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    BLOCK_ENTITIES.register(bus);
    TABS.register(bus);
  }
}
