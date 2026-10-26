package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.bridge.TestPlayers;
import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import ic2.api.recipe.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class AdvancedMachinesGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  private static TileEntityHeatingMachine machine(GameTestHelper h, Block block) {
    h.setBlock(POS, block);
    return h.getBlockEntity(POS);
  }

  private static void heat(TileEntityHeatingMachine machine, int heat) {
    var tag = machine.saveWithFullMetadata(machine.getLevel().registryAccess());
    tag.putShort("heat", (short) heat);
    machine.loadWithComponents(tag, machine.getLevel().registryAccess());
  }

  private static void fill(TileEntityHeatingMachine machine) {
    if (machine instanceof TileEntityHeatingWaterMachine water) {
      water.tank.fillMb(Ic2FluidStack.create(Fluids.WATER, 8000), false);
      water.waterTickFinished();
    }
  }

  @GameTest(template = EMPTY)
  public static void everyAdvancedMachineUsesRealTransferSlots(GameTestHelper h) {
    var player = TestPlayers.create(h);
    for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
      var tile = machine(h, entry.get());
      var menu =
          (ic2.core.gui.dynamic.DynamicContainer<?>) tile.createServerScreenHandler(1, player);
      var inputs = com.igoryok95.ic2raddons.machines.compat.AddonMachineSlots.inputs(menu);
      var inventory = com.igoryok95.ic2raddons.machines.compat.AddonMachineSlots.inventory(menu);
      h.assertTrue(
          inputs.size() == 1
              && ((ic2.core.slot.SlotInvSlot) inputs.getFirst()).invSlot == tile.inputSlot,
          "all eight menus target only their actual processable input");
      h.assertTrue(
          inventory.size() == 36 && inventory.stream().noneMatch(inputs::contains),
          "all player slots are resolved without reaching past shorter addon menus");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void everyMachineConvertsRawWarmupToPercentages(GameTestHelper h) {
    for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
      var machine = machine(h, entry.get());
      heat(machine, 0);
      h.assertTrue(machine.getWarmupPercent() == 0, "cold machine displays zero percent");
      heat(machine, 3750);
      h.assertTrue(
          machine.getWarmupPercent() == 37.5 && machine.getRawHeat() == 3750,
          "display conversion preserves actual processing state");
      heat(machine, 10000);
      h.assertTrue(machine.getWarmupPercent() == 100, "full speed or heat displays 100 percent");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void manualWarmupIsLocalPersistentAndUsesOriginalSupplies(GameTestHelper h) {
    for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
      var machine = machine(h, entry.get());
      fill(machine);
      machine.setManualWarmup(true);
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == 0, "no EU means no idle warmup");
      machine.getComponent(Energy.class).addEnergy(100);
      machine.updateEntityServer();
      h.assertTrue(
          machine.getRawHeat() == 1
              && machine.getComponent(Energy.class).getEnergy() == 100 - machine.idleEU,
          "all eight machines keep their original standby EU demand");
      var saved = machine.saveWithFullMetadata(h.getLevel().registryAccess());
      var loaded =
          (TileEntityHeatingMachine)
              BlockEntity.loadStatic(
                  machine.getBlockPos(),
                  machine.getBlockState(),
                  saved,
                  h.getLevel().registryAccess());
      h.assertTrue(loaded != null && loaded.isManualWarmup(), "standby survives saving");
      for (var side : Direction.values())
        h.assertTrue(
            machine.getBlockState().getSignal(h.getLevel(), machine.getBlockPos(), side) == 0
                && machine
                        .getBlockState()
                        .getDirectSignal(h.getLevel(), machine.getBlockPos(), side)
                    == 0,
            "standby does not emit redstone to neighboring blocks");
      var player = TestPlayers.create(h);
      machine.onNetworkEvent(player, ic2.core.block.machine.IWarmupControl.TOGGLE_WARMUP_EVENT);
      h.assertTrue(machine.isManualWarmup(), "unopened menu cannot toggle machine");
      player.containerMenu = machine.createServerScreenHandler(1, player);
      machine.onNetworkEvent(player, ic2.core.block.machine.IWarmupControl.TOGGLE_WARMUP_EVENT);
      h.assertTrue(!machine.isManualWarmup(), "nearby open menu toggles off");
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == 0, "idle machine cools when switched off");
      h.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == 1, "external redstone still works independently");
      h.setBlock(POS.east(), Blocks.AIR);
      if (machine instanceof TileEntityHeatingWaterMachine water) {
        water.tank.drainMbUnchecked(8000, false);
        heat(machine, 10000);
        machine.setManualWarmup(true);
        machine.updateEntityServer();
        h.assertTrue(
            machine.getRawHeat() < 10000, "hot water machines still require standby water");
      }
    }
    h.succeed();
  }

  private static MachineRecipe<IRecipeInput, Collection<ItemStack>> first(
      GameTestHelper h,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    for (var recipe : manager.get(h.getLevel()).getRecipes())
      if (!recipe.getOutput().isEmpty() && !recipe.getInput().getInputs().isEmpty()) return recipe;
    throw new IllegalStateException("IC2R recipe manager has no usable recipe");
  }

  private static void assertSoundState(
      GameTestHelper h, TileEntityHeatingMachine machine, boolean processing, String reason) {
    h.assertTrue(
        machine.getActive() == processing
            && machine.getBlockState().getValue(ic2.core.block.tileentity.Ic2TileEntityBlock.ACTIVE)
                == processing,
        reason);
  }

  @GameTest(template = EMPTY)
  public static void operatingSoundsFollowProcessingInsteadOfStoredHeat(GameTestHelper h) {
    var blocks =
        List.of(
            AdvMachinesBlocks.ROTARY_MACERATOR.get(),
            AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(),
            AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(),
            AdvMachinesBlocks.COMPACTING_RECYCLER.get(),
            AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(),
            AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(),
            AdvMachinesBlocks.WATER_JET_CUTTER.get(),
            AdvMachinesBlocks.THERMAL_WASHER.get());
    int[] processingEu = {16, 16, 16, 16, 24, 24, 24, 48};
    for (int index = 0; index < blocks.size(); index++) {
      var machine = machine(h, blocks.get(index));
      h.assertTrue(
          machine.getActiveEU() == processingEu[index] && machine.idleEU == (index == 7 ? 6 : 1),
          "basic machines match induction total EU/t; specialized and standby ratings are preserved");
      fill(machine);
      heat(machine, 3750);
      machine.getComponent(Energy.class).addEnergy(10000);
      machine.setManualWarmup(true);
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == 3751, "manual standby still warms the machine");
      assertSoundState(h, machine, false, "manual standby is silent");

      machine.setManualWarmup(false);
      h.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
      machine.redstone.update();
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == 3752, "external standby still warms the machine");
      assertSoundState(h, machine, false, "external redstone standby is silent");
      h.setBlock(POS.east(), Blocks.AIR);
      machine.redstone.update();

      if (index == 3) {
        machine.inputSlot.put(new ItemStack(Ic2Items.SCRAP, 9));
      } else {
        var recipe =
            switch (index) {
              case 0 -> first(h, Recipes.macerator);
              case 1 -> first(h, Recipes.compressor);
              case 2 -> first(h, Recipes.extractor);
              case 4 -> first(h, Recipes.metalformerExtruding);
              case 5 -> first(h, Recipes.metalformerRolling);
              case 6 -> first(h, Recipes.metalformerCutting);
              case 7 -> first(h, Recipes.oreWashing);
              default -> throw new IllegalArgumentException("Unexpected test machine");
            };
        machine.inputSlot.put(
            recipe.getInput().getInputs().getFirst().copyWithCount(recipe.getInput().getAmount()));
      }
      double before = machine.getEnergy();
      machine.updateEntityServer();
      h.assertTrue(
          machine.progress > 0 && machine.getEnergy() == before - processingEu[index],
          "actual recipe progress still consumes the rated processing EU");
      assertSoundState(h, machine, true, "processing starts the native IC2R sound lifecycle");

      for (int slot = 0; slot < machine.outputSlot.size(); slot++)
        machine.outputSlot.put(slot, new ItemStack(Items.BEDROCK, 64));
      machine.setManualWarmup(true);
      machine.updateEntityServer();
      assertSoundState(h, machine, false, "blocked output stops sound while standby stays hot");
      for (int slot = 0; slot < machine.outputSlot.size(); slot++)
        machine.outputSlot.put(slot, ItemStack.EMPTY);

      machine.updateEntityServer();
      assertSoundState(h, machine, true, "unblocking output resumes processing sound");
      machine.getComponent(Energy.class).useEnergy(machine.getEnergy());
      machine.updateEntityServer();
      assertSoundState(h, machine, false, "power loss stops sound before the machine cools down");
      machine.getComponent(Energy.class).addEnergy(10000);
      machine.updateEntityServer();
      assertSoundState(h, machine, true, "restoring power resumes processing sound");
      machine.inputSlot.put(ItemStack.EMPTY);
      machine.updateEntityServer();
      assertSoundState(h, machine, false, "removing input stops sound despite manual warmup");
      machine.setManualWarmup(false);
      int hot = machine.getRawHeat();
      machine.updateEntityServer();
      h.assertTrue(machine.getRawHeat() == hot - 2, "normal cooling is unchanged");
      assertSoundState(h, machine, false, "residual heat does not restart operating sound");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void inductionEnergyAudit(GameTestHelper h) throws ReflectiveOperationException {
    h.setBlock(POS, Ic2Blocks.INDUCTION_FURNACE);
    ic2.core.block.machine.tileentity.TileEntityInduction furnace = h.getBlockEntity(POS);
    var tick = furnace.getClass().getDeclaredMethod("updateEntityServer");
    tick.setAccessible(true);
    furnace.heat = 10000;
    furnace.setManualWarmup(true);
    var energy = furnace.getComponent(Energy.class);
    energy.addEnergy(10000);
    tick.invoke(furnace);
    h.assertTrue(energy.getEnergy() == 9999, "induction standby costs 1 EU/t");
    h.assertTrue(!furnace.getActive(), "native induction standby is silent");

    furnace.inputSlotA.put(new ItemStack(Ic2Items.IRON_DUST));
    furnace.inputSlotB.put(new ItemStack(Ic2Items.IRON_DUST));
    double before = energy.getEnergy();
    for (int i = 0; i < 13; i++) tick.invoke(furnace);
    h.assertTrue(
        energy.getEnergy() == before - 13 * 16,
        "induction consumes 1 warmup plus 15 processing EU per tick for both inputs together");
    h.assertTrue(furnace.getActive(), "induction processing enables operating sound");
    tick.invoke(furnace);
    h.assertTrue(
        furnace.outputSlotA.get().is(Items.IRON_INGOT)
            && furnace.outputSlotB.get().is(Items.IRON_INGOT),
        "full-heat induction completes two smelts after 13 paid processing ticks");
    h.assertTrue(!furnace.getActive(), "induction becomes silent after finishing its last inputs");
    h.succeed();
  }

  private static void process(
      GameTestHelper h,
      Block block,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    var machine = machine(h, block);
    var recipe = first(h, manager);
    var input =
        recipe.getInput().getInputs().getFirst().copyWithCount(recipe.getInput().getAmount());
    machine.inputSlot.put(input);
    fill(machine);
    heat(machine, 10000);
    machine.getComponent(Energy.class).addEnergy(10000);
    int waterBefore =
        machine instanceof TileEntityHeatingWaterMachine water ? water.getWaterAmount() : 0;
    int ticks = machine.getMaxProgress() / 10000;
    for (int i = 0; i < ticks; i++) machine.updateEntityServer();
    h.assertTrue(machine.inputSlot.isEmpty(), "recipe consumed its complete input");
    for (ItemStack expected : recipe.getOutput()) {
      int actual = 0;
      for (int i = 0; i < machine.outputSlot.size(); i++) {
        var stack = machine.outputSlot.get(i);
        if (ItemStack.isSameItemSameComponents(stack, expected)) actual += stack.getCount();
      }
      h.assertTrue(actual == expected.getCount(), "exact IC2R recipe output: " + expected);
    }
    h.assertTrue(
        machine.getEnergy() == 10000 - ticks * machine.getActiveEU(),
        "rated EU/t is consumed exactly");
    if (machine instanceof TileEntityHeatingWaterMachine water)
      h.assertTrue(
          water.getWaterAmount() < waterBefore,
          "water is actually consumed from an insert-only tank");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void rotaryMacerator(GameTestHelper h) {
    process(h, AdvMachinesBlocks.ROTARY_MACERATOR.get(), Recipes.macerator);
  }

  @GameTest(template = EMPTY)
  public static void singularityCompressor(GameTestHelper h) {
    process(h, AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(), Recipes.compressor);
  }

  @GameTest(template = EMPTY)
  public static void centrifugeExtractor(GameTestHelper h) {
    process(h, AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(), Recipes.extractor);
  }

  @GameTest(template = EMPTY)
  public static void liquescentExtruder(GameTestHelper h) {
    process(h, AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(), Recipes.metalformerExtruding);
  }

  @GameTest(template = EMPTY)
  public static void impellerizedRoller(GameTestHelper h) {
    process(h, AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(), Recipes.metalformerRolling);
  }

  @GameTest(template = EMPTY)
  public static void waterJetCutter(GameTestHelper h) {
    process(h, AdvMachinesBlocks.WATER_JET_CUTTER.get(), Recipes.metalformerCutting);
  }

  @GameTest(template = EMPTY)
  public static void thermalWasher(GameTestHelper h) {
    process(h, AdvMachinesBlocks.THERMAL_WASHER.get(), Recipes.oreWashing);
  }

  @GameTest(template = EMPTY)
  public static void recyclerCompactsAndDoesNotWasteIncompleteScrap(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.COMPACTING_RECYCLER.get());
    machine.inputSlot.put(new ItemStack(Ic2Items.SCRAP, 8));
    heat(machine, 10000);
    machine.getComponent(Energy.class).addEnergy(10000);
    machine.updateEntityServer();
    h.assertTrue(
        machine.inputSlot.get().getCount() == 8 && machine.getEnergy() == 10000,
        "incomplete scrap batch must wait");
    machine.inputSlot.put(new ItemStack(Ic2Items.SCRAP, 18));
    heat(machine, 10000);
    machine.updateEntityServer();
    machine.updateEntityServer();
    h.assertTrue(
        machine.inputSlot.isEmpty()
            && machine.outputSlot.get().is(Ic2Items.SCRAP_BOX)
            && machine.outputSlot.get().getCount() == 2,
        "18 scrap becomes exactly two boxes");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void recyclerRetainsBaseBlacklistAndChance(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.COMPACTING_RECYCLER.get());
    int successes = 0;
    machine.getComponent(Energy.class).addEnergy(10000);
    heat(machine, 10000);
    ItemStack ordinary = new ItemStack(Items.COBBLESTONE, 64);
    h.assertTrue(!TileEntityRecycler.getIsItemBlacklisted(ordinary), "test input is recyclable");
    h.getLevel().random.setSeed(6849721);
    for (int batch = 0; batch < 8; batch++) {
      machine.inputSlot.put(ordinary.copy());
      for (int tick = 0; tick < 64; tick++) machine.updateEntityServer();
      successes += machine.outputSlot.get().getCount();
      machine.outputSlot.put(ItemStack.EMPTY);
    }
    h.assertTrue(
        successes > 30 && successes < 100,
        "recycling keeps a probabilistic 1-in-8 yield, not guaranteed scrap");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void blockedOutputAndChangedInputCannotCreateFreeWork(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.ROTARY_MACERATOR.get());
    var recipe = first(h, Recipes.macerator);
    var input =
        recipe.getInput().getInputs().getFirst().copyWithCount(recipe.getInput().getAmount());
    machine.inputSlot.put(input);
    heat(machine, 10000);
    machine.getComponent(Energy.class).addEnergy(10000);
    machine.updateEntityServer();
    int paidProgress = machine.progress;
    double paidEnergy = machine.getEnergy();
    for (int i = 0; i < machine.outputSlot.size(); i++)
      machine.outputSlot.put(i, new ItemStack(Items.BEDROCK, 64));
    for (int i = 0; i < 20; i++) machine.updateEntityServer();
    h.assertTrue(
        machine.getEnergy() == paidEnergy && machine.progress == paidProgress,
        "blocked output pauses paid work without EU loss");
    h.assertTrue(
        machine.inputSlot.get().getCount() == input.getCount(),
        "blocked output cannot consume input");
    machine.inputSlot.put(ItemStack.EMPTY);
    machine.updateEntityServer();
    h.assertTrue(machine.progress == 0, "removing input resets partial work");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void redstoneWarmsUpAndPowerLossCoolsDown(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.ROTARY_MACERATOR.get());
    h.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
    machine.redstone.update();
    machine.getComponent(Energy.class).addEnergy(1000);
    for (int i = 0; i < 40; i++) machine.updateEntityServer();
    h.assertTrue(
        machine.getRawHeat() == 40 && machine.getEnergy() == 960,
        "idle redstone warm-up costs exactly 1 EU/t");
    machine.getComponent(Energy.class).useEnergy(960);
    for (int i = 0; i < 20; i++) machine.updateEntityServer();
    h.assertTrue(machine.getRawHeat() == 0, "without EU the machine cools by two units per tick");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void waterCapabilityIsSimulationSafeAndRejectsLava(GameTestHelper h) {
    var machine =
        (TileEntityHeatingWaterMachine) machine(h, AdvMachinesBlocks.WATER_JET_CUTTER.get());
    for (var side : Direction.values()) {
      var fluid =
          h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, machine.getBlockPos(), side);
      h.assertTrue(fluid != null, "fluid pipes can connect on every side");
      h.assertTrue(
          fluid.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE) == 0, "lava rejected");
      h.assertTrue(
          fluid.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE) == 1000,
          "fill simulation supported");
      h.assertTrue(machine.getWaterAmount() == 0, "simulation never changes the tank");
    }
    var fluid =
        h.getLevel()
            .getCapability(Capabilities.FluidHandler.BLOCK, machine.getBlockPos(), Direction.UP);
    h.assertTrue(
        fluid.fill(new FluidStack(Fluids.WATER, 10000), FluidAction.EXECUTE) == 8000,
        "tank clamps to 8 buckets");
    h.assertTrue(
        fluid.drain(1000, FluidAction.EXECUTE).isEmpty(), "external draining remains disabled");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void dryWaterMachinesNeverConsumeInputOrEu(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.WATER_JET_CUTTER.get());
    var recipe = first(h, Recipes.metalformerCutting);
    machine.inputSlot.put(
        recipe.getInput().getInputs().getFirst().copyWithCount(recipe.getInput().getAmount()));
    machine.getComponent(Energy.class).addEnergy(10000);
    heat(machine, 10000);
    for (int i = 0; i < 30; i++) machine.updateEntityServer();
    h.assertTrue(
        machine.getEnergy() == 10000
            && !machine.inputSlot.isEmpty()
            && machine.outputSlot.isEmpty(),
        "dry machine waits without losses");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void allMachinesHaveMenusRecipesAndAutomation(GameTestHelper h) {
    for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
      var machine = machine(h, entry.get());
      h.assertTrue(GuiDefs.parse(machine) != null, "machine GUI parses");
      var player = TestPlayers.create(h);
      var menu = machine.createServerScreenHandler(1, player);
      h.assertTrue(menu != null && !menu.slots.isEmpty(), "real synchronized container exists");
      h.assertTrue(
          h.getLevel()
              .getRecipeManager()
              .byKey(IC2RAddons.id("shaped/" + entry.getId().getPath()))
              .isPresent(),
          "machine has a loaded crafting recipe");
      for (var side : Direction.values())
        h.assertTrue(
            h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, machine.getBlockPos(), side)
                != null,
            "item pipes can connect");
      h.assertTrue(!machine.inputSlot.accepts(ItemStack.EMPTY), "empty input inspection is safe");
      var wrench = (ic2.api.tile.IWrenchAble) entry.get();
      h.assertTrue(
          wrench.setFacing(h.getLevel(), machine.getBlockPos(), Direction.EAST, player),
          "IC2 and bridge wrenches can rotate the upgraded machine");
      h.assertTrue(
          wrench
              .getWrenchDrops(
                  h.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine, player, 0)
              .getFirst()
              .is(entry.get().asItem()),
          "IC2 wrench keeps the upgraded machine");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void heatWorkEnergyInventoryAndWaterSurviveSaving(GameTestHelper h) {
    var machine = machine(h, AdvMachinesBlocks.WATER_JET_CUTTER.get());
    var recipe = first(h, Recipes.metalformerCutting);
    machine.inputSlot.put(
        recipe.getInput().getInputs().getFirst().copyWithCount(recipe.getInput().getAmount()));
    fill(machine);
    heat(machine, 8000);
    machine.getComponent(Energy.class).addEnergy(10000);
    machine.updateEntityServer();
    var tag = machine.saveWithFullMetadata(h.getLevel().registryAccess());
    var restored =
        (TileEntityHeatingWaterMachine)
            BlockEntity.loadStatic(
                machine.getBlockPos(), machine.getBlockState(), tag, h.getLevel().registryAccess());
    h.assertTrue(
        restored.getRawHeat() == machine.getRawHeat() && restored.progress == machine.progress,
        "heat and partial work persist");
    h.assertTrue(restored.getEnergy() == machine.getEnergy(), "paid EU persists");
    h.assertTrue(
        restored.getWaterAmount() == ((TileEntityHeatingWaterMachine) machine).getWaterAmount(),
        "water persists");
    h.assertTrue(
        ItemStack.matches(restored.inputSlot.get(), machine.inputSlot.get()), "input persists");
    h.succeed();
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void machinesReceiveNativeIc2Eu(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CESU);
    var source =
        (ic2.core.block.wiring.tileentity.TileEntityElectricBlock)
            h.getBlockEntity(new BlockPos(1, 2, 1));
    source.energy.addEnergy(10000);
    var machine = machine(h, AdvMachinesBlocks.ROTARY_MACERATOR.get());
    h.succeedWhen(
        () ->
            h.assertTrue(
                machine.getEnergy() > 0,
                "tier-2 energy reaches the machine through the native energy network"));
  }

  @GameTest(template = EMPTY)
  public static void thermalShockOnlyOnHotEmptyTankRefill(GameTestHelper h) {
    var washer = (TileEntityThermalWasher) machine(h, AdvMachinesBlocks.THERMAL_WASHER.get());
    heat(washer, 4999);
    washer.tank.fillMb(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    h.assertTrue(!washer.isThermalShockPending(), "below 50 percent refill is safe");
    heat(washer, 5000);
    h.assertTrue(
        washer.isThermalShockPending(), "50 percent hot empty refill triggers the original hazard");
    washer.waterTickFinished();
    h.assertTrue(!washer.isThermalShockPending(), "already filled hot tanks are safe");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void waterContainersReturnEmptyBuckets(GameTestHelper h) {
    var machine =
        (TileEntityHeatingWaterMachine) machine(h, AdvMachinesBlocks.WATER_JET_CUTTER.get());
    machine.fluidSlot.put(new ItemStack(Items.WATER_BUCKET));
    machine.updateEntityServer();
    h.assertTrue(machine.getWaterAmount() == 1000, "water bucket fills the IC2R tank");
    h.assertTrue(
        machine.fluidSlot.isEmpty() && machine.cellSlot.get().is(Items.BUCKET),
        "empty bucket is returned");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void thermalShockUsesCancellableIc2Explosion(GameTestHelper h) {
    var washer = (TileEntityThermalWasher) machine(h, AdvMachinesBlocks.THERMAL_WASHER.get());
    heat(washer, 5000);
    washer.tank.fillMb(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    var seen = new java.util.concurrent.atomic.AtomicBoolean();
    java.util.function.Consumer<ic2.api.event.ExplosionEvent> listener =
        event -> {
          if (event.getLevel() == h.getLevel()
              && event.pos.distanceTo(
                      net.minecraft.world.phys.Vec3.atCenterOf(washer.getBlockPos()))
                  < 0.1) {
            seen.set(true);
            event.setCanceled(true);
          }
        };
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
    try {
      washer.updateEntityServer();
      h.assertTrue(seen.get(), "hot refill invokes the native, cancellable IC2R thermal explosion");
      h.assertTrue(
          !washer.isThermalShockPending(),
          "a cancelled explosion does not repeat for the same refill");
    } finally {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);
    }
    h.succeed();
  }
}
