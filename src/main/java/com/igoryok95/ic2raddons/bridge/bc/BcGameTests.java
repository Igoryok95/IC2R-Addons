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
    var p = e.mjPort(e.getFacing()).orElseThrow();
    var amount = MjAmount.ofMj(100);
    double before = e.energy.getEnergy();
    h.assertTrue(
        p.extract(amount, OperationMode.SIMULATE).transferred().microMj() > 0,
        "Simulation should report available output");
    h.assertTrue(e.energy.getEnergy() == before, "Simulation cannot spend EU");
    long sent = p.extract(amount, OperationMode.EXECUTE).transferred().microMj();
    double expectedCost = sent / (BridgeConfig.MJ_PER_EU.get() * 1000000.0);
    h.assertTrue(
        Math.abs(before - e.energy.getEnergy() - expectedCost) < 0.000001,
        "Exact EU/MJ conservation");
    h.assertTrue(
        p.extract(amount, OperationMode.EXECUTE).transferred().isZero(),
        "Repeated extraction cannot bypass per-tick rate");
    h.assertTrue(
        e.mjPort(e.getFacing().getOpposite()).isEmpty(), "Only the piston face exports MJ");
    var saved = e.saveWithFullMetadata(h.getLevel().registryAccess());
    ElectricEngine loaded =
        (ElectricEngine)
            BlockEntity.loadStatic(
                e.getBlockPos(), e.getBlockState(), saved, h.getLevel().registryAccess());
    h.assertTrue(
        loaded != null && loaded.energy.getEnergy() == e.energy.getEnergy(),
        "EU buffer survives saving");
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
                h.assertTrue(
                    e.energy.getEnergy() == 1000
                        && e.telemetry().getString("status").equals("no_consumer"),
                    "Unconnected engine must not waste EU");
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
    h.succeedWhen(
        () -> h.assertTrue(e.energy.getEnergy() > 0, "Engine must join the real IC2 EU network"));
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
