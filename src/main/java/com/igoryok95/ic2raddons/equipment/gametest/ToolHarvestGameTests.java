package com.igoryok95.ic2raddons.equipment.gametest;

import com.igoryok95.ic2raddons.bridge.TestPlayers;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.Mining;
import com.igoryok95.ic2raddons.equipment.items.MiningTool;
import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Native server harvesting, rather than directly invoking obsolete item callbacks. */
@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class ToolHarvestGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void everyMiningAndEfficiencyModeHarvestsAndPaysPerBlock(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var center = h.absolutePos(new BlockPos(1, 1, 1));
    aim(player, center, Direction.NORTH);
    for (var tool :
        List.of(
            GraviItems.ADVANCED_DRILL.get(),
            GraviItems.ADVANCED_IRIDIUM_DRILL.get(),
            GraviItems.VAJRA.get())) {
      float previousSpeed = Float.POSITIVE_INFINITY;
      int previousCost = Integer.MAX_VALUE;
      for (int efficiency = 0; efficiency < 3; efficiency++) {
        for (int mode = 0; mode < (tool.vajra ? 3 : 2); mode++) {
          var stack = ElectricItemManager.getCharged(tool, 40000);
          int eff = efficiency, mining = mode;
          ItemState.update(
              stack,
              tag -> {
                tag.putInt("efficiency", eff);
                tag.putInt("mining", mining);
              });
          player.setItemSlot(EquipmentSlot.MAINHAND, stack);
          var state =
              (tool.vajra && mode == 1 ? Blocks.IRON_ORE : Blocks.DIORITE).defaultBlockState();
          for (var pos : Mining.plane(center, Direction.NORTH))
            player.serverLevel().setBlockAndUpdate(pos, state);
          player.serverLevel().setBlockAndUpdate(center, state);
          double before = ElectricItem.manager.getCharge(stack);
          h.assertTrue(player.gameMode.destroyBlock(center), "native center harvest must succeed");
          int removed = 1;
          for (var pos : Mining.plane(center, Direction.NORTH))
            if (player.level().getBlockState(pos).isAir()) removed++;
          int expected = mode == 0 ? 1 : 9;
          h.assertTrue(
              removed == expected,
              "native mode " + mode + " removed " + removed + " blocks for " + tool);
          h.assertTrue(
              before - ElectricItem.manager.getCharge(stack) == expected * tool.cost(stack),
              "exact EU for "
                  + tool
                  + " efficiency="
                  + efficiency
                  + " mining="
                  + mode
                  + ": expected="
                  + expected * tool.cost(stack)
                  + " actual="
                  + (before - ElectricItem.manager.getCharge(stack)));
          float speed = tool.getDestroySpeed(stack, state);
          if (mode == 0) {
            h.assertTrue(
                speed < previousSpeed && tool.cost(stack) < previousCost,
                "each lower-power mode must reduce both speed and EU");
            previousSpeed = speed;
            previousCost = tool.cost(stack);
          }
        }
      }
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void drillPlanesFollowAllSixFaces(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var center = h.absolutePos(new BlockPos(1, 1, 1));
    for (var face : Direction.values()) {
      var stack = ElectricItemManager.getCharged(GraviItems.ADVANCED_DRILL.get(), 10000);
      ItemState.update(stack, tag -> tag.putInt("mining", 1));
      player.setItemSlot(EquipmentSlot.MAINHAND, stack);
      aim(player, center, face);
      for (var pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1)))
        player.serverLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
      h.assertTrue(player.gameMode.destroyBlock(center), "center harvest for " + face);
      for (var pos : Mining.plane(center, face))
        h.assertTrue(
            player.level().getBlockState(pos).isAir(), "3x3 face " + face + " must include " + pos);
      h.assertTrue(
          player.level().getBlockState(center.relative(face)).is(Blocks.STONE),
          "do not drill a second depth layer");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void lowChargeAndSneakLimitAreaWithoutFreeBlocks(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var center = h.absolutePos(new BlockPos(1, 1, 1));
    aim(player, center, Direction.NORTH);
    for (boolean sneak : List.of(false, true)) {
      var stack = ElectricItemManager.getCharged(GraviItems.ADVANCED_DRILL.get(), 480);
      ItemState.update(stack, tag -> tag.putInt("mining", 1));
      player.setItemSlot(EquipmentSlot.MAINHAND, stack);
      player.setShiftKeyDown(sneak);
      player.serverLevel().setBlockAndUpdate(center, Blocks.STONE.defaultBlockState());
      for (var pos : Mining.plane(center, Direction.NORTH))
        player.serverLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
      h.assertTrue(player.gameMode.destroyBlock(center), "charged native harvest");
      int removed = 1;
      for (var pos : Mining.plane(center, Direction.NORTH))
        if (player.level().getBlockState(pos).isAir()) removed++;
      h.assertTrue(removed == (sneak ? 1 : 3), "sneak or EU limit must stop extra mining");
      h.assertTrue(
          ElectricItem.manager.getCharge(stack) == 480 - removed * 160,
          "no free blocks or double charges");
    }
    player.setShiftKeyDown(false);
    var empty = new ItemStack(GraviItems.ADVANCED_DRILL.get());
    player.setItemSlot(EquipmentSlot.MAINHAND, empty);
    player.serverLevel().setBlockAndUpdate(center, Blocks.STONE.defaultBlockState());
    h.assertTrue(
        !player.gameMode.destroyBlock(center), "empty tool must not harvest even its center");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void treeFellingRunsThroughNativeHarvestAndHonorsSneak(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var center = h.absolutePos(new BlockPos(1, 0, 1));
    for (int mode = 0; mode < 3; mode++) {
      var stack = ElectricItemManager.getCharged(GraviItems.ADVANCED_CHAINSAW.get(), 5000);
      boolean enabled = mode != 0;
      ItemState.update(stack, tag -> tag.putBoolean("tree", enabled));
      player.setShiftKeyDown(mode == 2);
      player.setItemSlot(EquipmentSlot.MAINHAND, stack);
      for (int y = 0; y < 3; y++)
        player.serverLevel().setBlockAndUpdate(center.above(y), Blocks.OAK_LOG.defaultBlockState());
      h.assertTrue(player.gameMode.destroyBlock(center), "native log harvest");
      int expected = mode == 1 ? 3 : 1;
      h.assertTrue(
          player.level().getBlockState(center.above()).isAir() == (expected == 3),
          "tree mode and sneak must affect connected logs");
      h.assertTrue(
          ElectricItem.manager.getCharge(stack) == 5000 - expected * 100,
          "tree felling charges once per log");
    }
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void enchantmentModesProduceTheirRealDrops(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var center = h.absolutePos(new BlockPos(1, 1, 1));
    for (MiningTool item :
        List.of(GraviItems.ADVANCED_IRIDIUM_DRILL.get(), GraviItems.VAJRA.get())) {
      for (int mode = 0; mode < 2; mode++) {
        player
            .serverLevel()
            .getEntitiesOfClass(
                ItemEntity.class, new net.minecraft.world.phys.AABB(center).inflate(3))
            .forEach(ItemEntity::discard);
        var stack = ElectricItemManager.getCharged(item, 40000);
        int enchantment = mode;
        ItemState.update(stack, tag -> tag.putInt("enchantment", enchantment));
        player.setItemSlot(EquipmentSlot.MAINHAND, stack);
        item.inventoryTick(stack, player.level(), player, 0, true);
        player.serverLevel().setBlockAndUpdate(center, Blocks.STONE.defaultBlockState());
        h.assertTrue(player.gameMode.destroyBlock(center), "native enchanted harvest");
        var drops =
            player
                .serverLevel()
                .getEntitiesOfClass(
                    ItemEntity.class, new net.minecraft.world.phys.AABB(center).inflate(2));
        h.assertTrue(
            drops.stream()
                .anyMatch(
                    entity ->
                        entity.getItem().is(enchantment == 1 ? Items.STONE : Items.COBBLESTONE)),
            "silk and fortune must produce the appropriate real block drops");
      }
    }
    h.succeed();
  }

  private static void aim(ServerPlayer player, BlockPos center, Direction face) {
    var eye =
        net.minecraft.world.phys.Vec3.atCenterOf(center)
            .add(net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(2.5));
    player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
    player.setYRot(
        switch (face) {
          case NORTH -> 0;
          case SOUTH -> 180;
          case WEST -> -90;
          case EAST -> 90;
          default -> 0;
        });
    player.setXRot(face == Direction.UP ? 90 : face == Direction.DOWN ? -90 : 0);
    player.setOnGround(true);
  }

  @GameTest(template = EMPTY)
  public static void gravitoolModesPerformTheirRealActionsAndChargeOnce(GameTestHelper h) {
    var player = TestPlayers.create(h);
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var item = GraviItems.GRAVITOOL.get();
    var stack = ElectricItemManager.getCharged(item, 1000);
    player.setItemSlot(EquipmentSlot.MAINHAND, stack);
    player.serverLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
    ItemState.update(stack, tag -> tag.putInt("tool", 2));
    player.serverLevel().setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
    item.useOn(context(player, pos, Direction.UP));
    h.assertTrue(
        player.level().getBlockState(pos).is(Blocks.FARMLAND), "hoe mode must till the block");
    h.assertTrue(ElectricItem.manager.getCharge(stack) == 950, "hoe uses native 50 EU");

    ItemState.update(stack, tag -> tag.putInt("tool", 1));
    var wet =
        java.util.Arrays.stream(ic2.core.block.misc.RubberLogBlock.RubberWoodState.values())
            .filter(state -> state.wet && state.facing == Direction.NORTH)
            .findFirst()
            .orElseThrow();
    player
        .serverLevel()
        .setBlockAndUpdate(
            pos,
            ic2.core.ref.Ic2Blocks.RUBBER_LOG
                .defaultBlockState()
                .setValue(ic2.core.block.misc.RubberLogBlock.stateProperty, wet));
    item.useOn(context(player, pos, Direction.NORTH));
    h.assertTrue(
        !player
            .level()
            .getBlockState(pos)
            .getValue(ic2.core.block.misc.RubberLogBlock.stateProperty)
            .wet,
        "treetap mode must extract real resin");
    h.assertTrue(ElectricItem.manager.getCharge(stack) == 900, "treetap uses native 50 EU");

    ItemState.update(stack, tag -> tag.putInt("tool", 3));
    var original = Blocks.PISTON.defaultBlockState();
    player.serverLevel().setBlockAndUpdate(pos, original);
    item.useOn(context(player, pos, Direction.UP));
    h.assertTrue(
        player.level().getBlockState(pos) != original,
        "screwdriver must rotate a directional block");
    h.assertTrue(ElectricItem.manager.getCharge(stack) == 750, "screwdriver uses 150 EU once");

    ItemState.update(stack, tag -> tag.putInt("tool", 0));
    var machine =
        (ic2.core.block.tileentity.Ic2TileEntityBlock) ic2.core.ref.Ic2Blocks.ELECTRIC_FURNACE;
    player
        .serverLevel()
        .setBlockAndUpdate(
            pos,
            machine
                .defaultBlockState()
                .setValue(machine.facingProperty, Direction.NORTH)
                .setValue(ic2.core.block.tileentity.Ic2TileEntityBlock.ACTIVE, false));
    item.onItemUseFirst(stack, context(player, pos, Direction.EAST));
    h.assertTrue(
        player.level().getBlockState(pos).getValue(machine.facingProperty) == Direction.EAST,
        "wrench mode must use IC2R facing logic");
    h.succeed();
  }

  private static net.minecraft.world.item.context.UseOnContext context(
      ServerPlayer player, BlockPos pos, Direction face) {
    return new net.minecraft.world.item.context.UseOnContext(
        player,
        net.minecraft.world.InteractionHand.MAIN_HAND,
        new net.minecraft.world.phys.BlockHitResult(
            net.minecraft.world.phys.Vec3.atCenterOf(pos)
                .add(net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(.5)),
            face,
            pos,
            false));
  }
}
