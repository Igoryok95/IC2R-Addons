package com.igoryok95.ic2raddons.solar.gametest;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import com.igoryok95.ic2raddons.solar.SolarPanelType;
import com.igoryok95.ic2raddons.solar.te.TileEntitySolarPanel;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.gui.GuiElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Real model, translated-menu and live-tooltip checks in the opt-in development world. */
@EventBusSubscriber(modid = IC2RAddons.MODID, value = Dist.CLIENT)
public final class SolarTiersClientSmoke {
  private static final int MENU_TICKS = 140;
  private static int ticks;
  private static volatile List<BlockPos> positions;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.solarTiersSmoke")) return;
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
    mc.options.pauseOnLostFocus = false;
    var id = mc.player.getUUID();
    if (ticks++ == 0) {
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                var base = new BlockPos(0, 128, 0);
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                player.getInventory().clearContent();
                for (int x = -3; x <= 10; x++)
                  for (int z = -3; z <= 8; z++) {
                    level.setBlockAndUpdate(
                        base.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++) {
                      var pos = base.offset(x, y, z);
                      if (level.getBlockEntity(pos)
                          instanceof net.minecraft.world.Container inventory)
                        inventory.clearContent();
                      level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                  }
                level
                    .getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(-3, 127, -3, 11, 133, 9))
                    .forEach(net.minecraft.world.entity.Entity::discard);
                var spots = new ArrayList<BlockPos>();
                for (var type : SolarPanelType.values()) {
                  int index = type.ordinal();
                  var pos = base.offset(index % 4 * 2, 0, 2 + index / 4 * 3);
                  level.setBlockAndUpdate(
                      pos,
                      BuiltInRegistries.BLOCK
                          .get(IC2RAddons.id(type.name().toLowerCase(Locale.ROOT) + "_solar_panel"))
                          .defaultBlockState());
                  spots.add(pos);
                }
                positions = List.copyOf(spots);
              });
    }
    if (positions == null) return;
    if (ticks == 80) {
      for (var pos : positions) {
        var state = mc.level.getBlockState(pos);
        var model = mc.getBlockRenderer().getBlockModel(state);
        for (var side : Direction.values()) {
          var quads = model.getQuads(state, side, RandomSource.create(0));
          if (quads.isEmpty()) throw new IllegalStateException("Missing solar face: " + side);
          for (var quad : quads) {
            var sprite = quad.getSprite();
            if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())
                || sprite.contents().height() != 32)
              throw new IllegalStateException(
                  "Solar face is missing or is not 32 pixels: " + sprite.contents().name());
          }
        }
      }
      IC2RAddons.LOGGER.info("SOLAR_TIERS_TEXTURES_PASS panels=8 faces=48 resolution=32");
    }
    int step = ticks - 100;
    if (step >= 0 && step < positions.size() * MENU_TICKS) {
      int index = step / MENU_TICKS;
      if (step % MENU_TICKS == 0) {
        mc.getSingleplayerServer()
            .execute(
                () -> {
                  var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                  var pos = positions.get(index);
                  var panel = (TileEntitySolarPanel) player.serverLevel().getBlockEntity(pos);
                  var energy = panel.getComponent(Energy.class);
                  energy.forceAddEnergy(energy.getCapacity() / 2 - energy.getEnergy());
                  player.closeContainer();
                  player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 16);
                  ((IHasGui) panel).openManagedBe(player, InteractionHand.MAIN_HAND);
                });
      }
      if (step % MENU_TICKS == 49) moveCursor(mc, 4, 4);
      if (step % MENU_TICKS == 50) {
        if (!(mc.screen instanceof Ic2Gui<?> gui))
          throw new IllegalStateException("Missing solar menu " + index);
        if (mc.font.width(gui.getTitle()) > gui.getXSize() - 12
            || gui.getTitle().getString().startsWith("container."))
          throw new IllegalStateException(
              "Oversized or untranslated solar title " + gui.getTitle().getString());
        var panel = (TileEntitySolarPanel) mc.level.getBlockEntity(positions.get(index));
        for (String text :
            List.of(panel.getPacketOutputText(), panel.getMaxOutput(), panel.getOutput()))
          if (mc.font.width(text) > 136 || text.startsWith("screen."))
            throw new IllegalStateException("Solar reading does not fit: " + text);
        for (var slot : gui.getMenu().slots)
          if (slot.x < 0
              || slot.y < 0
              || slot.x + 16 > gui.getXSize()
              || slot.y + 16 > gui.getYSize())
            throw new IllegalStateException("Solar slot outside menu " + index);
        verifyGaugeTooltip(gui, panel);
        screenshot(mc, "solar-tier-" + index);
      }
      if (step % MENU_TICKS >= 69 && step % MENU_TICKS <= 75 && mc.screen instanceof Ic2Gui<?> gui)
        hoverGauge(mc, gui);
      if (step % MENU_TICKS == 75) screenshot(mc, "solar-tier-tooltip-" + index);
    }
    if (ticks == 100 + positions.size() * MENU_TICKS + 40) {
      IC2RAddons.LOGGER.info(
          "SOLAR_TIERS_CLIENT_PASS language={} panels=8", mc.options.languageCode);
      mc.stop();
    }
  }

  private static void verifyGaugeTooltip(Ic2Gui<?> gui, TileEntitySolarPanel panel) {
    try {
      var elementsField = Ic2Gui.class.getDeclaredField("elements");
      elementsField.setAccessible(true);
      var providerField = GuiElement.class.getDeclaredField("tooltipProvider");
      providerField.setAccessible(true);
      for (var element : (List<GuiElement<?>>) elementsField.get(gui)) {
        if (!element.contains(25, 30)) continue;
        var provider = (Supplier<String>) providerField.get(element);
        if (provider != null && provider.get().equals(panel.getEnergyTooltip())) return;
      }
      throw new IllegalStateException("Solar gauge did not resolve its live energy tooltip");
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException("Cannot inspect solar gauge", error);
    }
  }

  private static void screenshot(Minecraft mc, String name) {
    Screenshot.grab(
        mc.gameDirectory,
        name + "-" + mc.options.languageCode + ".png",
        mc.getMainRenderTarget(),
        message -> {});
  }

  private static void hoverGauge(Minecraft mc, Ic2Gui<?> gui) {
    moveCursor(
        mc,
        (gui.getGuiLeft() + 25D)
            * mc.getWindow().getScreenWidth()
            / mc.getWindow().getGuiScaledWidth(),
        (gui.getGuiTop() + 30D)
            * mc.getWindow().getScreenHeight()
            / mc.getWindow().getGuiScaledHeight());
  }

  public static void moveCursor(Minecraft mc, double x, double y) {
    try {
      var move =
          net.minecraft.client.MouseHandler.class.getDeclaredMethod(
              "onMove", long.class, double.class, double.class);
      move.setAccessible(true);
      move.invoke(mc.mouseHandler, mc.getWindow().getWindow(), x, y);
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException("Cannot position the cursor in the client check", error);
    }
  }
}
