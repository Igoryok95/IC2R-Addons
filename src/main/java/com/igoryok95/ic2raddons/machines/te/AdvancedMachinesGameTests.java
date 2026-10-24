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

  private static MachineRecipe<IRecipeInput, Collection<ItemStack>> first(
      GameTestHelper h,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    for (var recipe : manager.get(h.getLevel()).getRecipes())
      if (!recipe.getOutput().isEmpty() && !recipe.getInput().getInputs().isEmpty()) return recipe;
    throw new IllegalStateException("IC2R recipe manager has no usable recipe");
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
