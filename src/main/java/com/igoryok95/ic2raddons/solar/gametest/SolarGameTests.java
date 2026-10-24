package com.igoryok95.ic2raddons.solar.gametest;

import com.igoryok95.ic2raddons.solar.*;
import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import com.igoryok95.ic2raddons.solar.item.ItemSolarHelmet;
import com.igoryok95.ic2raddons.solar.recipe.*;
import com.igoryok95.ic2raddons.solar.te.*;
import ic2.api.item.*;
import ic2.core.block.comp.Energy;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class SolarGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void restoredPanelRecipesCraftWithTheRequiredPreviousTier(GameTestHelper h) {
    int[] previousCount = {2, 4, 4, 4};
    String[] names = {"spectral", "singular", "light_absorbing", "photonic"};
    for (int i = 0; i < names.length; i++) {
      var recipe =
          (CraftingRecipe)
              h.getLevel()
                  .getRecipeManager()
                  .byKey(IC2RAddons.id("shaped/" + names[i] + "_solar_panel"))
                  .orElseThrow()
                  .value();
      var stacks =
          recipe.getIngredients().stream()
              .map(
                  ingredient ->
                      ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy())
              .toList();
      var input = CraftingInput.of(3, 3, stacks);
      h.assertTrue(recipe.matches(input, h.getLevel()), "late-panel recipe can be crafted");
      var result = recipe.assemble(input, h.getLevel().registryAccess());
      h.assertTrue(
          result.is(BuiltInRegistries.ITEM.get(IC2RAddons.id(names[i] + "_solar_panel"))),
          "correct late-panel output");
      long consumed =
          stacks.stream()
              .filter(
                  stack ->
                      stack.getItem() instanceof com.igoryok95.ic2raddons.solar.item.ItemSolarPanel)
              .count();
      h.assertTrue(
          consumed == previousCount[i], "recipe consumes the actual preceding generation capacity");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void latePanelsKeepGenerationWithinSixthTierPackets(GameTestHelper h) {
    for (var type : SolarPanelType.values()) {
      var pos = new BlockPos(1, 1, 1);
      h.setBlock(
          pos,
          BuiltInRegistries.BLOCK.get(
              IC2RAddons.id(type.name().toLowerCase(Locale.ROOT) + "_solar_panel")));
      TileEntitySolarPanel panel = h.getBlockEntity(pos);
      var energy = panel.getComponent(Energy.class);
      h.assertTrue(energy.getSourceTier() <= 6, "all panels support the modern IC2R voltage range");
      h.assertTrue(
          energy.getPacketOutput() == type.getPacketCount(),
          "configured packet count matches the tooltip");
      h.assertTrue(
          type.getMaxOutput() * type.getPacketCount() >= type.dayPower,
          "default generation can leave the panel buffer");
    }
    h.assertTrue(
        SolarPanelType.LIGHT_ABSORBING.getPacketCount() == 4
            && SolarPanelType.PHOTONIC.getPacketCount() == 16,
        "approved multi-packet adaptation retains original late-tier power");
    h.succeed();
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void photonicFeedsPesuThroughPlasmaWithoutOvervoltage(GameTestHelper h) {
    var wire = new BlockPos(1, 1, 1);
    h.setBlock(wire.above(), AdvSolarBlocks.PHOTONIC_SOLAR_PANEL.get());
    h.setBlock(wire.above(2), Blocks.STONE);
    h.setBlock(wire, ic2.core.classic.ClassicWiring.PLASMA);
    h.setBlock(wire.below(), ic2.core.classic.ClassicEnergy.PESU);
    TileEntitySolarPanel source = h.getBlockEntity(wire.above());
    ic2.core.classic.ClassicEnergy.Storage sink = h.getBlockEntity(wire.below());
    source.getComponent(Energy.class).addEnergy(10_000_000);
    h.succeedWhen(
        () -> {
          h.assertTrue(
              sink.energy.getEnergy() > 500_000,
              "multiple safe packets reach PESU through one cable");
          h.assertBlockPresent(ic2.core.classic.ClassicWiring.PLASMA, wire);
          h.assertBlockPresent(ic2.core.classic.ClassicEnergy.PESU, wire.below());
          h.assertTrue(
              source.getComponent(Energy.class).getEnergy() + sink.energy.getEnergy() <= 10_000_000,
              "multi-packet transfer does not create energy");
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void quantumFeedsEsuThroughTheNativeEvTransformer(GameTestHelper h) {
    var middle = new BlockPos(1, 1, 1);
    h.setBlock(middle.above(), AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get());
    h.setBlock(middle.above(2), Blocks.STONE);
    h.setBlock(
        middle,
        Ic2Blocks.EV_TRANSFORMER
            .defaultBlockState()
            .setValue(
                ic2.core.block.tileentity.Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
    h.setBlock(middle.below(), ic2.core.classic.ClassicEnergy.ESU);
    TileEntitySolarPanel source = h.getBlockEntity(middle.above());
    source.getComponent(Energy.class).addEnergy(100_000);
    ic2.core.classic.ClassicEnergy.Storage sink = h.getBlockEntity(middle.below());
    h.succeedWhen(
        () -> {
          h.assertTrue(
              sink.energy.getEnergy() >= 2048,
              "Quantum safely charges ESU through native step-down");
          h.assertBlockPresent(ic2.core.classic.ClassicEnergy.ESU, middle.below());
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void quantumFeedsIsuThroughTheClassicIvTransformer(GameTestHelper h) {
    var middle = new BlockPos(1, 1, 1);
    h.setBlock(middle.above(), AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get());
    h.setBlock(middle.above(2), Blocks.STONE);
    h.setBlock(
        middle,
        ic2.core.classic.ClassicWiring.IV_TRANSFORMER
            .defaultBlockState()
            .setValue(
                ic2.core.block.tileentity.Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
    h.setBlock(middle.below(), ic2.core.classic.ClassicEnergy.ISU);
    TileEntitySolarPanel source = h.getBlockEntity(middle.above());
    source.getComponent(Energy.class).addEnergy(100_000);
    ic2.core.classic.ClassicEnergy.Storage sink = h.getBlockEntity(middle.below());
    h.succeedWhen(
        () -> {
          h.assertTrue(
              sink.energy.getEnergy() >= 8192,
              "Quantum safely charges ISU through the new transformer");
          h.assertBlockPresent(ic2.core.classic.ClassicEnergy.ISU, middle.below());
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void quantumChargesLargePesuThroughPlasmaAndPreservesTheSavedReading(
      GameTestHelper h) {
    var wire = new BlockPos(1, 1, 1);
    h.setBlock(wire.above(), AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get());
    h.setBlock(wire.above(2), Blocks.STONE);
    h.setBlock(wire, ic2.core.classic.ClassicWiring.PLASMA);
    h.setBlock(wire.below(), ic2.core.classic.ClassicEnergy.PESU);
    TileEntitySolarPanel source = h.getBlockEntity(wire.above());
    source.getComponent(Energy.class).addEnergy(100_000);
    ic2.core.classic.ClassicEnergy.Storage sink = h.getBlockEntity(wire.below());
    sink.energy.addEnergy(2_999_999_999D);
    h.succeedWhen(
        () -> {
          h.assertTrue(
              sink.getStoredLong() > 3_000_000_000L, "Quantum charges PESU above integer range");
          var saved = sink.saveWithFullMetadata(h.getLevel().registryAccess());
          var restored =
              (ic2.core.classic.ClassicEnergy.Storage)
                  net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                      sink.getBlockPos(),
                      sink.getBlockState(),
                      saved,
                      h.getLevel().registryAccess());
          h.assertTrue(
              restored != null && restored.getStoredLong() == sink.getStoredLong(),
              "large network reading survives saving");
          h.assertBlockPresent(ic2.core.classic.ClassicWiring.PLASMA, wire);
        });
  }

  @GameTest(template = EMPTY)
  public static void transformerDoesNotOccludeFloorOrLight(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    h.assertTrue(
        !h.getBlockState(new BlockPos(1, 1, 1)).canOcclude(),
        "open transformer model must not cull adjacent floor faces");
    h.assertTrue(
        h.getBlockState(new BlockPos(1, 1, 1))
                .getLightBlock(h.getLevel(), h.absolutePos(new BlockPos(1, 1, 1)))
            < 15,
        "model must not block all light");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void eightPanelProfilesGenerateExactEu(GameTestHelper h) {
    h.assertTrue(SolarPanelType.values().length == 8, "all eight solar tiers are registered");
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    for (var type : SolarPanelType.values()) {
      var block =
          BuiltInRegistries.BLOCK.get(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", type.name().toLowerCase(Locale.ROOT) + "_solar_panel"));
      h.setBlock(new BlockPos(1, 1, 1), block);
      TileEntitySolarPanel te = h.getBlockEntity(new BlockPos(1, 1, 1));
      double before = te.getComponent(Energy.class).getEnergy();
      te.gainEnergy();
      h.assertTrue(
          te.getComponent(Energy.class).getEnergy() - before == type.dayPower,
          "day generation for " + type);
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nightAndRoofGeneration(GameTestHelper h) {
    h.getLevel().setDayTime(18000);
    h.getLevel().updateSkyBrightness();
    for (var profile : SolarPanelType.values()) {
      var pos = new BlockPos(1, 1, 1);
      h.setBlock(pos.above(), Blocks.AIR);
      h.setBlock(
          pos,
          BuiltInRegistries.BLOCK.get(
              IC2RAddons.id(profile.name().toLowerCase(Locale.ROOT) + "_solar_panel")));
      TileEntitySolarPanel panel = h.getBlockEntity(pos);
      panel.gainEnergy();
      h.assertTrue(
          panel.getComponent(Energy.class).getEnergy() == profile.nightPower,
          "rated night output: " + profile);
      h.setBlock(pos.above(), Blocks.STONE);
      panel.gainEnergy();
      h.assertTrue(
          panel.getComponent(Energy.class).getEnergy() == profile.nightPower,
          "roof stops generation: " + profile);
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void panelsChargeIc2Items(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    TileEntitySolarPanel te = h.getBlockEntity(new BlockPos(1, 1, 1));
    ItemStack battery = new ItemStack(Ic2Items.RE_BATTERY);
    te.chargeSlot.put(battery);
    te.getComponent(Energy.class).addEnergy(1000);
    h.succeedWhen(
        () ->
            h.assertTrue(
                ElectricItem.manager.getCharge(battery) > 0,
                "panel must charge real IC2R battery"));
  }

  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void panelsFeedIc2EnergyNetwork(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    h.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.MFSU);
    TileEntitySolarPanel source = h.getBlockEntity(new BlockPos(1, 1, 1));
    source.getComponent(Energy.class).addEnergy(10000);
    ic2.core.block.wiring.tileentity.TileEntityElectricBlock sink =
        h.getBlockEntity(new BlockPos(2, 1, 1));
    h.succeedWhen(
        () ->
            h.assertTrue(
                sink.energy.getEnergy() > 0, "solar source must transfer EU into real MFSU"));
  }

  @GameTest(template = EMPTY)
  public static void helmetProfilesImproveIc2Armor(GameTestHelper h) {
    Item[] helmets = {
      AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
      AdvSolarBlocks.HYBRID_SOLAR_HELMET.get(),
      AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
      AdvSolarBlocks.QUANTUM_SOLAR_HELMET.get()
    };
    double[] capacities = {8000000, 40000000, 100000000, 100000000};
    int[] tiers = {3, 4, 4, 5};
    double[] transfers = {5000, 12000, 50000, 200000};
    for (int i = 0; i < helmets.length; i++) {
      ItemStack stack = new ItemStack(helmets[i]);
      IElectricItem electric = (IElectricItem) helmets[i];
      h.assertTrue(electric.getMaxCharge(stack) == capacities[i], "modern helmet capacity");
      h.assertTrue(electric.getTier(stack) == tiers[i], "helmet voltage progression");
      h.assertTrue(electric.getTransferLimit(stack) == transfers[i], "helmet transfer progression");
      h.assertTrue(
          ElectricItem.manager.charge(
                  stack, Double.POSITIVE_INFINITY, electric.getTier(stack), true, false)
              == capacities[i],
          "full IC2R charge");
    }
    h.assertTrue(
        helmets[0] instanceof ic2.core.item.armor.ItemArmorNanoSuit, "nano abilities retained");
    h.assertTrue(
        helmets[1] instanceof ic2.core.item.armor.ItemArmorQuantumSuit,
        "quantum abilities retained");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void quantumHelmetProducesItsRatedEuOnlyWhileWorn(GameTestHelper h) {
    var level = h.getLevel();
    level.setDayTime(6000);
    level.updateSkyBrightness();
    level.setWeatherParameters(10000, 0, false, false);
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    ItemStack helmet = new ItemStack(AdvSolarBlocks.QUANTUM_SOLAR_HELMET.get());
    var item = helmet.getItem();
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    player.teleportTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    item.inventoryTick(helmet, level, player, 0, false);
    h.assertTrue(ElectricItem.manager.getCharge(helmet) == 4096, "quantum daylight generation");
    level.setDayTime(18000);
    level.updateSkyBrightness();
    item.inventoryTick(helmet, level, player, 0, false);
    h.assertTrue(ElectricItem.manager.getCharge(helmet) == 6144, "quantum night generation");
    var roof = player.blockPosition().above();
    level.setBlockAndUpdate(roof, Blocks.STONE.defaultBlockState());
    item.inventoryTick(helmet, level, player, 0, false);
    h.assertTrue(ElectricItem.manager.getCharge(helmet) == 6144, "a roof stops helmet generation");
    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    level.setBlockAndUpdate(roof, Blocks.AIR.defaultBlockState());
    item.inventoryTick(helmet, level, player, 0, false);
    h.assertTrue(
        ElectricItem.manager.getCharge(helmet) == 6144, "inventory helmets cannot generate");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void quantumHelmetRecipeCarriesChargeAndRequiresThePreviousTier(GameTestHelper h) {
    CraftingRecipe recipe =
        (CraftingRecipe)
            h.getLevel()
                .getRecipeManager()
                .byKey(IC2RAddons.id("shaped/quantum_solar_helmet"))
                .orElseThrow()
                .value();
    var previous =
        ElectricItemManager.getCharged(AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(), 1234567);
    var crystal = ElectricItemManager.getCharged(Ic2Items.LAPOTRON_CRYSTAL, 2000);
    var input =
        CraftingInput.of(
            3,
            3,
            List.of(
                ItemStack.EMPTY,
                new ItemStack(AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get()),
                ItemStack.EMPTY,
                new ItemStack(Ic2Items.ADVANCED_CIRCUIT),
                previous,
                new ItemStack(Ic2Items.ADVANCED_CIRCUIT),
                ItemStack.EMPTY,
                crystal,
                ItemStack.EMPTY));
    h.assertTrue(
        recipe.matches(input, h.getLevel()),
        "quantum upgrade requires ultimate helmet and quantum panel");
    var result = recipe.assemble(input, h.getLevel().registryAccess());
    h.assertTrue(result.is(AdvSolarBlocks.QUANTUM_SOLAR_HELMET.get()), "new quantum helmet output");
    h.assertTrue(
        ElectricItem.manager.getCharge(result) == 1236567,
        "ingredient EU survives quantum upgrade");
    h.assertTrue(
        ElectricItem.manager.getCharge(previous) == 1234567,
        "recipe preview cannot drain ingredients");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void helmetsGenerateEveryTickAndConserveEu(GameTestHelper h) {
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    player.teleportTo(
        h.absolutePos(new BlockPos(1, 1, 1)).getX() + 0.5,
        h.absolutePos(new BlockPos(1, 1, 1)).getY(),
        h.absolutePos(new BlockPos(1, 1, 1)).getZ() + 0.5);
    ItemStack helmet = new ItemStack(AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get()),
        tool = new ItemStack(Ic2Items.DIAMOND_DRILL);
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    player.setItemSlot(EquipmentSlot.MAINHAND, tool);
    double total = 0;
    for (int i = 0; i < 20; i++)
      total += ItemSolarHelmet.generateAndCharge(player, helmet, 512, 64);
    h.assertTrue(total == 10240, "20 ticks must produce 20 times 512 EU");
    h.assertTrue(
        ElectricItem.manager.getCharge(tool) + ElectricItem.manager.getCharge(helmet) == total,
        "no duplicated or lost accepted EU");
    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    h.assertTrue(
        ItemSolarHelmet.generateAndCharge(player, helmet, 512, 64) == 0,
        "unequipped helmet cannot generate");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void transformerSpendsExactEnergyAndConsumesOnce(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    TileEntityMolecularTransformer te = h.getBlockEntity(new BlockPos(1, 1, 1));
    te.inputSlot.put(new ItemStack(Items.NETHERRACK, 2));
    te.getComponent(Energy.class).addEnergy(70000);
    h.succeedWhen(
        () -> {
          h.assertTrue(te.outputSlot.get().is(Items.GUNPOWDER), "expected molecular output");
          h.assertTrue(te.outputSlot.get().getCount() == 2, "output count");
          h.assertTrue(te.inputSlot.get(0).getCount() == 1, "consume exactly one input");
          h.assertTrue(te.getComponent(Energy.class).getEnergy() == 0, "spend exactly 70000 EU");
        });
  }

  @GameTest(template = EMPTY)
  public static void transformerPersistsPartialWork(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    TileEntityMolecularTransformer te = h.getBlockEntity(new BlockPos(1, 1, 1));
    te.inputSlot.put(new ItemStack(Items.GLOWSTONE_DUST));
    te.getComponent(Energy.class).addEnergy(1000);
    h.runAfterDelay(
        3,
        () -> {
          h.assertTrue(te.energyUsed == 1000, "partial energy recorded");
          var tag = te.saveWithFullMetadata(h.getLevel().registryAccess());
          var restored =
              net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                  te.getBlockPos(), te.getBlockState(), tag, h.getLevel().registryAccess());
          h.assertTrue(
              restored instanceof TileEntityMolecularTransformer, "machine type survives save");
          var copy = (TileEntityMolecularTransformer) restored;
          h.assertTrue(
              copy.energyUsed == 1000 && copy.inputSlot.get(0).is(Items.GLOWSTONE_DUST),
              "partial progress and unconsumed input survive load");
          h.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void transformerWaitsForOutputSpace(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    TileEntityMolecularTransformer te = h.getBlockEntity(new BlockPos(1, 1, 1));
    te.inputSlot.put(new ItemStack(Items.NETHERRACK));
    te.outputSlot.put(new ItemStack(Items.STONE, 64));
    te.getComponent(Energy.class).addEnergy(70000);
    h.runAfterDelay(
        4,
        () -> {
          h.assertTrue(te.inputSlot.get(0).is(Items.NETHERRACK), "blocked output preserves input");
          h.assertTrue(
              te.getComponent(Energy.class).getEnergy() == 70000, "blocked output preserves EU");
          h.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void transformerDoesNotReuseProgressForDifferentInput(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
    TileEntityMolecularTransformer te = h.getBlockEntity(new BlockPos(1, 1, 1));
    te.inputSlot.put(new ItemStack(Items.GLOWSTONE_DUST));
    te.getComponent(Energy.class).addEnergy(1000);
    h.runAfterDelay(
        3,
        () -> {
          h.assertTrue(te.energyUsed == 1000, "original partial work");
          te.inputSlot.put(new ItemStack(Items.COAL));
          h.runAfterDelay(
              2,
              () -> {
                h.assertTrue(
                    te.energyUsed == 0 && te.currentOutput.is(Items.DIAMOND),
                    "different input resets progress and output");
                h.succeed();
              });
        });
  }

  @GameTest(template = EMPTY)
  public static void helmetUpgradePreservesIngredientCharge(GameTestHelper h) {
    var holder =
        h.getLevel()
            .getRecipeManager()
            .byKey(
                ResourceLocation.fromNamespaceAndPath(
                    "ic2r_addons", "shaped/advanced_solar_helmet"))
            .orElseThrow();
    CraftingRecipe recipe = (CraftingRecipe) holder.value();
    ItemStack original = ElectricItemManager.getCharged(Ic2Items.NANO_HELMET, 5000),
        crystal = ElectricItemManager.getCharged(Ic2Items.ENERGY_CRYSTAL, 2000);
    var input =
        CraftingInput.of(
            3,
            3,
            List.of(
                ItemStack.EMPTY,
                new ItemStack(AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get()),
                ItemStack.EMPTY,
                new ItemStack(Ic2Items.ADVANCED_CIRCUIT),
                original,
                new ItemStack(Ic2Items.ADVANCED_CIRCUIT),
                ItemStack.EMPTY,
                crystal,
                ItemStack.EMPTY));
    h.assertTrue(recipe.matches(input, h.getLevel()), "helmet upgrade recipe matches");
    ItemStack result = recipe.assemble(input, h.getLevel().registryAccess());
    h.assertTrue(
        result.is(AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get())
            && ElectricItem.manager.getCharge(result) == 7000,
        "upgrade carries input EU");
    h.assertTrue(
        ElectricItem.manager.getCharge(original) == 5000, "recipe preview preserves input EU");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void craftingIngredientsResolve(GameTestHelper h) {
    for (var holder : h.getLevel().getRecipeManager().getRecipes()) {
      if (!holder.id().getNamespace().equals("ic2r_addons")) continue;
      if (holder.value() instanceof CraftingRecipe || holder.value() instanceof MTRecipe) {
        for (var ingredient : holder.value().getIngredients())
          h.assertTrue(
              ingredient.isEmpty() || ingredient.getItems().length > 0,
              "unresolved ingredient in " + holder.id());
      }
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void everyItemUsesTheUnifiedCreatorModId(GameTestHelper h) {
    var modList = net.neoforged.fml.ModList.get();
    h.assertTrue(
        !modList.isLoaded("advanced_solar_panels") && !modList.isLoaded("gravisuite"),
        "no legacy loader identities");
    var items =
        java.util.stream.Stream.concat(
                AdvSolarBlocks.ITEMS.getEntries().stream(),
                com.igoryok95.ic2raddons.equipment.GraviItems.ITEMS.getEntries().stream())
            .map(java.util.function.Supplier::get)
            .toList();
    h.assertTrue(items.size() > 40, "both families must be included");
    for (Item item : items) {
      ItemStack stack = new ItemStack(item);
      h.assertTrue(
          BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IC2RAddons.MODID),
          "unified registry identity: " + item);
      h.assertTrue(
          IC2RAddons.MODID.equals(item.getCreatorModId(stack)),
          "JEI creator lookup must use the combined mod: " + item);
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void integratedArmorSetsHaveMatchingCapacityAndOneModContainer(GameTestHelper h) {
    h.assertTrue(
        net.neoforged.fml.ModList.get().isLoaded(IC2RAddons.MODID),
        "combined mod must use its own loader identity");
    Item[][] sets = {
      {
        AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_LEGGINGS.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_BOOTS.get()
      },
      {
        AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.GRAVI_CHESTPLATE.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_LEGGINGS.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_BOOTS.get()
      }
    };
    double[] capacity = {8_000_000, 100_000_000};
    for (int i = 0; i < sets.length; i++)
      for (Item item : sets[i]) {
        ItemStack stack = new ItemStack(item);
        h.assertTrue(
            ((IElectricItem) item).getMaxCharge(stack) == capacity[i],
            "uniform upgraded set capacity: " + item);
        ElectricItem.manager.charge(stack, 1_234_567, Integer.MAX_VALUE, true, false);
        var restored =
            ItemStack.parseOptional(
                h.getLevel().registryAccess(),
                (net.minecraft.nbt.CompoundTag) stack.save(h.getLevel().registryAccess()));
        h.assertTrue(
            restored.is(item) && ElectricItem.manager.getCharge(restored) == 1_234_567,
            "legacy item IDs and EU survive serialization");
      }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void solarHelmetChargesGraviArmorWhenPresent(GameTestHelper h) {
    var id = ResourceLocation.fromNamespaceAndPath("ic2r_addons", "advanced_quant");
    h.assertTrue(
        BuiltInRegistries.ITEM.containsKey(id), "integrated GraviSuite armor must be present");
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    ItemStack helmet = new ItemStack(AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get()),
        chest = new ItemStack(BuiltInRegistries.ITEM.get(id));
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    player.setItemSlot(EquipmentSlot.CHEST, chest);
    ItemSolarHelmet.generateAndCharge(player, helmet, 512, 64);
    h.assertTrue(
        ElectricItem.manager.getCharge(chest) == 512,
        "solar helmet charges GraviSuite chestplate directly");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void allRecipesAndGuisLoad(GameTestHelper h) {
    h.assertTrue(
        h.getLevel()
                .getRecipeManager()
                .getAllRecipesFor(MolecularTransformerRecipes.TYPE.get())
                .size()
            == 15,
        "all molecular recipes loaded");
    for (var entry : AdvSolarBlocks.BLOCKS.getEntries()) {
      h.setBlock(new BlockPos(1, 1, 1), entry.get());
      ic2.core.block.tileentity.Ic2TileEntity te = h.getBlockEntity(new BlockPos(1, 1, 1));
      h.assertTrue(GuiDefs.parse(te) != null, "GUI parses for " + entry.getId());
      if (!entry.getId().getPath().equals("quantum_generator"))
        h.assertTrue(
            h.getLevel()
                .getRecipeManager()
                .byKey(
                    ResourceLocation.fromNamespaceAndPath(
                        "ic2r_addons", "shaped/" + entry.getId().getPath()))
                .isPresent(),
            "craftable machine " + entry.getId());
    }
    for (String name :
        List.of(
            "advanced_solar_helmet",
            "hybrid_solar_helmet",
            "ultimate_solar_helmet",
            "quantum_solar_helmet"))
      h.assertTrue(
          h.getLevel()
              .getRecipeManager()
              .byKey(ResourceLocation.fromNamespaceAndPath("ic2r_addons", "shaped/" + name))
              .isPresent(),
          "helmet crafting recipe");
    h.succeed();
  }
}
