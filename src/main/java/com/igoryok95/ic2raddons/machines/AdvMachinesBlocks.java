package com.igoryok95.ic2raddons.machines;

import com.igoryok95.ic2raddons.machines.te.TileEntityCentrifugeExtractor;
import com.igoryok95.ic2raddons.machines.te.TileEntityCompactingRecycler;
import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import com.igoryok95.ic2raddons.machines.te.TileEntityImpellerizedRoller;
import com.igoryok95.ic2raddons.machines.te.TileEntityLiquescentExtruder;
import com.igoryok95.ic2raddons.machines.te.TileEntityRotaryMacerator;
import com.igoryok95.ic2raddons.machines.te.TileEntitySingularityCompressor;
import com.igoryok95.ic2raddons.machines.te.TileEntityThermalWasher;
import com.igoryok95.ic2raddons.machines.te.TileEntityWaterJetCutter;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.Ic2TileEntityBlock.DefaultDrop;
import ic2.core.util.Util;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Forge registration for all Advanced Machines content: one {@link Ic2TileEntityBlock} per machine
 * (mirroring IC2's own processing machines), a matching {@link BlockEntityType}, a {@link
 * BlockItem}, plus the Sharp Plate item and a creative tab.
 */
public final class AdvMachinesBlocks {
  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, IC2RAddons.MODID);
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, IC2RAddons.MODID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IC2RAddons.MODID);

  // --- Rotary Macerator ---
  public static final Supplier<Block> ROTARY_MACERATOR =
      block("rotary_macerator", TileEntityRotaryMacerator.class);
  public static final Supplier<BlockEntityType<TileEntityRotaryMacerator>> BE_ROTARY_MACERATOR =
      be("rotary_macerator", ROTARY_MACERATOR, TileEntityRotaryMacerator::new);
  public static final Supplier<Item> ITEM_ROTARY_MACERATOR =
      item("rotary_macerator", ROTARY_MACERATOR);

  // --- Singularity Compressor ---
  public static final Supplier<Block> SINGULARITY_COMPRESSOR =
      block("singularity_compressor", TileEntitySingularityCompressor.class);
  public static final Supplier<BlockEntityType<TileEntitySingularityCompressor>>
      BE_SINGULARITY_COMPRESSOR =
          be(
              "singularity_compressor",
              SINGULARITY_COMPRESSOR,
              TileEntitySingularityCompressor::new);
  public static final Supplier<Item> ITEM_SINGULARITY_COMPRESSOR =
      item("singularity_compressor", SINGULARITY_COMPRESSOR);

  // --- Centrifuge Extractor ---
  public static final Supplier<Block> CENTRIFUGE_EXTRACTOR =
      block("centrifuge_extractor", TileEntityCentrifugeExtractor.class);
  public static final Supplier<BlockEntityType<TileEntityCentrifugeExtractor>>
      BE_CENTRIFUGE_EXTRACTOR =
          be("centrifuge_extractor", CENTRIFUGE_EXTRACTOR, TileEntityCentrifugeExtractor::new);
  public static final Supplier<Item> ITEM_CENTRIFUGE_EXTRACTOR =
      item("centrifuge_extractor", CENTRIFUGE_EXTRACTOR);

  // --- Compacting Recycler ---
  public static final Supplier<Block> COMPACTING_RECYCLER =
      block("compacting_recycler", TileEntityCompactingRecycler.class);
  public static final Supplier<BlockEntityType<TileEntityCompactingRecycler>>
      BE_COMPACTING_RECYCLER =
          be("compacting_recycler", COMPACTING_RECYCLER, TileEntityCompactingRecycler::new);
  public static final Supplier<Item> ITEM_COMPACTING_RECYCLER =
      item("compacting_recycler", COMPACTING_RECYCLER);

  // --- Liquescent Extruder ---
  public static final Supplier<Block> LIQUESCENT_EXTRUDER =
      block("liquescent_extruder", TileEntityLiquescentExtruder.class);
  public static final Supplier<BlockEntityType<TileEntityLiquescentExtruder>>
      BE_LIQUESCENT_EXTRUDER =
          be("liquescent_extruder", LIQUESCENT_EXTRUDER, TileEntityLiquescentExtruder::new);
  public static final Supplier<Item> ITEM_LIQUESCENT_EXTRUDER =
      item("liquescent_extruder", LIQUESCENT_EXTRUDER);

  // --- Impellerized Roller ---
  public static final Supplier<Block> IMPELLERIZED_ROLLER =
      block("impellerized_roller", TileEntityImpellerizedRoller.class);
  public static final Supplier<BlockEntityType<TileEntityImpellerizedRoller>>
      BE_IMPELLERIZED_ROLLER =
          be("impellerized_roller", IMPELLERIZED_ROLLER, TileEntityImpellerizedRoller::new);
  public static final Supplier<Item> ITEM_IMPELLERIZED_ROLLER =
      item("impellerized_roller", IMPELLERIZED_ROLLER);

  // --- Water Jet Cutter ---
  public static final Supplier<Block> WATER_JET_CUTTER =
      block("water_jet_cutter", TileEntityWaterJetCutter.class);
  public static final Supplier<BlockEntityType<TileEntityWaterJetCutter>> BE_WATER_JET_CUTTER =
      be("water_jet_cutter", WATER_JET_CUTTER, TileEntityWaterJetCutter::new);
  public static final Supplier<Item> ITEM_WATER_JET_CUTTER =
      item("water_jet_cutter", WATER_JET_CUTTER);

  // --- Thermal Washer ---
  public static final Supplier<Block> THERMAL_WASHER =
      block("thermal_washer", TileEntityThermalWasher.class);
  public static final Supplier<BlockEntityType<TileEntityThermalWasher>> BE_THERMAL_WASHER =
      be("thermal_washer", THERMAL_WASHER, TileEntityThermalWasher::new);
  public static final Supplier<Item> ITEM_THERMAL_WASHER = item("thermal_washer", THERMAL_WASHER);

  // --- Sharp Plate ---
  public static final Supplier<Item> SHARP_PLATE =
      ITEMS.register("sharp_plate", () -> new Item(new Item.Properties()));

  public static final java.util.List<Supplier<Item>> ALL_ITEMS =
      java.util.List.of(
          ITEM_ROTARY_MACERATOR,
          ITEM_SINGULARITY_COMPRESSOR,
          ITEM_CENTRIFUGE_EXTRACTOR,
          ITEM_COMPACTING_RECYCLER,
          ITEM_LIQUESCENT_EXTRUDER,
          ITEM_IMPELLERIZED_ROLLER,
          ITEM_WATER_JET_CUTTER,
          ITEM_THERMAL_WASHER,
          SHARP_PLATE);

  private AdvMachinesBlocks() {}

  private static BlockBehaviour.Properties machineProps() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_LIGHT_GRAY)
        .strength(5.0F, 10.0F)
        .requiresCorrectToolForDrops()
        .sound(SoundType.METAL);
  }

  private static Supplier<Block> block(String name, Class<? extends Ic2TileEntity> teClass) {
    return BLOCKS.register(
        name,
        () ->
            Ic2TileEntityBlock.create(
                machineProps(),
                teClass,
                true,
                DefaultDrop.AdvMachine,
                Util.horizontalFacings,
                true));
  }

  private static <T extends Ic2TileEntity> Supplier<BlockEntityType<T>> be(
      String name, Supplier<Block> block, BlockEntitySupplier<T> factory) {
    return BLOCK_ENTITIES.register(
        name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
  }

  private static Supplier<Item> item(String name, Supplier<Block> block) {
    return ITEMS.register(
        name,
        () ->
            new BlockItem(block.get(), new Item.Properties()) {
              @Override
              public void appendHoverText(
                  net.minecraft.world.item.ItemStack stack,
                  Item.TooltipContext context,
                  java.util.List<Component> tip,
                  net.minecraft.world.item.TooltipFlag flag) {
                super.appendHoverText(stack, context, tip, flag);
                int active =
                    name.equals("thermal_washer")
                        ? 48
                        : java.util.Set.of(
                                    "water_jet_cutter",
                                    "liquescent_extruder",
                                    "impellerized_roller")
                                .contains(name)
                            ? 24
                            : TileEntityHeatingMachine.DEFAULT_ACTIVE_EU;
                tip.add(Component.translatable("tooltip.ic2r_addons.machines.power", active));
                tip.add(
                    Component.translatable(
                        "tooltip.ic2r_addons.machines.warmup",
                        name.equals("thermal_washer") ? 6 : 1));
                if (name.equals("water_jet_cutter") || name.equals("thermal_washer"))
                  tip.add(Component.translatable("tooltip.ic2r_addons.machines.water"));
                if (name.equals("thermal_washer"))
                  tip.add(
                      Component.translatable("tooltip.ic2r_addons.machines.thermal_shock")
                          .withStyle(net.minecraft.ChatFormatting.RED));
              }
            });
  }

  public static void register(net.neoforged.bus.api.IEventBus bus) {
    bus.addListener(MachinesCapabilities::register);
    BLOCKS.register(bus);
    ITEMS.register(bus);
    BLOCK_ENTITIES.register(bus);
  }
}
