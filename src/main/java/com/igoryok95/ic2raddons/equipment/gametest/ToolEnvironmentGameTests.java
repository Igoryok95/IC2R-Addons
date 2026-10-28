package com.igoryok95.ic2raddons.equipment.gametest;

import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.items.MiningTool;
import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.tool.ItemDrill;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class ToolEnvironmentGameTests {
  @GameTest(template = "gametest/empty3x3x3")
  public static void allMiningProfilesKeepTheirAdvantageUnderWaterAndInAir(GameTestHelper h) {
    var player = h.makeMockServerPlayerInLevel();
    player.setNoGravity(true);
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    player.teleportTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    var head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
    var stone = Blocks.STONE.defaultBlockState();
    var tools =
        List.of(
            GraviItems.ADVANCED_DRILL.get(),
            GraviItems.ADVANCED_IRIDIUM_DRILL.get(),
            GraviItems.VAJRA.get());
    float[][] profiles = {{48, 24, 16}, {72, 36, 24}, {128, 64, 32}};
    int checks = 0;
    for (boolean water : new boolean[] {false, true}) {
      for (boolean onGround : new boolean[] {true, false}) {
        for (boolean affinity : water ? new boolean[] {false, true} : new boolean[] {false}) {
          player
              .serverLevel()
              .setBlockAndUpdate(head, (water ? Blocks.WATER : Blocks.AIR).defaultBlockState());
          player.baseTick();
          player.setOnGround(onGround);
          player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).setBaseValue(affinity ? 1 : .2);
          h.assertTrue(
              player.isEyeInFluid(FluidTags.WATER) == water, "native eye-fluid fixture is wrong");
          float compensation = (water && !affinity ? 3 : 1) * (onGround ? 1 : 3);
          float penalty = (water && !affinity ? .2F : 1) * (onGround ? 1 : .2F);
          for (var nativeTool : List.of(Ic2Items.DIAMOND_DRILL, Ic2Items.IRIDIUM_DRILL)) {
            var nativeStack = ElectricItemManager.getCharged(nativeTool, Double.POSITIVE_INFINITY);
            player.setItemInHand(InteractionHand.MAIN_HAND, nativeStack);
            float base = nativeTool == Ic2Items.DIAMOND_DRILL ? 16 : 24;
            near(
                h,
                player.getDigSpeed(stone, pos),
                base * compensation * penalty,
                "native drill baseline changed");
          }
          for (int t = 0; t < tools.size(); t++) {
            MiningTool tool = tools.get(t);
            for (int mode = 0; mode < 3; mode++) {
              var stack = ElectricItemManager.getCharged(tool, Double.POSITIVE_INFINITY);
              final int efficiency = mode;
              ItemState.update(stack, tag -> tag.putInt("efficiency", efficiency));
              player.setItemInHand(InteractionHand.MAIN_HAND, stack);
              double charge = ElectricItem.manager.getCharge(stack);
              near(
                  h,
                  ItemDrill.getEnvironmentSpeedMultiplier(stack),
                  compensation,
                  "shared compensation");
              near(
                  h,
                  tool.getDestroySpeed(stack, stone),
                  profiles[t][mode] * compensation,
                  "tool profile under environmental compensation");
              near(
                  h,
                  player.getDigSpeed(stone, pos),
                  profiles[t][mode] * compensation * penalty,
                  "actual player mining speed");
              h.assertValueEqual(
                  ElectricItem.manager.getCharge(stack),
                  charge,
                  "speed inspection must not spend EU");
              checks++;
            }
          }
        }
      }
    }
    for (var tool : tools) {
      player.setOnGround(false);
      var empty = new ItemStack(tool);
      player.setItemInHand(InteractionHand.MAIN_HAND, empty);
      near(
          h, tool.getDestroySpeed(empty, stone), .1F, "empty tool received an environmental bonus");
    }
    player.serverLevel().setBlockAndUpdate(head, Blocks.AIR.defaultBlockState());
    org.slf4j.LoggerFactory.getLogger("IC2R-Tool-Environment")
        .info(
            "IC2R_ADDONS_ENVIRONMENT_PASS: {} profile/environment combinations, native baselines, affinity and empty tools",
            checks);
    h.succeed();
  }

  private static void near(GameTestHelper h, float actual, float expected, String context) {
    h.assertTrue(
        Math.abs(actual - expected) < .001F,
        context + ": expected=" + expected + ", actual=" + actual);
  }
}
