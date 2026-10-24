// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.api.ItemStackHelper;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock;
import com.igoryok95.ic2raddons.control.tileentities.*;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Real menu / screen rendering and synchronized card data in an isolated developer world. */
@EventBusSubscriber(modid = IC2RAddons.MODID, value = Dist.CLIENT)
public final class ControlClientSmoke {
  private static int ticks, menus, frames;
  private static volatile List<BlockPos> positions;
  private static BlockPos engine;
  private static BlockPos sceneBase;
  private static final String[] NAMES = {
    "panel",
    "advanced-panel",
    "holo-panel",
    "range-trigger",
    "remote-monitor",
    "kit-assembler",
    "timer",
    "counter"
  };

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.controlSmoke")) return;
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
    mc.options.pauseOnLostFocus = false;
    if (frames++ % Math.clamp(Integer.getInteger("ic2r_addons.controlTickStride", 5), 1, 5) != 0)
      return;
    if (ticks++ == 0) {
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                var base = new BlockPos(0, 64, 0);
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                sceneBase = base;
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                for (int x = -8; x <= 8; x++)
                  for (int z = -2; z <= 10; z++) {
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
                var reactorPos = base.offset(4, 0, 4);
                level.setBlockAndUpdate(
                    reactorPos, ic2.core.ref.Ic2Blocks.NUCLEAR_REACTOR.defaultBlockState());
                var reactor =
                    (ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric)
                        level.getBlockEntity(reactorPos);
                reactor.setHeat(750);
                var blocks =
                    List.of(
                        ModItems.info_panel.get(),
                        ModItems.info_panel_advanced.get(),
                        ModItems.holo_panel.get(),
                        ModItems.range_trigger.get(),
                        ModItems.remote_thermo.get(),
                        ModItems.kit_assembler.get(),
                        ModItems.timer.get(),
                        ModItems.energy_counter.get(),
                        ModItems.average_counter.get(),
                        ModItems.thermal_monitor.get(),
                        ModItems.howler_alarm.get(),
                        ModItems.industrial_alarm.get());
                var placed = new ArrayList<BlockPos>();
                for (int i = 0; i < blocks.size(); i++) {
                  var pos = base.offset(-4 + (i % 4) * 2, 0, 3 + (i / 4) * 2);
                  var state = blocks.get(i).defaultBlockState();
                  if (state.hasProperty(FacingBlock.FACING))
                    state = state.setValue(FacingBlock.FACING, Direction.NORTH);
                  level.setBlockAndUpdate(pos, state);
                  placed.add(pos);
                  if (level.getBlockEntity(pos) instanceof TileEntityEnergyCounter counter) {
                    counter.setItem(0, new ItemStack(ic2.core.ref.Ic2Items.TRANSFORMER_UPGRADE, 2));
                    var data = counter.saveWithFullMetadata(level.registryAccess());
                    data.putDouble("total", 12345.5);
                    data.putLong("samples", 100);
                    counter.loadWithComponents(data, level.registryAccess());
                  }
                  if (level.getBlockEntity(pos) instanceof TileEntityInfoPanel panel) {
                    var card = new ItemStack(ModItems.card_ic2);
                    ItemStackHelper.setCoordinates(card, reactorPos);
                    panel.setItem(0, card);
                    level.setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    panel.setPowered(true);
                  }
                  if (level.getBlockEntity(pos) instanceof TileEntityRemoteThermalMonitor monitor) {
                    var card = new ItemStack(ModItems.card_ic2);
                    ItemStackHelper.setCoordinates(card, reactorPos);
                    monitor.setItem(0, card);
                  }
                }
                player.connection.teleport(
                    base.getX() + 0.5, base.getY() + 0.3, base.getZ() - 2.5, 0, 13);
                engine = base.offset(0, 0, 2);
                positions = List.copyOf(placed);
              });
    }
    if (positions == null) return;
    if (ticks == 40) {
      for (String slot :
          List.of("card", "range", "color", "touch", "power", "dischargeable", "transformer")) {
        var sprite =
            mc.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
                .apply(IC2RAddons.id("slots/slot_" + slot));
        if (sprite
            .contents()
            .name()
            .equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
          throw new IllegalStateException("Missing control slot sprite: " + slot);
      }
      for (var block : ModItems.BLOCKS.getEntries()) {
        var model = mc.getBlockRenderer().getBlockModel(block.get().defaultBlockState());
        if (model
            .getParticleIcon()
            .contents()
            .name()
            .equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
          throw new IllegalStateException("Missing control block texture: " + block.getId());
      }
      for (var item : ModItems.ITEMS.getEntries()) {
        var model =
            mc.getItemRenderer().getModel(new ItemStack(item.get()), mc.level, mc.player, 0);
        if (model
            .getParticleIcon()
            .contents()
            .name()
            .equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
          throw new IllegalStateException("Missing control item texture: " + item.getId());
      }
      IC2RAddons.LOGGER.info(
          "IC2R_CONTROL_TEXTURES_PASS slots=7 blocks={} items={}",
          ModItems.BLOCKS.getEntries().size(),
          ModItems.ITEMS.getEntries().size());
    }
    if (ticks >= 60 && ticks < 380 && (ticks - 60) % 40 == 0) {
      int index = (ticks - 60) / 40;
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var tile = player.serverLevel().getBlockEntity(positions.get(index));
                player.connection.teleport(
                    tile.getBlockPos().getX() + 0.5,
                    tile.getBlockPos().getY() + 0.1,
                    tile.getBlockPos().getZ() - 1.5,
                    0,
                    10);
                if (!(tile instanceof MenuProvider provider))
                  throw new IllegalStateException("Missing control menu: " + NAMES[index]);
                if (tile instanceof TileEntityKitAssembler assembler) {
                  assembler.setItem(1, new ItemStack(ModItems.card_energy.get()));
                  assembler.setItem(2, new ItemStack(ModItems.strong_string.get(), 2));
                  assembler.setItem(3, new ItemStack(ModItems.card_energy.get()));
                  var data = assembler.saveWithFullMetadata(player.registryAccess());
                  data.putInt("energy", 4500);
                  data.putDouble("production", 450);
                  assembler.loadWithComponents(data, player.registryAccess());
                  assembler.setChanged();
                }
                if (tile instanceof TileEntityTimer timer) {
                  timer.setTime(2000);
                  timer.setIsWorking(true);
                }
                IC2RAddons.LOGGER.info(
                    "IC2R_CONTROL_MENU_OPEN {} valid={} pos={}",
                    NAMES[index],
                    provider.createMenu(99, player.getInventory(), player).stillValid(player),
                    tile.getBlockPos());
                player.openMenu(provider, buffer -> buffer.writeBlockPos(tile.getBlockPos()));
              });
    }
    if (ticks >= 85 && ticks < 405 && (ticks - 85) % 40 == 0) {
      int index = (ticks - 85) / 40;
      if (!(mc.screen
          instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen))
        throw new IllegalStateException(
            "No synchronized control screen: "
                + NAMES[index]
                + " current="
                + mc.screen
                + " player="
                + mc.player.position()
                + " state="
                + mc.level.getBlockState(positions.get(index)));
      for (var slot : screen.getMenu().slots) {
        if (slot.x < 0
            || slot.y < 0
            || slot.x + 16 > screen.getXSize()
            || slot.y + 16 > screen.getYSize())
          throw new IllegalStateException("Invalid control slot position");
        var icon = slot.getNoItemIcon();
        if (icon != null
            && mc.getTextureAtlas(icon.getFirst())
                .apply(icon.getSecond())
                .contents()
                .name()
                .equals(
                    net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
          throw new IllegalStateException(
              "Missing real menu slot icon: " + NAMES[index] + " / " + icon.getSecond());
      }
      if (index == 5) {
        var assembler = (TileEntityKitAssembler) mc.level.getBlockEntity(positions.get(index));
        if (assembler.getEnergy() <= 0
            || assembler.getProductionFactor() <= 0
            || assembler.getProductionFactor() >= 24)
          throw new IllegalStateException(
              "Assembler energy/progress did not synchronize: "
                  + assembler.getEnergy()
                  + " / "
                  + assembler.getProduction());
        IC2RAddons.LOGGER.info(
            "IC2R_CONTROL_ASSEMBLER_SYNC_PASS energy={} progress={}",
            assembler.getEnergy(),
            assembler.getProduction());
      }
      if (index == 6) {
        var timer = (TileEntityTimer) mc.level.getBlockEntity(positions.get(index));
        if (!timer.getIsWorking() || timer.getTime() >= 2000 || timer.getTime() <= 0)
          throw new IllegalStateException("Timer did not synchronize its countdown");
      }
      shot(mc, "control-" + NAMES[index]);
      menus++;
    }
    if (ticks == 390) {
      mc.setScreen(null);
      var tile = (TileEntityThermalMonitor) mc.level.getBlockEntity(positions.get(9));
      mc.setScreen(new com.igoryok95.ic2raddons.control.gui.GuiThermalMonitor(tile));
    }
    if (ticks == 410) shot(mc, "control-thermal-monitor");
    if (ticks == 420) {
      var tile = (TileEntityHowlerAlarm) mc.level.getBlockEntity(positions.get(10));
      mc.setScreen(new com.igoryok95.ic2raddons.control.gui.GuiHowlerAlarm(tile, false));
    }
    if (ticks == 440) shot(mc, "control-alarm");
    if (ticks == 460) {
      mc.setScreen(null);
      var panel = (TileEntityInfoPanel) mc.level.getBlockEntity(positions.get(0));
      var lines = panel.getPanelStringList(false, true);
      if (lines == null
          || lines.isEmpty()
          || lines.stream()
              .anyMatch(
                  line -> line.textLeft != null && line.textLeft.contains("message.ic2r_addons")))
        throw new IllegalStateException("Missing localized live panel telemetry");
      IC2RAddons.LOGGER.info("IC2R_CONTROL_LIVE_PANEL_PASS {}", lines.size());
      var playerId = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                player.connection.teleport(
                    sceneBase.getX() + 0.5, sceneBase.getY() + 0.3, sceneBase.getZ() - 2.5, 0, 13);
              });
    }
    if (ticks == 500) shot(mc, "control-world");
    if (ticks == 510) {
      for (int i = 0; i < positions.size(); i++) {
        var state = mc.level.getBlockState(positions.get(i));
        if ((i == 7 || i == 8) && state.getRenderShape() != RenderShape.MODEL)
          throw new IllegalStateException("Invisible counter block: " + state);
      }
      IC2RAddons.LOGGER.info("IC2R_CONTROL_BLOCK_RENDER_PASS counters=2");
    }
    if (ticks >= 520 && ticks < 622 && (ticks - 520) % 6 == 0) {
      int index = (ticks - 520) / 6;
      var block = new ArrayList<>(ModItems.BLOCKS.getEntries()).get(index).get();
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                var pos = sceneBase.offset(7, 0, 1);
                if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container inventory)
                  inventory.clearContent();
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos.south(), Blocks.SMOOTH_STONE.defaultBlockState());
                var state = block.defaultBlockState();
                if (state.hasProperty(FacingBlock.FACING))
                  state = state.setValue(FacingBlock.FACING, Direction.NORTH);
                level.setBlockAndUpdate(pos, state);
                player.connection.teleport(
                    pos.getX() + 1.8, pos.getY() + 0.2, pos.getZ() - 1.8, 30, 22);
              });
    }
    if (ticks >= 523 && ticks < 625 && (ticks - 523) % 6 == 0) {
      int index = (ticks - 523) / 6;
      shot(
          mc,
          "control-block-"
              + new ArrayList<>(ModItems.BLOCKS.getEntries()).get(index).getId().getPath());
    }
    if (net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy"))
      com.igoryok95.ic2raddons.bridge.client.EngineClientSmoke.tick(mc, ticks - 320, engine);
    if (ticks == 720 && net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy"))
      mc.getSingleplayerServer()
          .execute(
              () -> {
                if (mc.getSingleplayerServer().overworld().getBlockEntity(engine)
                    instanceof com.igoryok95.ic2raddons.bridge.bc.ElectricEngine motor)
                  motor.energy.addEnergy(9000);
              });
    if (ticks == 760) {
      if (menus != 8) throw new IllegalStateException("Not all control menus rendered: " + menus);
      IC2RAddons.LOGGER.info(
          "IC2R_CONTROL_CLIENT_PASS menus={} language={}", menus, mc.options.languageCode);
      mc.stop();
    }
  }

  private static void shot(Minecraft mc, String name) {
    Screenshot.grab(
        mc.gameDirectory,
        name + "-" + mc.options.languageCode + ".png",
        mc.getMainRenderTarget(),
        message -> {});
  }
}
