package com.igoryok95.ic2raddons.equipment.gametest;

import com.igoryok95.ic2raddons.bridge.TestPlayers;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.items.MiningTool;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.ElectricItemManager;
import ic2.core.util.Keyboard;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class DrillPathGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos POS = new BlockPos(1, 1, 1);
  private static final double CHARGE = 10000;

  private static List<MiningTool> drills() {
    return List.of(GraviItems.ADVANCED_DRILL.get(), GraviItems.ADVANCED_IRIDIUM_DRILL.get());
  }

  private static InteractionResult use(GameTestHelper h, ServerPlayer player) {
    var pos = h.absolutePos(POS);
    return player.gameMode.useItemOn(
        player,
        h.getLevel(),
        player.getMainHandItem(),
        InteractionHand.MAIN_HAND,
        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
  }

  @GameTest(template = EMPTY)
  public static void allDrillProfilesCreateOnePathAndPayTheirSelectedCost(GameTestHelper h) {
    var player = TestPlayers.create(h);
    for (var tool : drills()) {
      for (int efficiency = 0; efficiency < 3; efficiency++) {
        for (int mining = 0; mining < 2; mining++) {
          var stack = ElectricItemManager.getCharged(tool, CHARGE);
          int eff = efficiency, mode = mining;
          ItemState.update(
              stack,
              tag -> {
                tag.putInt("efficiency", eff);
                tag.putInt("mining", mode);
              });
          player.setItemInHand(InteractionHand.MAIN_HAND, stack);
          for (var ground : List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.ROOTED_DIRT)) {
            h.setBlock(POS, ground);
            h.setBlock(POS.east(), ground);
            double before = ElectricItem.manager.getCharge(stack);
            h.assertTrue(
                use(h, player).consumesAction(), "advanced drill creates path in every profile");
            h.assertBlockPresent(Blocks.DIRT_PATH, POS);
            h.assertBlockPresent(ground, POS.east());
            h.assertValueEqual(
                ElectricItem.manager.getCharge(stack),
                before - tool.cost(stack),
                "one path pays the current profile cost without area mining");
          }
        }
      }
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void advancedPathsRespectLowPowerAndModeShortcuts(GameTestHelper h) {
    var player = TestPlayers.create(h);
    for (var tool : drills()) {
      for (int efficiency = 0; efficiency < 3; efficiency++) {
        var stack = new ItemStack(tool);
        int eff = efficiency;
        ItemState.update(stack, tag -> tag.putInt("efficiency", eff));
        double charge = tool.cost(stack) - 1;
        ElectricItem.manager.charge(stack, charge, Integer.MAX_VALUE, true, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        h.setBlock(POS, Blocks.GRASS_BLOCK);
        h.assertTrue(
            !use(h, player).consumesAction(), "insufficient profile charge cannot flatten");
        h.assertBlockPresent(Blocks.GRASS_BLOCK, POS);
        h.assertValueEqual(
            ElectricItem.manager.getCharge(stack), charge, "failed path costs no EU");
      }
      for (var key : List.of(Keyboard.Key.modeSwitch, Keyboard.Key.alt)) {
        var stack = ElectricItemManager.getCharged(tool, CHARGE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        h.setBlock(POS, Blocks.GRASS_BLOCK);
        IC2.keyboard.processKeyUpdate(player, 1 << key.ordinal());
        try {
          h.assertTrue(use(h, player).consumesAction(), "native mode shortcut must be consumed");
          h.assertBlockPresent(Blocks.GRASS_BLOCK, POS);
          h.assertTrue(
              ItemState.mode(stack, key == Keyboard.Key.alt ? "efficiency" : "mining", 3) == 1,
              "requested mode changes instead of creating a path");
          h.assertValueEqual(
              ElectricItem.manager.getCharge(stack), CHARGE, "shortcut spends no EU");
        } finally {
          IC2.keyboard.processKeyUpdate(player, 0);
        }
      }
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void pathingPreservesTorchFallbackAndVajraBehavior(GameTestHelper h) {
    var player = TestPlayers.create(h);
    for (var tool : drills()) {
      var stack = ElectricItemManager.getCharged(tool, CHARGE);
      player.getInventory().clearContent();
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      var torches = new ItemStack(Items.TORCH, 2);
      player.getInventory().setItem(9, torches);
      h.setBlock(POS, Blocks.GRASS_BLOCK);
      h.setBlock(POS.above(), Blocks.AIR);
      h.assertTrue(
          use(h, player).consumesAction(), "ground uses path action before torch fallback");
      h.assertBlockPresent(Blocks.DIRT_PATH, POS);
      h.assertBlockPresent(Blocks.AIR, POS.above());
      h.assertValueEqual(torches.getCount(), 2, "path action must not consume a torch");
      double afterPath = ElectricItem.manager.getCharge(stack);
      h.setBlock(POS, Blocks.STONE);
      h.assertTrue(use(h, player).consumesAction(), "stone keeps existing torch placement");
      h.assertBlockPresent(Blocks.TORCH, POS.above());
      h.assertValueEqual(torches.getCount(), 1, "one torch is placed");
      h.assertValueEqual(
          ElectricItem.manager.getCharge(stack), afterPath, "torch placement spends no EU");
    }
    player.getInventory().clearContent();
    var vajra = ElectricItemManager.getCharged(GraviItems.VAJRA.get(), CHARGE);
    player.setItemInHand(InteractionHand.MAIN_HAND, vajra);
    player.getInventory().setItem(9, new ItemStack(Items.TORCH, 2));
    h.setBlock(POS.above(), Blocks.AIR);
    h.setBlock(POS, Blocks.GRASS_BLOCK);
    h.assertTrue(use(h, player).consumesAction(), "Vajra keeps torch-only right-click behavior");
    h.assertBlockPresent(Blocks.GRASS_BLOCK, POS);
    h.assertBlockPresent(Blocks.TORCH, POS.above());
    h.assertValueEqual(
        ElectricItem.manager.getCharge(vajra), CHARGE, "Vajra gains no path energy cost");
    h.succeed();
  }
}
