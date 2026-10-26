package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.machines.te.MachineClientFixtures;
import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TextLabel;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** One isolated visual pass at maximum warm-up, including real widget clearances. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class WarmupLayoutClientSmoke {
  private static final BlockPos POS = new BlockPos(0, 128, 48);
  private static int ticks;
  private static int startupTicks;
  private static int checks;
  private static CompletableFuture<Void> reload;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.warmupLayoutSmoke")) return;
    var mc = Minecraft.getInstance();
    if (startupTicks > 80 && mc.getSingleplayerServer() == null) {
      mc.stop();
      return;
    }
    if (mc.player == null || mc.getSingleplayerServer() == null) return;
    if (++startupTicks < 80) return;
    mc.options.pauseOnLostFocus = false;
    var blocks = blocks();
    int view = ticks / 40;
    if (view >= blocks.size() * 2) {
      IC2RAddons.LOGGER.info("WARMUP_LAYOUT_COMPLETE: views={} clearance_checks={}", view, checks);
      mc.stop();
      return;
    }
    if (reload != null) {
      if (!reload.isDone() || mc.getOverlay() != null) return;
      reload = null;
    }
    var language = view < blocks.size() ? "ru_ru" : "en_us";
    if (!mc.getLanguageManager().getSelected().equals(language)) {
      mc.setScreen(null);
      mc.options.languageCode = language;
      mc.getLanguageManager().setSelected(language);
      reload = mc.reloadResourcePacks();
      return;
    }
    int index = view % blocks.size();
    int phase = ticks++ % 40;
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    if (phase == 0) {
      mc.options.guiScale().set(2);
      mc.resizeDisplay();
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            player.closeContainer();
            player.setGameMode(GameType.CREATIVE);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().clearContent();
            var level = player.serverLevel();
            level.setBlockAndUpdate(POS.below(), Blocks.BEDROCK.defaultBlockState());
            level.removeBlock(POS, false);
            level.setBlockAndUpdate(POS, blocks.get(index).defaultBlockState());
            var tile = (Ic2TileEntity) level.getBlockEntity(POS);
            if (tile instanceof TileEntityInduction furnace) {
              furnace.heat = 10000;
              furnace.inputSlotA.put(new ItemStack(Ic2Items.IRON_DUST, 64));
              furnace.inputSlotB.put(new ItemStack(Ic2Items.IRON_DUST, 64));
              furnace.setManualWarmup(true);
            } else if (tile instanceof TileEntityHeatingMachine machine) {
              MachineClientFixtures.prepare(machine);
              var saved = machine.saveWithFullMetadata(level.registryAccess());
              saved.putShort("heat", (short) 10000);
              machine.loadWithComponents(saved, level.registryAccess());
              machine.setManualWarmup(true);
            }
            tile.getComponent(Energy.class).addEnergy(10000);
            player.teleportTo(.5, 128, 45.5);
          });
    }
    if (phase == 8) {
      var expected =
          ((ic2.core.block.tileentity.Ic2TileEntityBlock) blocks.get(index))
              .getDummyTe()
              .getClass();
      if (!expected.isInstance(mc.level.getBlockEntity(POS))) {
        ticks--;
        return;
      }
    }
    if (phase == 8)
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            ((IHasGui) player.serverLevel().getBlockEntity(POS))
                .openManagedBe(player, InteractionHand.MAIN_HAND);
          });
    if (phase == 25) {
      if (!(mc.screen instanceof Ic2Gui<?> gui))
        throw new IllegalStateException("Missing layout GUI");
      check(gui);
      for (String name : List.of("xpos", "ypos"))
        field(net.minecraft.client.MouseHandler.class, name).setDouble(mc.mouseHandler, -1000);
    }
    if (phase == 30) {
      Screenshot.grab(
          mc.gameDirectory,
          "warmup-spacing-" + language + "-" + index + ".png",
          mc.getMainRenderTarget(),
          text -> {});
      IC2RAddons.LOGGER.info("WARMUP_LAYOUT_PASS: {} {}", language, blocks.get(index));
    }
  }

  @SuppressWarnings("unchecked")
  private static void check(Ic2Gui<?> gui) throws ReflectiveOperationException {
    var elements = (List<GuiElement<?>>) field(Ic2Gui.class, "elements").get(gui);
    var warmup =
        elements.stream()
            .filter(
                element ->
                    element instanceof TextLabel
                        && rect(element).x == 6
                        && (rect(element).y == 24
                            || rect(element).y == 36
                            || rect(element).y == 40))
            .toList();
    if (warmup.size() != 2)
      throw new IllegalStateException("Warm-up label/value column is missing");
    String value =
        gui.getMenu().base instanceof TileEntityInduction furnace
            ? furnace.getHeat()
            : ((TileEntityHeatingMachine) gui.getMenu().base).getWarmupText();
    if (!value.equals("100.00%"))
      throw new IllegalStateException("Layout must be checked at 100.00%: " + value);
    int smallest = Integer.MAX_VALUE;
    for (var label : warmup) {
      var text = rect(label);
      for (var other : elements) {
        if (other == label || !other.isEnabled()) continue;
        var obstacle = rect(other);
        if (obstacle.width <= 0 || obstacle.height <= 0) continue;
        int gap =
            Math.max(
                Math.max(obstacle.x - text.right(), text.x - obstacle.right()),
                Math.max(obstacle.y - text.bottom(), text.y - obstacle.bottom()));
        smallest = Math.min(smallest, gap);
        if (gap < 4)
          throw new IllegalStateException(
              "Warm-up text has only "
                  + gap
                  + "px clearance from "
                  + other.getClass().getSimpleName()
                  + ": "
                  + text
                  + " vs "
                  + obstacle);
        checks++;
      }
    }
    IC2RAddons.LOGGER.info("WARMUP_LAYOUT_CLEARANCE: minimum={}px value={}", smallest, value);
  }

  private record Rect(int x, int y, int width, int height) {
    int right() {
      return x + width;
    }

    int bottom() {
      return y + height;
    }
  }

  private static Rect rect(GuiElement<?> element) {
    try {
      int x = field(GuiElement.class, "x").getInt(element);
      int y = field(GuiElement.class, "y").getInt(element);
      int width = field(GuiElement.class, "width").getInt(element);
      int height = field(GuiElement.class, "height").getInt(element);
      if (element instanceof Gauge<?>) {
        var p = (Gauge.GaugeProperties) field(Gauge.class, "properties").get(element);
        int originX = x - p.hoverXOffset(), originY = y - p.hoverYOffset();
        int left = Math.min(0, p.bgXOffset()), top = Math.min(0, p.bgYOffset());
        x = originX + left;
        y = originY + top;
        width = Math.max(p.innerWidth(), p.bgXOffset() + Math.max(0, p.bgWidth())) - left;
        height = Math.max(p.innerHeight(), p.bgYOffset() + Math.max(0, p.bgHeight())) - top;
      }
      return new Rect(x, y, width, height);
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var field = type.getDeclaredField(name);
    field.setAccessible(true);
    return field;
  }

  private static List<Block> blocks() {
    return List.of(
        Ic2Blocks.INDUCTION_FURNACE,
        AdvMachinesBlocks.ROTARY_MACERATOR.get(),
        AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(),
        AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(),
        AdvMachinesBlocks.COMPACTING_RECYCLER.get(),
        AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(),
        AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(),
        AdvMachinesBlocks.WATER_JET_CUTTER.get(),
        AdvMachinesBlocks.THERMAL_WASHER.get());
  }
}
