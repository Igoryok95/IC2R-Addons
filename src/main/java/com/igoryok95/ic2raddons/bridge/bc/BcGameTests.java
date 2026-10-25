package com.igoryok95.ic2raddons.bridge.bc;

import buildcraft.api.v2.*;
import buildcraft.api.v2.energy.*;
import com.igoryok95.ic2raddons.bridge.BridgeConfig;
import com.igoryok95.ic2raddons.solar.*;
import ic2.core.block.comp.Energy;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@PrefixGameTestTemplate(false)
public final class BcGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static boolean bc() {
    return ModList.get().isLoaded("buildcraftenergy");
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void bcWrenchDismantlesIc2MachineOnceAndRespectsCancellation(GameTestHelper h) {
    var relative = new BlockPos(1, 1, 1);
    h.setBlock(relative, ic2.core.ref.Ic2Blocks.MACERATOR);
    var pos = h.absolutePos(relative);
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("buildcraftcore:wrench"))));
    var blocked =
        new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(
            h.getLevel(), pos, h.getLevel().getBlockState(pos), player);
    blocked.setCanceled(true);
    WrenchBridge.breakBlock(blocked);
    h.assertTrue(
        !h.getLevel().getBlockState(pos).isAir(),
        "Cancelled dismantling must leave machine intact");
    var allowed =
        new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(
            h.getLevel(), pos, h.getLevel().getBlockState(pos), player);
    WrenchBridge.breakBlock(allowed);
    h.assertTrue(
        allowed.isCanceled() && h.getLevel().getBlockState(pos).isAir(),
        "Wrench removal must replace vanilla breaking");
    var drops =
        h.getLevel()
            .getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, new AABB(pos).inflate(1));
    int machines =
        drops.stream()
            .filter(e -> e.getItem().is(ic2.core.ref.Ic2Items.MACERATOR))
            .mapToInt(e -> e.getItem().getCount())
            .sum();
    h.assertTrue(
        machines == 1, "Exactly one whole machine must drop, without duplicate vanilla loot");
    h.succeed();
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 160)
  public static void engineFeedsQuarryThroughWoodenKinesisPipe(GameTestHelper h) {
    var e = engine(h);
    var pipePos = e.getBlockPos().relative(e.getFacing());
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    h.getLevel().setBlockAndUpdate(pipePos.below(), Blocks.STONE.defaultBlockState());
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(buildcraft.transport.BCTransportItems.PIPE_POWER_WOOD.get()));
    var placement =
        new net.minecraft.world.item.context.UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(
                Vec3.atCenterOf(pipePos.below()).add(0, 0.5, 0),
                Direction.UP,
                pipePos.below(),
                false));
    h.assertTrue(
        player.getMainHandItem().useOn(placement).consumesAction(),
        "Real wooden Kinesis pipe must place");
    var quarryPos = pipePos.relative(e.getFacing());
    var quarry = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("buildcraftbuilders:quarry"));
    h.getLevel().setBlockAndUpdate(quarryPos, quarry.defaultBlockState());
    ((buildcraft.builders.tile.TileQuarry) h.getLevel().getBlockEntity(quarryPos))
        .onPlacedBy(player, new ItemStack(quarry));
    h.succeedWhen(
        () -> {
          var target =
              BuildCraftApi.service(BuildCraftServices.ENERGY)
                  .port(h.getLevel(), quarryPos, e.getFacing().getOpposite())
                  .orElseThrow();
          h.assertTrue(
              target.stored().microMj() > 0 && e.energy.getEnergy() < 1000,
              "MJ must reach quarry through actual Kinesis pipe");
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void ic2WrenchRotatesBuildCraftAndRejectsEmptyElectricTool(GameTestHelper h) {
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    h.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(buildcraft.energy.BCEnergyBlocks.ENGINE_FE_ITEM.get()));
    player
        .getMainHandItem()
        .useOn(
            new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    Vec3.atCenterOf(pos.below()).add(0, 0.5, 0),
                    Direction.UP,
                    pos.below(),
                    false)));
    var bcEngine = (buildcraft.lib.engine.TileEngineBase_BC8) h.getLevel().getBlockEntity(pos);
    var quarry = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("buildcraftbuilders:quarry"));
    for (Direction side : new Direction[] {Direction.NORTH, Direction.EAST}) {
      h.getLevel().setBlockAndUpdate(pos.relative(side), quarry.defaultBlockState());
      ((buildcraft.builders.tile.TileQuarry) h.getLevel().getBlockEntity(pos.relative(side)))
          .onPlacedBy(player, new ItemStack(quarry));
    }
    var electric = ic2.core.item.ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, 1000);
    player.setItemInHand(InteractionHand.MAIN_HAND, electric);
    h.assertTrue(
        BuildCraftApi.service(BuildCraftServices.WRENCHES).isWrench(electric),
        "BC API must recognize IC2 wrench");
    var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.5, 0, 0), Direction.EAST, pos, false);
    var before = bcEngine.getCurrentFacing();
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
        new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
    h.assertTrue(bcEngine.getCurrentFacing() != before, "IC2 wrench must rotate a real BC engine");
    h.assertTrue(
        ic2.api.item.ElectricItem.manager.getCharge(electric) == 900,
        "Successful rotation costs 100 EU");
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.ELECTRIC_WRENCH));
    before = bcEngine.getCurrentFacing();
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
        new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
    h.assertTrue(bcEngine.getCurrentFacing() == before, "Empty wrench cannot rotate BC machines");
    h.succeed();
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 100)
  public static void ic2WrenchesSwitchIronPipeOutputThroughBlockActivation(GameTestHelper h) {
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    for (var pipePos : java.util.List.of(pos.west(), pos, pos.east())) {
      h.getLevel().setBlockAndUpdate(pipePos.below(), Blocks.STONE.defaultBlockState());
      player.setItemInHand(
          InteractionHand.MAIN_HAND,
          new ItemStack(
              BuiltInRegistries.ITEM.get(
                  ResourceLocation.parse(
                      pipePos.equals(pos)
                          ? "buildcrafttransport:iron_item"
                          : "buildcrafttransport:stone_item"))));
      h.assertTrue(
          player
              .getMainHandItem()
              .useOn(
                  new net.minecraft.world.item.context.UseOnContext(
                      player,
                      InteractionHand.MAIN_HAND,
                      new BlockHitResult(
                          Vec3.atCenterOf(pipePos.below()).add(0, .5, 0),
                          Direction.UP,
                          pipePos.below(),
                          false)))
              .consumesAction(),
          "real BC transport pipe places");
    }
    h.runAfterDelay(
        10,
        () -> {
          var holder = (buildcraft.transport.tile.TilePipeHolder) h.getLevel().getBlockEntity(pos);
          var behaviour =
              (buildcraft.transport.pipe.behaviour.PipeBehaviourDirectional)
                  holder.getPipe().getBehaviour();
          var hit =
              new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0, -.25), Direction.NORTH, pos, false);
          player.setPos(pos.getX() + .5, pos.getY() - 1, pos.getZ() - 2);
          player.setYRot(0);
          player.setXRot(0);
          for (var tool :
              java.util.List.of(
                  new ItemStack(Ic2Items.WRENCH),
                  ic2.core.item.ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, 1000))) {
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            var before = behaviour.getCurrentDir();
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                new PlayerInteractEvent.RightClickBlock(
                    player, InteractionHand.MAIN_HAND, pos, hit));
            h.assertTrue(
                behaviour.getCurrentDir() != before,
                "IC2 manual and electric wrenches switch a real iron item pipe");
            if (tool.is(Ic2Items.ELECTRIC_WRENCH))
              h.assertTrue(
                  ic2.api.item.ElectricItem.manager.getCharge(tool) == 900,
                  "pipe action charges an electric wrench exactly once");
          }
          var before = behaviour.getCurrentDir();
          player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.ELECTRIC_WRENCH));
          net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
              new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
          h.assertTrue(
              behaviour.getCurrentDir() == before, "empty electric wrench cannot switch pipe");
          h.succeed();
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 120)
  public static void placedEngineFacesExistingWoodenMjPipeAndOpensRealMenu(GameTestHelper h) {
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var pipePos = pos.east();
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    h.getLevel().setBlockAndUpdate(pipePos.below(), Blocks.STONE.defaultBlockState());
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(buildcraft.transport.BCTransportItems.PIPE_POWER_WOOD.get()));
    player
        .getMainHandItem()
        .useOn(
            new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    Vec3.atCenterOf(pipePos.below()).add(0, .5, 0),
                    Direction.UP,
                    pipePos.below(),
                    false)));
    h.getLevel().setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BcContent.ENGINE_ITEM.get()));
    h.assertTrue(
        player
            .getMainHandItem()
            .useOn(
                new net.minecraft.world.item.context.UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(
                        Vec3.atCenterOf(pos.below()).add(0, .5, 0),
                        Direction.UP,
                        pos.below(),
                        false)))
            .consumesAction(),
        "engine item places");
    var engine = (ElectricEngine) h.getLevel().getBlockEntity(pos);
    var menu = engine.createServerScreenHandler(1, player);
    h.assertTrue(
        menu.slots.size() == 41 && menu.base == engine,
        "real engine GUI has battery and complete player inventory");
    for (var slot : menu.slots) {
      if (slot.container != player.getInventory()) continue;
      int index = slot.getContainerSlot();
      int expectedX = 8 + index % 9 * 18;
      int expectedY = index < 9 ? 142 : 84 + (index - 9) / 9 * 18;
      h.assertTrue(
          slot.x == expectedX && slot.y == expectedY,
          "clickable inventory slots align with the original Forestry background: " + index);
    }
    engine.dischargeSlot.put(
        ic2.core.item.ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, 1000));
    var quarryPos = pipePos.east();
    var quarry = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("buildcraftbuilders:quarry"));
    h.getLevel().setBlockAndUpdate(quarryPos, quarry.defaultBlockState());
    ((buildcraft.builders.tile.TileQuarry) h.getLevel().getBlockEntity(quarryPos))
        .onPlacedBy(player, new ItemStack(quarry));
    h.succeedWhen(
        () -> {
          var target =
              BuildCraftApi.service(BuildCraftServices.ENERGY)
                  .port(h.getLevel(), quarryPos, Direction.WEST)
                  .orElseThrow();
          h.assertTrue(
              engine.getFacing() == Direction.EAST,
              "engine automatically faces existing wooden MJ pipe");
          var holder =
              (buildcraft.transport.tile.TilePipeHolder) h.getLevel().getBlockEntity(pipePos);
          h.assertTrue(
              holder.getPipe().isConnected(Direction.WEST),
              "wooden pipe displays its actual engine connection");
          h.assertTrue(
              target.stored().microMj() > 0,
              "battery-powered engine feeds quarry through existing wooden MJ pipe");
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void forestryHeatAndIc2UpgradesKeepTheAgreedBalance(GameTestHelper h) {
    var e = engine(h);
    e.updateEntityServer();
    h.assertTrue(
        e.energy.getEnergy() == 995
            && e.mjPort(e.getFacing()).orElseThrow().stored().microMj() == 2_000_000,
        "base conversion is five EU for two MJ per tick");
    h.assertTrue(e.heat == 1 && e.getHeatStage() == 0, "cold start stays blue and warms gradually");
    e.upgradeSlot.put(0, new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE, 4));
    e.upgradeSlot.put(1, new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE));
    e.upgradeSlot.put(2, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE));
    double before = e.energy.getEnergy();
    long mjBefore = e.mjPort(e.getFacing()).orElseThrow().stored().microMj();
    e.updateEntityServer();
    h.assertTrue(
        e.energy.getCapacity() > 10000 && e.telemetry().getInt("voltage") == 512,
        "storage and transformer upgrades affect actual EU properties");
    h.assertTrue(
        e.mjPort(e.getFacing()).orElseThrow().stored().microMj() - mjBefore == 8_000_000
            && before - e.energy.getEnergy() >= 20,
        "overclockers cap generation at eight MJ with worse EU efficiency");
    var port = e.mjPort(e.getFacing()).orElseThrow();
    h.assertTrue(
        port.extract(MjAmount.ofMj(100), OperationMode.SIMULATE).transferred().microMj()
            == 8_000_000,
        "four overclockers allow eight MJ delivery");
    e.upgradeSlot.put(0, ItemStack.EMPTY);
    e.updateEntityServer();
    h.assertTrue(
        port.extract(MjAmount.ofMj(100), OperationMode.SIMULATE).transferred().microMj()
            == 2_000_000,
        "removing overclockers restores two MJ delivery even with stored MJ");
    h.succeed();
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 100)
  public static void manualEngineDirectionSurvivesTicksNeighborsAndReload(GameTestHelper h) {
    var level = h.getLevel();
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var pipePos = pos.east();
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    level.setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    level.setBlockAndUpdate(pipePos.below(), Blocks.STONE.defaultBlockState());
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(buildcraft.transport.BCTransportItems.PIPE_POWER_WOOD.get()));
    player
        .getMainHandItem()
        .useOn(
            new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    Vec3.atCenterOf(pipePos.below()).add(0, .5, 0),
                    Direction.UP,
                    pipePos.below(),
                    false)));
    var placement =
        new BlockHitResult(
            Vec3.atCenterOf(pos.below()).add(0, .5, 0), Direction.UP, pos.below(), false);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BcContent.ENGINE_ITEM.get()));
    player
        .getMainHandItem()
        .useOn(
            new net.minecraft.world.item.context.UseOnContext(
                player, InteractionHand.MAIN_HAND, placement));
    var engine = (ElectricEngine) level.getBlockEntity(pos);
    var oldOutput = new buildcraft.lib.internal.mj.IMjPassiveProvider[1];
    h.runAfterDelay(
        3,
        () -> {
          h.assertTrue(
              engine.getFacing() == Direction.EAST,
              "placement finds the existing wooden pipe after its ports load");
          oldOutput[0] = engine.legacyOutput(Direction.EAST);
        });
    var tools =
        java.util.List.of(
            new ItemStack(Ic2Items.WRENCH),
            new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("buildcraftcore:wrench"))),
            ic2.core.item.ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, 1000));
    Direction[] choices = {Direction.NORTH, Direction.SOUTH, Direction.UP};
    for (int i = 0; i < tools.size(); i++) {
      int index = i;
      h.runAfterDelay(
          5 + i * 10,
          () -> {
            var tool = tools.get(index);
            var chosen = choices[index];
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            var hit =
                new BlockHitResult(Vec3.atCenterOf(pos).relative(chosen, .5), chosen, pos, false);
            h.assertTrue(
                player
                    .gameMode
                    .useItemOn(player, level, tool, InteractionHand.MAIN_HAND, hit)
                    .consumesAction(),
                "real wrench interaction succeeds: " + tool);
            h.assertTrue(engine.getFacing() == chosen, "wrench changes output direction");
            level.neighborChanged(pos, Blocks.STONE, pipePos);
            h.runAfterDelay(
                6,
                () -> {
                  h.assertTrue(
                      engine.getFacing() == chosen,
                      "ticks and neighbor updates must not undo manual rotation");
                  h.assertTrue(
                      engine.mjPort(Direction.EAST).isEmpty()
                          && oldOutput[0].extractPower(0, 1_000_000, false) == 0,
                      "old pipe face and cached passive provider no longer export MJ");
                  var pipe =
                      (buildcraft.transport.tile.TilePipeHolder) level.getBlockEntity(pipePos);
                  h.assertTrue(
                      !pipe.getPipe().isConnected(Direction.WEST),
                      "wooden pipe disconnects after output rotates away");
                });
          });
    }
    h.runAfterDelay(
        38,
        () -> {
          var saved = engine.saveWithFullMetadata(level.registryAccess());
          h.assertTrue(saved.getBoolean("manualDirection"), "manual selection is persisted");
          var restored =
              (ElectricEngine)
                  BlockEntity.loadStatic(
                      pos, engine.getBlockState(), saved, level.registryAccess());
          level.removeBlockEntity(pos);
          level.setBlockEntity(restored);
          level.neighborChanged(pos, Blocks.STONE, pipePos);
          h.runAfterDelay(
              10,
              () -> {
                h.assertTrue(
                    restored.getFacing() == Direction.UP,
                    "reloading must preserve the manually selected direction");
                h.assertTrue(
                    restored
                        .saveWithFullMetadata(level.registryAccess())
                        .getBoolean("manualDirection"),
                    "manual mode survives a loaded engine ticking");
                level.removeBlock(pos, false);
                player.setItemInHand(
                    InteractionHand.MAIN_HAND, new ItemStack(BcContent.ENGINE_ITEM.get()));
                player
                    .getMainHandItem()
                    .useOn(
                        new net.minecraft.world.item.context.UseOnContext(
                            player, InteractionHand.MAIN_HAND, placement));
                var replacement = (ElectricEngine) level.getBlockEntity(pos);
                h.runAfterDelay(
                    8,
                    () -> {
                      h.assertTrue(
                          replacement.getFacing() == Direction.EAST
                              && !replacement
                                  .saveWithFullMetadata(level.registryAccess())
                                  .getBoolean("manualDirection"),
                          "placing the block again resets manual mode and finds the pipe");
                      h.succeed();
                    });
              });
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 80)
  public static void shutdownFinishesPistonStrokeAndCoolsGradually(GameTestHelper h) {
    var e = engine(h);
    e.heat = 900;
    e.pistonMoving = true;
    e.pistonPhase = .2f;
    h.setBlock(new BlockPos(1, 0, 1), Blocks.AIR);
    e.updateEntityServer();
    h.assertTrue(
        e.heat == 899 && e.pistonMoving && e.pistonPhase > .2f,
        "switch-off neither snaps piston to zero nor resets heat");
    var saved = e.saveWithFullMetadata(h.getLevel().registryAccess());
    var copy =
        (ElectricEngine)
            BlockEntity.loadStatic(
                e.getBlockPos(), e.getBlockState(), saved, h.getLevel().registryAccess());
    h.assertTrue(
        copy != null && copy.heat == 899 && copy.pistonMoving,
        "thermal and moving state survive saving");
    h.runAfterDelay(
        40,
        () -> {
          h.assertTrue(
              !e.pistonMoving && e.pistonPhase == 0 && e.heat > 600 && e.getHeatStage() == 1,
              "last stroke reaches rest while hot engine retains its warm color");
          h.succeed();
        });
  }

  private static ElectricEngine engine(GameTestHelper h) {
    h.setBlock(new BlockPos(1, 0, 1), Blocks.REDSTONE_BLOCK);
    h.setBlock(new BlockPos(1, 1, 1), BcContent.ENGINE.get());
    ElectricEngine e = h.getBlockEntity(new BlockPos(1, 1, 1));
    ((ic2.api.tile.IWrenchAble) BcContent.ENGINE.get())
        .setFacing(
            h.getLevel(),
            e.getBlockPos(),
            Direction.NORTH,
            com.igoryok95.ic2raddons.bridge.TestPlayers.create(h));
    e.energy.addEnergy(1000);
    return e;
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void engineConservesEnergyAcrossSimulationRateLimitsAndSaving(GameTestHelper h) {
    if (!bc()) {
      h.succeed();
      return;
    }
    ElectricEngine e = engine(h);
    var saved = e.saveWithFullMetadata(h.getLevel().registryAccess());
    saved.putLong("mjBuffer", 50_000_000L);
    e.loadWithComponents(saved, h.getLevel().registryAccess());
    var port = e.mjPort(e.getFacing()).orElseThrow();
    var legacy = e.legacyOutput(e.getFacing());
    var amount = MjAmount.ofMj(100);
    double before = e.energy.getEnergy();
    h.assertTrue(
        port.extract(amount, OperationMode.SIMULATE).transferred().microMj() == 2_000_000,
        "API simulation respects the two MJ base delivery budget");
    h.assertTrue(
        legacy.extractPower(0, 100_000_000, false) == 2_000_000
            && e.energy.getEnergy() == before
            && port.stored().microMj() == 50_000_000,
        "legacy false is simulation and neither path spends energy");
    long sent = legacy.extractPower(0, 100_000_000, true);
    h.assertTrue(
        sent == 2_000_000
            && port.stored().microMj() == 48_000_000
            && e.energy.getEnergy() == before,
        "MJ export drains its buffer without charging EU twice");
    h.assertTrue(
        port.extract(amount, OperationMode.EXECUTE).transferred().isZero(),
        "mixed legacy and API paths share one per-tick budget");
    h.assertTrue(e.mjPort(e.getFacing().getOpposite()).isEmpty(), "only piston face exports MJ");
    saved = e.saveWithFullMetadata(h.getLevel().registryAccess());
    var loaded =
        (ElectricEngine)
            BlockEntity.loadStatic(
                e.getBlockPos(), e.getBlockState(), saved, h.getLevel().registryAccess());
    h.assertTrue(
        loaded != null
            && loaded.energy.getEnergy() == before
            && loaded.mjPort(loaded.getFacing()).orElseThrow().stored().microMj() == 48_000_000,
        "both EU and MJ buffers survive saving");
    h.succeed();
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 60)
  public static void engineWaitsWithoutRedstoneOrConsumer(GameTestHelper h) {
    if (!bc()) {
      h.succeed();
      return;
    }
    var e = engine(h);
    h.setBlock(new BlockPos(1, 0, 1), Blocks.AIR);
    h.runAfterDelay(
        5,
        () -> {
          h.assertTrue(
              e.energy.getEnergy() == 1000 && e.telemetry().getString("status").equals("redstone"),
              "No signal must preserve EU");
          h.setBlock(new BlockPos(1, 0, 1), Blocks.REDSTONE_BLOCK);
          h.runAfterDelay(
              5,
              () -> {
                double equivalent =
                    e.energy.getEnergy()
                        + e.mjPort(e.getFacing()).orElseThrow().stored().microMj()
                            / (BridgeConfig.MJ_PER_EU.get() * 1_000_000.0);
                h.assertTrue(
                    Math.abs(equivalent - 1000) < .000001
                        && e.telemetry().getString("status").equals("no_consumer"),
                    "unconnected engine preserves EU as stored MJ");
                h.succeed();
              });
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 120)
  public static void enginePowersActualBuildCraftQuarry(GameTestHelper h) {
    if (!bc()) {
      h.succeed();
      return;
    }
    var e = engine(h);
    BlockPos target = e.getBlockPos().relative(e.getFacing());
    h.getLevel()
        .setBlockAndUpdate(
            target,
            BuiltInRegistries.BLOCK
                .get(ResourceLocation.parse("buildcraftbuilders:quarry"))
                .defaultBlockState());
    ((buildcraft.builders.tile.TileQuarry) h.getLevel().getBlockEntity(target))
        .onPlacedBy(
            com.igoryok95.ic2raddons.bridge.TestPlayers.create(h),
            new ItemStack(h.getLevel().getBlockState(target).getBlock()));
    h.succeedWhen(
        () -> {
          h.assertTrue(
              e.energy.getEnergy() < 1000, "Actual BC quarry must accept MJ from EU engine");
          h.assertTrue(
              BuildCraftApi.service(BuildCraftServices.ENERGY)
                  .descriptor(h.getLevel(), e.getBlockPos(), e.getFacing())
                  .orElseThrow()
                  .has(MjPortRole.PROVIDER),
              "BC must discover the engine as a provider");
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY, timeoutTicks = 120)
  public static void realIc2NetworkChargesEngine(GameTestHelper h) {
    if (!bc()) {
      h.succeed();
      return;
    }
    var e = engine(h);
    e.energy.useEnergy(1000);
    h.setBlock(new BlockPos(2, 1, 1), AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get());
    com.igoryok95.ic2raddons.solar.te.TileEntitySolarPanel source =
        h.getBlockEntity(new BlockPos(2, 1, 1));
    source.getComponent(Energy.class).addEnergy(5000);
    boolean[] sawFlow = {false};
    h.onEachTick(
        () -> {
          if (sawFlow[0] || e.telemetry().getDouble("networkEuPerTick") <= 5) return;
          h.assertTrue(e.energy.getEnergy() > 0, "Engine joins the real IC2 EU network");
          h.assertTrue(
              e.telemetry().getDouble("euPerTick") == 5,
              "grid input and actual conversion consumption are independent readings");
          sawFlow[0] = true;
          h.setBlock(new BlockPos(2, 1, 1), Blocks.AIR);
          h.runAfterDelay(
              5,
              () -> {
                h.assertTrue(
                    e.telemetry().getDouble("networkEuPerTick") == 0
                        && e.telemetry().getDouble("euPerTick") == 5,
                    "disconnected engine consumes stored EU while grid input returns to zero");
                h.setBlock(new BlockPos(1, 0, 1), Blocks.AIR);
                h.runAfterDelay(
                    5,
                    () -> {
                      h.assertTrue(
                          e.telemetry().getDouble("networkEuPerTick") == 0
                              && e.telemetry().getDouble("euPerTick") == 0,
                          "idle readings are live values, not fixed localized constants");
                      h.succeed();
                    });
              });
        });
  }

  @GameTest(templateNamespace = "ic2r_addons", template = EMPTY)
  public static void buildCraftWrenchRotatesIc2Machine(GameTestHelper h) {
    if (!bc()) {
      h.succeed();
      return;
    }
    h.setBlock(new BlockPos(1, 1, 1), ic2.core.ref.Ic2Blocks.MACERATOR);
    var pos = h.absolutePos(new BlockPos(1, 1, 1));
    var player = com.igoryok95.ic2raddons.bridge.TestPlayers.create(h);
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("buildcraftcore:wrench"))));
    var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.5, 0, 0), Direction.EAST, pos, false);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
        new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
    h.assertTrue(
        ((ic2.api.tile.IWrenchAble) h.getBlockState(new BlockPos(1, 1, 1)).getBlock())
                .getFacing(h.getLevel(), pos)
            == Direction.EAST,
        "BC wrench must apply IC2 click-face rotation");
    h.succeed();
  }
}
