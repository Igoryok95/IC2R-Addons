package com.igoryok95.ic2raddons.bridge.client;

import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.ItemState;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.experimental.ExperimentalPipes;
import ic2.core.item.ElectricItemManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** Opt-in real GUI pass, also checking the paired core's pipe tags through Advanced drills. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class EngineInfoClientSmoke {
  private static final BlockPos POS = new BlockPos(0, 128, 0);
  private static int ticks, waitTicks;
  private static CompletableFuture<Void> pending;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.engineInfoSmoke")) return;
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    if (mc.player == null
        || mc.level == null
        || server == null
        || mc.getOverlay() != null
        || pending != null && !pending.isDone()) return;
    if (pending != null) pending.join();
    mc.options.pauseOnLostFocus = false;
    int phase = ++ticks;
    var id = mc.player.getUUID();
    if (phase == 1) {
      mc.options.guiScale().set(2);
      mc.resizeDisplay();
      pending =
          CompletableFuture.runAsync(
              () -> {
                var player = server.getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                level.setBlockAndUpdate(POS.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(POS, Blocks.AIR.defaultBlockState());
                var engine = ResourceLocation.parse("ic2r_addons:electric_engine");
                if (!BuiltInRegistries.BLOCK.containsKey(engine))
                  throw new IllegalStateException("Supply BuildCraft for this opt-in engine pass");
                level.setBlockAndUpdate(
                    POS, BuiltInRegistries.BLOCK.get(engine).defaultBlockState());
                ((Ic2TileEntity) level.getBlockEntity(POS))
                    .getComponent(Energy.class)
                    .addEnergy(10000);
                player.connection.teleport(.5, 128, -2.5, 0, 0);
              },
              server);
      language(mc, "ru_ru");
    }
    if (phase == 35 || phase == 115) {
      pending =
          CompletableFuture.runAsync(
              () -> {
                var player = server.getPlayerList().getPlayer(id);
                ((IHasGui) player.serverLevel().getBlockEntity(POS))
                    .openManagedBe(player, InteractionHand.MAIN_HAND);
              },
              server);
    }
    if ((phase == 54 || phase == 134 || phase == 64 || phase == 144)
        && mc.screen instanceof Ic2Gui<?> gui) {
      int left = (mc.getWindow().getGuiScaledWidth() - gui.getXSize()) / 2;
      int top = (mc.getWindow().getGuiScaledHeight() - gui.getYSize()) / 2;
      boolean hover = phase == 64 || phase == 144;
      pointer(mc, left + (hover ? 7 : 110), top + (hover ? 7 : 75));
    }
    if (phase == 55 || phase == 135 || phase == 65 || phase == 145) {
      if (!(mc.screen instanceof Ic2Gui<?> gui)) {
        if (++waitTicks < 100) {
          ticks--;
          return;
        }
        throw new IllegalStateException("Engine GUI did not open");
      }
      waitTicks = 0;
      int left = (mc.getWindow().getGuiScaledWidth() - gui.getXSize()) / 2;
      int top = (mc.getWindow().getGuiScaledHeight() - gui.getYSize()) / 2;
      boolean hover = phase == 65 || phase == 145;
      if (!hover) verifyVisibleIcon(mc, left, top);
      Screenshot.grab(
          mc.gameDirectory,
          "engine-info-" + mc.options.languageCode + (hover ? "-tooltip.png" : "-icon.png"),
          mc.getMainRenderTarget(),
          text -> {});
    }
    if (phase == 80) {
      mc.player.closeContainer();
      language(mc, "en_us");
    }
    if (phase == 155) {
      verifyAdvancedDrills();
      LoggerFactory.getLogger("IC2R-Engine-Info")
          .info(
              "IC2R_ENGINE_INFO_CLIENT_PASS: visible blue info icon and native upgrade tooltip in RU/EN; Advanced drill pipe profiles");
      mc.stop();
    }
  }

  private static void verifyVisibleIcon(Minecraft mc, int left, int top) {
    double scale = mc.getWindow().getGuiScale();
    int blue = 0;
    try (var frame = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
      for (int y = (int) ((top + 3) * scale); y < (top + 13) * scale; y++)
        for (int x = (int) ((left + 3) * scale); x < (left + 13) * scale; x++) {
          int pixel = frame.getPixelRGBA(x, y);
          int r = pixel & 255, g = pixel >> 8 & 255, b = pixel >> 16 & 255;
          if (r < 120 && g > 80 && b > 150) blue++;
        }
    }
    if (blue == 0)
      throw new IllegalStateException("Engine's native blue information icon is covered");
  }

  private static void verifyAdvancedDrills() {
    float[][] rates = {{48, 24, 16}, {72, 36, 24}};
    var tools = List.of(GraviItems.ADVANCED_DRILL.get(), GraviItems.ADVANCED_IRIDIUM_DRILL.get());
    for (int t = 0; t < tools.size(); t++)
      for (int mode = 0; mode < 3; mode++) {
        var stack = ElectricItemManager.getCharged(tools.get(t), 10000);
        final int efficiency = mode;
        ItemState.update(stack, tag -> tag.putInt("efficiency", efficiency));
        for (var pipe : ExperimentalPipes.BLOCKS.values())
          if (stack.getDestroySpeed(pipe.defaultBlockState()) != rates[t][mode])
            throw new IllegalStateException("Advanced drill pipe rate changed");
      }
  }

  private static void pointer(Minecraft mc, double x, double y)
      throws ReflectiveOperationException {
    for (var entry :
        java.util.Map.of(
                "xpos",
                x * mc.getWindow().getScreenWidth() / mc.getWindow().getGuiScaledWidth(),
                "ypos",
                y * mc.getWindow().getScreenHeight() / mc.getWindow().getGuiScaledHeight())
            .entrySet()) {
      var field = mc.mouseHandler.getClass().getDeclaredField(entry.getKey());
      field.setAccessible(true);
      field.setDouble(mc.mouseHandler, entry.getValue());
    }
  }

  private static void language(Minecraft mc, String code) {
    mc.options.languageCode = code;
    mc.getLanguageManager().setSelected(code);
    var previous = pending;
    pending =
        previous == null
            ? mc.reloadResourcePacks()
            : previous.thenCompose(
                ignored -> mc.submit(mc::reloadResourcePacks).thenCompose(value -> value));
  }

  private EngineInfoClientSmoke() {}
}
