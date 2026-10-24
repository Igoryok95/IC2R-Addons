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
