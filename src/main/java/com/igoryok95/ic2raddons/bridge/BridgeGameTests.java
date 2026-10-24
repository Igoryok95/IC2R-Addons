package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.solar.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class BridgeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static boolean bc() {
    return ModList.get().isLoaded("buildcraftenergy");
  }

  @GameTest(template = EMPTY)
  public static void retiredSolarTiersMigrateToQuantumWithBoundedStorage(GameTestHelper h) {
    var pos = new net.minecraft.core.BlockPos(1, 1, 1);
    h.setBlock(pos, AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get());
    com.igoryok95.ic2raddons.solar.te.TileEntitySolarPanel panel = h.getBlockEntity(pos);
    panel.chargeSlot.put(new net.minecraft.world.item.ItemStack(ic2.core.ref.Ic2Items.RE_BATTERY));
    panel.getComponent(ic2.core.block.comp.Energy.class).forceAddEnergy(100_000_000);
    for (String tier : java.util.List.of("spectral", "singular", "light_absorbing", "photonic")) {
      for (String namespace : java.util.List.of(IC2RAddons.MODID, "solar_gravisuite")) {
        var old = ResourceLocation.fromNamespaceAndPath(namespace, tier + "_solar_panel");
        h.assertTrue(
            !BuiltInRegistries.BLOCK.keySet().contains(old), "retired blocks are aliases only");
        h.assertTrue(
            BuiltInRegistries.BLOCK.get(old) == AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get(),
            "placed panel migration");
        h.assertTrue(
            BuiltInRegistries.ITEM.get(old) == AdvSolarBlocks.ITEM_QUANTUM_SOLAR_PANEL.get(),
            "inventory panel migration");
        var stackTag = new net.minecraft.nbt.CompoundTag();
        stackTag.putString("id", old.toString());
        stackTag.putInt("count", 3);
        var stack =
            net.minecraft.world.item.ItemStack.parseOptional(
                h.getLevel().registryAccess(), stackTag);
        h.assertTrue(
            stack.is(AdvSolarBlocks.ITEM_QUANTUM_SOLAR_PANEL.get()) && stack.getCount() == 3,
            "serialized item stacks keep their count");
        var saved = panel.saveWithFullMetadata(h.getLevel().registryAccess());
        saved.putString("id", old.toString());
        var restored =
            net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                panel.getBlockPos(), panel.getBlockState(), saved, h.getLevel().registryAccess());
        h.assertTrue(
            restored instanceof com.igoryok95.ic2raddons.solar.te.TileEntityQuantumSolar,
            "retired machine loads as quantum");
        var copy = (com.igoryok95.ic2raddons.solar.te.TileEntityQuantumSolar) restored;
        h.assertTrue(
            copy.getComponent(ic2.core.block.comp.Energy.class).getEnergy() == 10_000_000,
            "EU clamps to quantum capacity");
        h.assertTrue(
            copy.chargeSlot.get().is(ic2.core.ref.Ic2Items.RE_BATTERY),
            "charging slot survives migration");
        h.assertTrue(
            h.getLevel()
                .getRecipeManager()
                .byKey(IC2RAddons.id("shaped/" + tier + "_solar_panel"))
                .isEmpty(),
            "retired tiers cannot be crafted");
      }
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void legacyEquipmentAndBlockEntitiesMigrateWithoutDataLoss(GameTestHelper h) {
    var stack =
        new net.minecraft.world.item.ItemStack(
            com.igoryok95.ic2raddons.equipment.GraviItems.RELOCATOR.get());
    var previous = new net.minecraft.nbt.CompoundTag();
    previous.putInt("selected", 4);
    net.minecraft.world.item.component.CustomData.update(
        net.minecraft.core.component.DataComponents.CUSTOM_DATA,
        stack,
        root -> root.put("solar_gravisuite", previous));
    h.assertTrue(
        com.igoryok95.ic2raddons.equipment.ItemState.read(stack).getInt("selected") == 4,
        "Legacy equipment settings must load");
    com.igoryok95.ic2raddons.equipment.ItemState.update(
        stack, root -> root.putBoolean("enabled", true));
    h.assertTrue(
        com.igoryok95.ic2raddons.equipment.ItemState.read(stack).getInt("selected") == 4,
        "Migration must retain settings");
    h.setBlock(new net.minecraft.core.BlockPos(1, 1, 1), AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    var panel = h.getBlockEntity(new net.minecraft.core.BlockPos(1, 1, 1));
    var saved = panel.saveWithFullMetadata(h.getLevel().registryAccess());
    saved.putString("id", "solar_gravisuite:advanced_solar_panel");
    var restored =
        net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
            panel.getBlockPos(), panel.getBlockState(), saved, h.getLevel().registryAccess());
    h.assertTrue(
        restored != null && restored.getType() == panel.getType(),
        "Old block entity IDs must decode");
    h.succeed();
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void optionalBridgeMatchesInstalledMods(GameTestHelper h) {
    h.assertTrue(
        BuiltInRegistries.BLOCK.containsKey(IC2RAddons.id("electric_engine")) == bc(),
        "Engine registration must follow BC availability");
    h.assertTrue(
        BuiltInRegistries.ITEM.get(
                ResourceLocation.parse("solar_gravisuite:advanced_diamond_drill"))
            == BuiltInRegistries.ITEM.get(IC2RAddons.id("advanced_diamond_drill")),
        "Old items must resolve through migration aliases");
    h.assertTrue(
        BuiltInRegistries.BLOCK.get(ResourceLocation.parse("solar_gravisuite:advanced_solar_panel"))
            == AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get(),
        "Old placed panels must resolve");
    h.succeed();
  }
}
