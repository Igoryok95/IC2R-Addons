package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.machines.te.*;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Isolated development-world rendering/menu smoke; disabled in ordinary clients. */
@EventBusSubscriber(modid = IC2RAddons.MODID, value = Dist.CLIENT)
public final class MachineClientSmoke {
  private static final int MENU_TICKS = 140;
  private static int ticks;
  private static volatile java.util.List<BlockPos> positions;
  private static int menus;

  private static void screenshot(Minecraft mc, String name) {
    Screenshot.grab(
        mc.gameDirectory,
        name + "-" + mc.options.languageCode + ".png",
        mc.getMainRenderTarget(),
        message -> IC2RAddons.LOGGER.info("MACHINE_SMOKE_IMAGE {}", name));
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.machinesSmoke")) return;
    var mc = Minecraft.getInstance();
    mc.options.pauseOnLostFocus = false;
    if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
    if (ticks++ == 0) {
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                var base = player.blockPosition();
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                var area =
                    new net.minecraft.world.phys.AABB(
                        net.minecraft.world.phys.Vec3.atLowerCornerOf(base.offset(-7, -2, -3)),
                        net.minecraft.world.phys.Vec3.atLowerCornerOf(base.offset(8, 7, 10)));
                level
                    .getEntitiesOfClass(
                        net.minecraft.world.entity.decoration.ArmorStand.class, area)
                    .forEach(net.minecraft.world.entity.Entity::discard);
                level
                    .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area)
                    .forEach(net.minecraft.world.entity.Entity::discard);
                player.getInventory().clearContent();
                for (int x = -7; x <= 7; x++)
                  for (int z = -2; z <= 9; z++) {
                    level.setBlockAndUpdate(
                        base.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y <= 5; y++) {
                      var pos = base.offset(x, y, z);
                      if (level.getBlockEntity(pos)
                          instanceof net.minecraft.world.Container inventory)
                        inventory.clearContent();
                      level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                  }
                var placed = new java.util.ArrayList<BlockPos>();
                int i = 0;
                for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
                  var pos = base.offset(-3 + (i % 4) * 2, 0, 4 + (i / 4) * 2);
                  level.setBlockAndUpdate(pos, entry.get().defaultBlockState());
                  level.setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                  var machine = (TileEntityHeatingMachine) level.getBlockEntity(pos);
                  if (machine instanceof TileEntityHeatingWaterMachine water)
                    water.tank.fillMb(Ic2FluidStack.create(Fluids.WATER, 8000), false);
                  machine.getComponent(Energy.class).addEnergy(10000);
                  var nbt = machine.saveWithFullMetadata(level.registryAccess());
                  nbt.putShort("heat", (short) 9000);
                  machine.loadWithComponents(nbt, level.registryAccess());
                  placed.add(pos);
                  i++;
                }
                player.teleportTo(base.getX() + 0.5, base.getY() + 0.2, base.getZ() - 2.5);
                player.setYRot(0);
                player.setXRot(12);
                positions = java.util.List.copyOf(placed);
              });
    }
    if (positions == null) return;
    if (ticks == 60) {
      for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) {
        for (boolean active : new boolean[] {false, true}) {
          var state = entry.get().defaultBlockState().setValue(Ic2TileEntityBlock.ACTIVE, active);
          var model = mc.getBlockRenderer().getBlockModel(state);
          var random = RandomSource.create(0);
          for (var direction : Direction.values()) {
            var quads = model.getQuads(state, direction, random);
            if (quads.isEmpty())
              throw new IllegalStateException("Missing machine face: " + entry.getId());
            for (var quad : quads)
              if (quad.getSprite()
                  .contents()
                  .name()
                  .equals(
                      net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
                          .getLocation()))
                throw new IllegalStateException(
                    "Missing machine texture: " + entry.getId() + " " + direction + " " + active);
          }
        }
      }
      IC2RAddons.LOGGER.info("MACHINE_SMOKE_TEXTURES_PASS machines=8 states=16 faces=96");
    }
    if (ticks == 80) screenshot(mc, "advanced-machines-world");
    int step = ticks - 100;
    if (step >= 0 && step < 8 * MENU_TICKS) {
      int index = step / MENU_TICKS;
      if (step % MENU_TICKS == 0) {
        mc.setScreen(null);
        var id = mc.player.getUUID();
        mc.getSingleplayerServer()
            .execute(
                () -> {
                  var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                  var machine =
                      (TileEntityHeatingMachine)
                          player.serverLevel().getBlockEntity(positions.get(index));
                  var target = machine.getBlockPos();
                  MachineClientFixtures.prepare(machine);
                  player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() - 2.5);
                  if (!machine.openManagedBe(player, net.minecraft.world.InteractionHand.MAIN_HAND))
                    throw new IllegalStateException("Could not open machine menu " + index);
                });
      }
      if (step % MENU_TICKS == 24)
        com.igoryok95.ic2raddons.solar.gametest.SolarTiersClientSmoke.moveCursor(mc, 4, 4);
      if (step % MENU_TICKS == 25) {
        if (!(mc.screen instanceof ic2.core.gui.dynamic.DynamicGui gui))
          throw new IllegalStateException("Machine GUI did not reach client: " + index);
        if (gui.getTitle().getString().startsWith("container.")
            || mc.font.width(gui.getTitle()) > gui.getXSize() - 12)
          throw new IllegalStateException("Untranslated or oversized machine title: " + index);
        for (var slot : gui.getMenu().slots) {
          if (slot.x < 0
              || slot.y < 0
              || slot.x + 16 > gui.getXSize()
              || slot.y + 16 > gui.getYSize())
            throw new IllegalStateException("Slot outside machine screen: " + index);
        }
        screenshot(mc, "advanced-machines-gui-" + index);
        var machine = (TileEntityHeatingMachine) mc.level.getBlockEntity(positions.get(index));
        if (!machine.getWarmupText().endsWith("%")
            || mc.font.width(machine.getWarmupText()) > 40
            || machine.getWarmupPercent() < 0
            || machine.getWarmupPercent() > 100)
          throw new IllegalStateException("Invalid or oversized machine warm-up: " + index);
        if (machine.progress <= 0 || machine.progress >= machine.getMaxProgress())
          throw new IllegalStateException(
              "Expected visible partial processing progress for machine "
                  + index
                  + ": "
                  + machine.progress);
        IC2RAddons.LOGGER.info(
            "MACHINE_SMOKE_PROCESSING index={} progress={} maximum={}",
            index,
            machine.progress,
            machine.getMaxProgress());
        menus++;
      }
    }
    if (ticks == 100 + 8 * MENU_TICKS + 5) {
      mc.setScreen(null);
      mc.player.lookAt(
          net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
          net.minecraft.world.phys.Vec3.atCenterOf(positions.getLast()));
    }
    if (ticks == 100 + 8 * MENU_TICKS + 30) screenshot(mc, "advanced-machines-jade");
    if (ticks == 100 + 8 * MENU_TICKS + 50) {
      if (menus != 8) throw new IllegalStateException("Incomplete machine GUI smoke");
      IC2RAddons.LOGGER.info(
          "MACHINE_SMOKE_PASS language={} menus={}", mc.options.languageCode, menus);
      mc.stop();
    }
  }

  private MachineClientSmoke() {}
}
