package com.chocohead.advsolar.gametest;

import com.chocohead.advsolar.*;
import com.chocohead.advsolar.gui.GuiDefs;
import com.chocohead.advsolar.item.ItemSolarHelmet;
import com.chocohead.advsolar.recipe.*;
import com.chocohead.advsolar.te.*;
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

@GameTestHolder("advanced_solar_panels")
@PrefixGameTestTemplate(false)
public final class SolarGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

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
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    for (var type : SolarPanelType.values()) {
      var block =
          BuiltInRegistries.BLOCK.get(
              ResourceLocation.fromNamespaceAndPath(
                  "advanced_solar_panels", type.name().toLowerCase(Locale.ROOT) + "_solar_panel"));
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
    h.setBlock(new BlockPos(1, 1, 1), AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    TileEntitySolarPanel te = h.getBlockEntity(new BlockPos(1, 1, 1));
    te.gainEnergy();
    h.assertTrue(te.getComponent(Energy.class).getEnergy() == 1, "night output is 1 EU/t");
    h.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);
    te.gainEnergy();
    h.assertTrue(
        te.getComponent(Energy.class).getEnergy() == 1, "a roof must stop generation immediately");
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
      AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get()
    };
    double[] capacities = {4000000, 20000000, 40000000};
    for (int i = 0; i < helmets.length; i++) {
      ItemStack stack = new ItemStack(helmets[i]);
      IElectricItem electric = (IElectricItem) helmets[i];
      h.assertTrue(electric.getMaxCharge(stack) == capacities[i], "modern helmet capacity");
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
  public static void helmetsGenerateEveryTickAndConserveEu(GameTestHelper h) {
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    var player = h.makeMockServerPlayerInLevel();
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
                    "advanced_solar_panels", "shaped/advanced_solar_helmet"))
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
      if (!holder.id().getNamespace().equals("advanced_solar_panels")) continue;
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
  public static void solarHelmetChargesGraviArmorWhenPresent(GameTestHelper h) {
    var id = ResourceLocation.fromNamespaceAndPath("gravisuite", "advanced_quant");
    if (!BuiltInRegistries.ITEM.containsKey(id)) {
      h.succeed();
      return;
    }
    h.getLevel().setDayTime(6000);
    h.getLevel().updateSkyBrightness();
    h.getLevel().setWeatherParameters(10000, 0, false, false);
    var player = h.makeMockServerPlayerInLevel();
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
                        "advanced_solar_panels", "shaped/" + entry.getId().getPath()))
                .isPresent(),
            "craftable machine " + entry.getId());
    }
    for (String name :
        List.of("advanced_solar_helmet", "hybrid_solar_helmet", "ultimate_solar_helmet"))
      h.assertTrue(
          h.getLevel()
              .getRecipeManager()
              .byKey(
                  ResourceLocation.fromNamespaceAndPath("advanced_solar_panels", "shaped/" + name))
              .isPresent(),
          "helmet crafting recipe");
    h.succeed();
  }
}
