// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.api.*;
import com.igoryok95.ic2raddons.control.crossmod.*;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.items.cards.*;
import com.igoryok95.ic2raddons.control.tileentities.*;
import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import ic2.core.block.comp.Energy;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class ControlGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY, timeoutTicks = 240)
  public static void assemblerPausesAndResumesWithoutLosingWork(GameTestHelper h) {
    var pos = new BlockPos(1, 1, 1);
    h.setBlock(pos, ModItems.kit_assembler.get());
    TileEntityKitAssembler assembler = h.getBlockEntity(pos);
    assembler.setItem(1, new ItemStack(ModItems.card_energy.get()));
    assembler.setItem(2, new ItemStack(ModItems.strong_string.get(), 2));
    assembler.setItem(3, new ItemStack(ModItems.card_energy.get()));
    assembler.setItem(6, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE, 3));
    var initial = assembler.saveWithFullMetadata(h.getLevel().registryAccess());
    initial.putInt("energy", 5000);
    assembler.loadWithComponents(initial, h.getLevel().registryAccess());
    assembler.setChanged();
    h.runAfterDelay(
        5,
        () -> {
          h.assertTrue(
              assembler.getProduction() > 0, "Assembler actually processes a registered recipe");
          double progress = assembler.getProduction();
          var handler = assembler.getEnergyHandler(null);
          for (int i = 0; i < 200; i++) handler.extractEnergy(Integer.MAX_VALUE, false);
          h.runAfterDelay(
              5,
              () -> {
                h.assertTrue(
                    assembler.getProduction() == progress, "Power loss preserves partial work");
                var saved = assembler.saveWithFullMetadata(h.getLevel().registryAccess());
                assembler.loadWithComponents(saved, h.getLevel().registryAccess());
                h.assertTrue(
                    assembler.getProduction() == progress, "Saved partial work survives reload");
                for (int i = 0; i < 200; i++) handler.receiveEnergy(5000, false);
                assembler.setChanged();
                h.succeedWhen(
                    () -> {
                      h.assertTrue(
                          assembler.getItem(4).is(ModItems.kit_energy.get())
                              && assembler.getItem(4).getCount() == 2,
                          "Exact recipe output");
                      h.assertTrue(
                          assembler.getItem(1).isEmpty()
                              && assembler.getItem(2).isEmpty()
                              && assembler.getItem(3).isEmpty(),
                          "Inputs consumed once after successful work");
                    });
              });
        });
  }

  @GameTest(template = EMPTY)
  public static void inventoryTelemetryCountsSlotsBeyondTheSixDisplayedEntries(GameTestHelper h) {
    var pos = new BlockPos(1, 1, 1);
    h.setBlock(pos, ModItems.kit_assembler.get());
    TileEntityKitAssembler assembler = h.getBlockEntity(pos);
    assembler.setItem(2, new ItemStack(ModItems.strong_string.get(), 2));
    assembler.setItem(6, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE, 3));
    var data = CrossModLoader.getInventoryData(assembler);
    h.assertTrue(
        data != null
            && data.getInt("size") == 7
            && data.getInt("used") == 2
            && data.getInt("items") == 5,
        "All slots contribute to inventory totals");
    h.assertTrue(!data.contains("slot6"), "Only the six detailed display rows are serialized");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void controlRecipesAndNativeComponentsExist(GameTestHelper h) {
    for (var name :
        java.util.List.of(
            "energy_counter",
            "average_counter",
            "kit_ic2",
            "kit_counter",
            "thermometer",
            "thermometer_digital",
            "info_panel",
            "info_panel_advanced",
            "holo_panel",
            "howler_alarm",
            "thermal_monitor"))
      h.assertTrue(
          h.getLevel()
              .getRecipeManager()
              .byKey(ResourceLocation.fromNamespaceAndPath("ic2r_addons", name))
              .isPresent(),
          "Craftable control item: " + name);
    h.assertTrue(
        ModItems.basic_circuit.get() == Ic2Items.CIRCUIT
            && ModItems.advanced_circuit.get() == Ic2Items.ADVANCED_CIRCUIT
            && ModItems.machine_casing.get() == Ic2Items.MACHINE,
        "Control reuses existing IC2R components");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void reactorTelemetryUsesNativeHeatAndFuelWear(GameTestHelper h) {
    var pos = new BlockPos(1, 1, 1);
    h.setBlock(pos, Ic2Blocks.NUCLEAR_REACTOR);
    TileEntityNuclearReactorElectric reactor = h.getBlockEntity(pos);
    reactor.setHeat(1234);
    var rod = new ItemStack(Ic2Items.URANIUM_FUEL_ROD);
    reactor.setItemAt(0, 0, rod);
    var data = new CrossIC2R().getCardData(reactor);
    h.assertTrue(
        data.getInt("heat") == 1234 && data.getInt("maxHeat") > 1234,
        "Real reactor temperature and limit");
    h.assertTrue(
        data.getInt("fuel") == 1 && data.getInt("timeLeft") == 20000,
        "Native IC2R fuel-use data, not vanilla damage");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void everyAddonMachineExposesRealEu(GameTestHelper h) {
    var bridge = new CrossIC2R();
    var pos = new BlockPos(1, 1, 1);
    var blocks = new java.util.ArrayList<net.minecraft.world.level.block.Block>();
    AdvMachinesBlocks.BLOCKS.getEntries().forEach(holder -> blocks.add(holder.get()));
    blocks.add(AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    blocks.add(AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    for (var block : blocks) {
      h.setBlock(pos, block);
      Ic2TileEntity machine = h.getBlockEntity(pos);
      machine.getComponent(Energy.class).forceAddEnergy(321);
      var data = bridge.getEnergyData(machine);
      h.assertTrue(
          data != null && data.getDouble("energy") == 321 && data.getString("euType").equals("EU"),
          "Native EU for " + block);
    }
    h.succeed();
  }

  @GameTest(template = EMPTY, timeoutTicks = 80)
  public static void localMonitorSignalsOverheating(GameTestHelper h) {
    h.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.NUCLEAR_REACTOR);
    TileEntityNuclearReactorElectric reactor = h.getBlockEntity(new BlockPos(0, 1, 1));
    h.setBlock(new BlockPos(1, 1, 1), ModItems.thermal_monitor.get());
    TileEntityThermalMonitor monitor = h.getBlockEntity(new BlockPos(1, 1, 1));
    reactor.setHeat(1000);
    monitor.setHeatLevel(500);
    h.succeedWhen(
        () -> h.assertTrue(monitor.getPowered(), "Threshold produces real redstone power"));
  }

  @GameTest(template = EMPTY, timeoutTicks = 80)
  public static void remoteMonitorAndCardUseBoundCoordinates(GameTestHelper h) {
    var reactorPos = new BlockPos(0, 1, 1);
    h.setBlock(reactorPos, Ic2Blocks.NUCLEAR_REACTOR);
    TileEntityNuclearReactorElectric reactor = h.getBlockEntity(reactorPos);
    reactor.setHeat(750);
    h.setBlock(new BlockPos(2, 1, 1), ModItems.remote_thermo.get());
    TileEntityRemoteThermalMonitor monitor = h.getBlockEntity(new BlockPos(2, 1, 1));
    var card = new ItemStack(ModItems.card_ic2);
    ItemStackHelper.setCoordinates(card, h.absolutePos(reactorPos));
    monitor.setItem(0, card);
    var reader = new ItemCardReader(card);
    h.assertTrue(
        ((ItemCardIC2) card.getItem()).update(h.getLevel(), reader, 8, monitor.getBlockPos())
                == CardState.OK
            && reader.getInt("heat") == 750,
        "Reactor card updates on the server");
    h.succeedWhen(
        () ->
            h.assertTrue(
                monitor.getHeat() == 750 && monitor.getPowered(),
                "Remote monitor receives heat and switches"));
  }

  @GameTest(template = EMPTY)
  public static void monitorSettingsAndInventorySurviveSaving(GameTestHelper h) {
    var pos = new BlockPos(1, 1, 1);
    h.setBlock(pos, ModItems.remote_thermo.get());
    TileEntityRemoteThermalMonitor monitor = h.getBlockEntity(pos);
    monitor.setHeatLevel(4321);
    monitor.setInvertRedstone(true);
    monitor.setItem(0, new ItemStack(ModItems.card_ic2));
    var saved = monitor.saveWithFullMetadata(h.getLevel().registryAccess());
    var restored =
        new TileEntityRemoteThermalMonitor(monitor.getBlockPos(), monitor.getBlockState());
    restored.loadWithComponents(saved, h.getLevel().registryAccess());
    h.assertTrue(
        restored.getHeatLevel() == 4321
            && restored.getInvertRedstone()
            && restored.getItem(0).is(ModItems.card_ic2),
        "Persistent settings and actual card stack");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void countersRetainUpgradesAndSavedTotals(GameTestHelper h) {
    var pos = new BlockPos(1, 1, 1);
    h.setBlock(pos, ModItems.energy_counter.get());
    TileEntityEnergyCounter counter = h.getBlockEntity(pos);
    h.assertTrue(
        counter.getBlockState().getRenderShape()
            == net.minecraft.world.level.block.RenderShape.MODEL,
        "Counter body is visibly rendered, not inherited INVISIBLE");
    counter.setItem(0, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE, 2));
    var saved = counter.saveWithFullMetadata(h.getLevel().registryAccess());
    saved.putDouble("total", 12345.5);
    saved.putLong("samples", 100);
    counter.loadWithComponents(saved, h.getLevel().registryAccess());
    h.assertTrue(
        counter.maximumPacket() == 512
            && counter.total() == 12345.5
            && counter.average() == 123.455,
        "Counter persists fractional EU, sampling time and upgrades");
    var data = new CrossIC2R().getCardData(counter);
    h.assertTrue(
        data.getDouble("total") == counter.total(), "Counter cards receive actual readings");
    counter.reset();
    h.assertTrue(
        counter.total() == 0 && counter.average() == 0,
        "Explicit reset clears totals and averaging period");
    h.succeed();
  }

  @GameTest(template = EMPTY, timeoutTicks = 140)
  public static void countersCarryAndMeasureNativeEu(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
    var source =
        (ic2.core.block.wiring.tileentity.TileEntityElectricBlock)
            h.getBlockEntity(new BlockPos(1, 2, 1));
    source.energy.addEnergy(10000);
    h.setBlock(
        new BlockPos(1, 1, 1),
        ModItems.energy_counter
            .get()
            .defaultBlockState()
            .setValue(
                com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock.FACING, Direction.DOWN));
    TileEntityEnergyCounter counter = h.getBlockEntity(new BlockPos(1, 1, 1));
    h.setBlock(new BlockPos(1, 0, 1), AdvMachinesBlocks.ROTARY_MACERATOR.get());
    var sink = (Ic2TileEntity) h.getBlockEntity(new BlockPos(1, 0, 1));
    h.succeedWhen(
        () -> {
          double stored = sink.getComponent(Energy.class).getEnergy();
          h.assertTrue(
              stored > 0 && counter.total() > 0 && counter.average() > 0,
              "EU crosses the conductor and is measured");
          h.assertTrue(
              stored <= 10000 - source.energy.getEnergy() + 0.001, "Counter never creates energy");
        });
  }
}
