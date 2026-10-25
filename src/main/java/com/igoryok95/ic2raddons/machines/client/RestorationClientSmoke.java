package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.machines.te.MachineClientFixtures;
import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.wiring.GuiElectricBlock;
import ic2.core.experimental.*;
import ic2.core.ref.Ic2Blocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** One opt-in pass over changed screens and original profession artwork in an isolated world. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class RestorationClientSmoke {
  private static int ticks, captured;
  private static double originalVolume;
  private static float vanillaBlockVolume, vanillaMasterVolume;
  private static final Object AUDIO_OWNER = new Object();
  private static ic2.core.sound.Sound loopProbe;
  private static ic2.core.sound.PositionedSoundInstance volumeProbe;
  private static final int FIRST_MENU = Integer.getInteger("ic2r_addons.restorationStart", 0);
  private static final int MENUS_END = 80 + (23 - FIRST_MENU) * 45;
  private static volatile List<BlockPos> positions;
  private static final List<Villager> villagers = new ArrayList<>();
  private static final BlockPos BASE = new BlockPos(0, 128, 0);

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.restorationSmoke")) return;
    var mc = Minecraft.getInstance();
    mc.options.pauseOnLostFocus = false;
    if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
    var id = mc.player.getUUID();
    if (ticks++ == 0)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                for (int x = -3; x <= 34; x++)
                  for (int z = -3; z <= 11; z++) {
                    level.setBlockAndUpdate(
                        BASE.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++)
                      level.setBlockAndUpdate(BASE.offset(x, y, z), Blocks.AIR.defaultBlockState());
                  }
                var blocks =
                    new ArrayList<Block>(
                        List.of(
                            Ic2Blocks.BATBOX,
                            Ic2Blocks.CESU,
                            Ic2Blocks.MFE,
                            Ic2Blocks.MFSU,
                            AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get(),
                            AdvSolarBlocks.HYBRID_SOLAR_PANEL.get(),
                            AdvSolarBlocks.ULTIMATE_SOLAR_PANEL.get(),
                            AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get()));
                for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) blocks.add(entry.get());
                var placed = new ArrayList<BlockPos>();
                for (int i = 0; i < blocks.size(); i++) {
                  var pos = BASE.offset(i * 2, 0, 2);
                  placed.add(pos);
                  level.setBlockAndUpdate(pos, blocks.get(i).defaultBlockState());
                  var tile = (Ic2TileEntity) level.getBlockEntity(pos);
                  var energy = tile.getComponent(Energy.class);
                  if (energy != null) energy.addEnergy(energy.getCapacity() * .4);
                  if (tile instanceof TileEntityHeatingMachine machine)
                    MachineClientFixtures.prepare(machine);
                }
                level.setBlockAndUpdate(
                    BASE.offset(1, 0, 6), ExperimentalBrewing.BARREL.defaultBlockState());
                level.setBlockAndUpdate(
                    BASE.offset(3, 0, 6),
                    ExperimentalPipes.BLOCKS.get("bronze_pipe_medium").defaultBlockState());
                var pipe = (ExperimentalFluidPipe) level.getBlockEntity(BASE.offset(3, 0, 6));
                pipe.pumps[Direction.NORTH.ordinal()] = 1;
                for (var old :
                    level.getEntitiesOfClass(
                        Villager.class,
                        new net.minecraft.world.phys.AABB(-4, 126, -4, 35, 134, 12))) old.discard();
                for (var entry : RestoredVillagers.ALL.entrySet()) {
                  var villager = EntityType.VILLAGER.create(level);
                  int index = villagers.size();
                  villager.setVillagerData(
                      villager.getVillagerData().setProfession(entry.getValue().get()).setLevel(5));
                  villager.setNoAi(true);
                  villager.setInvulnerable(true);
                  villager.setPos(index * 2 + .5, 128, 8.5);
                  villager.setYRot(180);
                  level.addFreshEntity(villager);
                  villagers.add(villager);
                }
                positions = List.copyOf(placed);
              });
    if (positions == null) return;
    int step = ticks < 80 ? -1 : ticks - 80 + FIRST_MENU * 45;
    if (step >= 0 && step < 23 * 45) {
      int index = step / 45;
      if (step % 45 == 0)
        mc.getSingleplayerServer()
            .execute(
                () -> {
                  var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                  player.closeContainer();
                  player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                  if (index < 16) {
                    var pos = positions.get(index);
                    player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 12);
                    var tile = player.serverLevel().getBlockEntity(pos);
                    if (tile instanceof TileEntityHeatingMachine machine)
                      MachineClientFixtures.prepare(machine);
                    ((IHasGui) tile).openManagedBe(player, InteractionHand.MAIN_HAND);
                  } else if (index == 16) {
                    var stack = new ItemStack(ExperimentalItems.CONTAINMENT_BOX);
                    player.setItemInHand(InteractionHand.OFF_HAND, stack);
                    player.inventoryMenu.broadcastChanges();
                    ((ContainmentBox) stack.getItem())
                        .getInventory(player, InteractionHand.OFF_HAND, stack)
                        .openManagedItem(player, InteractionHand.OFF_HAND, null);
                  } else {
                    var villager = villagers.get(index - 17);
                    player.connection.teleport(villager.getX(), 128, villager.getZ() - 2, 0, 0);
                    villager.interact(player, InteractionHand.MAIN_HAND);
                  }
                });
      if (step % 45 == 25) {
        if (index < 17) {
          if (!(mc.screen instanceof Ic2Gui<?> gui))
            throw new IllegalStateException("Missing restoration menu " + index);
          if (mc.font.width(gui.getTitle()) > gui.getXSize() - 12)
            throw new IllegalStateException("Oversized title " + gui.getTitle());
          for (var slot : gui.getMenu().slots)
            if (slot.x < 0
                || slot.y < 0
                || slot.x + 16 > gui.getXSize()
                || slot.y + 16 > gui.getYSize())
              throw new IllegalStateException("Slot outside menu " + index);
          if (gui instanceof GuiElectricBlock storage)
            for (var line : storage.getReadingLines())
              if (mc.font.width(line) > 90)
                throw new IllegalStateException("Oversized storage reading " + line);
          try {
            var field = Ic2Gui.class.getDeclaredField("elements");
            field.setAccessible(true);
            var tooltip = ic2.core.gui.GuiElement.class.getDeclaredField("tooltipProvider");
            tooltip.setAccessible(true);
            boolean liveTooltip = false;
            for (var element : (List<?>) field.get(gui)) {
              var supplier = (java.util.function.Supplier<?>) tooltip.get(element);
              if (supplier == null) continue;
              var text = String.valueOf(supplier.get());
              if (text.contains("%base."))
                throw new IllegalStateException("Unresolved tooltip " + text);
              if (text.contains("/") && text.contains("EU")) liveTooltip = true;
            }
            if (index >= 4 && index < 8 && !liveTooltip)
              throw new IllegalStateException("No live solar charge tooltip");
          } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
          }
          if (index >= 8 && index < 16) {
            var machine = (TileEntityHeatingMachine) mc.level.getBlockEntity(positions.get(index));
            if (!machine.getWarmupText().endsWith("%"))
              throw new IllegalStateException("Warmup lost percentage");
          }
        } else if (!(mc.screen
            instanceof net.minecraft.client.gui.screens.inventory.MerchantScreen))
          throw new IllegalStateException("Missing profession trading screen " + index);
        screenshot(mc, "restoration-menu-" + index);
        captured++;
      }
    }
    if (ticks == MENUS_END)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                player.closeContainer();
                player.connection.teleport(5.5, 129, 2.5, 0, 2);
              });
    if (ticks == MENUS_END + 50) screenshot(mc, "restoration-professions-world");
    if (ticks == MENUS_END + 65) {
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                player.setGameMode(GameType.SURVIVAL);
                player.getInventory().clearContent();
                for (var item : ExperimentalItems.ITEMS.values())
                  player.getInventory().add(new ItemStack(item));
                player.getInventory().add(new ItemStack(ExperimentalBrewing.BARREL_ITEM));
                player.getInventory().add(ExperimentalBrewing.drink((100 << 7) | 2));
                player.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new ItemStack(ExperimentalItems.SOLAR_HELMET));
                player.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.FEET,
                    new ItemStack(ExperimentalItems.STATIC_BOOTS));
                player.containerMenu.broadcastChanges();
              });
    }
    if (ticks == MENUS_END + 80) {
      for (var item : ExperimentalItems.ITEMS.values()) {
        var model = mc.getItemRenderer().getModel(new ItemStack(item), mc.level, mc.player, 0);
        if (model == mc.getModelManager().getMissingModel()
            || model.getParticleIcon().contents().name().getPath().equals("missingno"))
          throw new IllegalStateException("Missing Experimental item model/texture " + item);
      }
      mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
    }
    if (ticks == MENUS_END + 95) screenshot(mc, "restoration-items");
    if (ticks == MENUS_END + 110) {
      originalVolume = ic2.core.init.IC2ClientConfig.audio.volume.get();
      vanillaBlockVolume = mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.BLOCKS);
      vanillaMasterVolume =
          mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MASTER);
      mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.15);
      mc.setScreen(
          new net.minecraft.client.gui.screens.options.SoundOptionsScreen(mc.screen, mc.options));
      for (var child : mc.screen.children())
        if (child instanceof net.minecraft.client.gui.components.OptionsList list)
          list.setScrollAmount(0);
    }
    if (ticks == MENUS_END + 115) {
      net.minecraft.client.gui.components.AbstractWidget slider = null;
      for (var child : mc.screen.children()) {
        if (!(child instanceof net.minecraft.client.gui.components.OptionsList list)) continue;
        for (Object row : list.children())
          for (var widget :
              ((net.minecraft.client.gui.components.events.ContainerEventHandler) row).children())
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget button
                && button
                    .getMessage()
                    .getString()
                    .startsWith(
                        net.minecraft.network.chat.Component.translatable("ic2.audio.volume")
                                .getString()
                            + ":")) slider = button;
      }
      if (!(slider
          instanceof net.minecraft.client.OptionInstance.OptionInstanceSliderButton<?> button))
        throw new IllegalStateException("IC2R audio slider is absent");
      if (button.getWidth() != 150)
        throw new IllegalStateException("IC2R slider must share the narrow voice row");
      button.onClick(button.getX() + 4 + (button.getWidth() - 8) * .25, button.getY() + 10);
      button.applyUnsavedValue();
      if (Math.abs(ic2.core.init.IC2ClientConfig.audio.volume.get() - .25) > .01)
        throw new IllegalStateException("Audio slider did not save 25 percent");
      volumeProbe =
          new ic2.core.sound.PositionedSoundInstance(
              ic2.core.ref.Ic2SoundEvents.MACHINE_FURNACE_INDUCTION_LOOP,
              net.minecraft.sounds.SoundSource.BLOCKS,
              .8F,
              1,
              mc.player.blockPosition());
      volumeProbe.resolve(mc.getSoundManager());
      if (Math.abs(volumeProbe.getVolume() - .2F) > .01)
        throw new IllegalStateException("Machine sound ignores configured volume");
      loopProbe =
          ic2.core.IC2.soundManager.createSound(
              AUDIO_OWNER,
              ic2.core.ref.Ic2SoundEvents.MACHINE_FURNACE_INDUCTION_LOOP,
              net.minecraft.sounds.SoundSource.BLOCKS,
              mc.player.blockPosition(),
              .8F,
              1);
      loopProbe.play();
    }
    if (ticks == MENUS_END + 125) screenshot(mc, "restoration-audio");
    if (ticks == MENUS_END + 130) {
      ic2.core.sound.Ic2AudioSettings.setVolume(0);
      if (volumeProbe.getVolume() != 0) throw new IllegalStateException("Machine mute failed");
    }
    if (ticks == MENUS_END + 155) {
      ic2.core.sound.Ic2AudioSettings.setVolume(.5);
      if (Math.abs(volumeProbe.getVolume() - .4F) > .001
          || mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.BLOCKS)
              != vanillaBlockVolume)
        throw new IllegalStateException("Unmute failed or changed vanilla volume");
    }
    if (ticks == MENUS_END + 185) {
      if (!loopProbe.isPlaying())
        throw new IllegalStateException("Active machine loop did not resume after mute");
      loopProbe.stop();
      ic2.core.IC2.soundManager.removeAllSound(AUDIO_OWNER);
      ic2.core.sound.Ic2AudioSettings.setVolume(originalVolume);
      mc.options
          .getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER)
          .set((double) vanillaMasterVolume);
      mc.options.save();
    }
    if (ticks == MENUS_END + 200) {
      if (captured != 23 - FIRST_MENU)
        throw new IllegalStateException("Incomplete restoration screens");
      IC2RAddons.LOGGER.info(
          "RESTORATION_CLIENT_PASS language={} screens={}", mc.options.languageCode, captured);
      mc.stop();
    }
  }

  private static void screenshot(Minecraft mc, String name) {
    Screenshot.grab(
        mc.gameDirectory,
        name + "-" + mc.options.languageCode + ".png",
        mc.getMainRenderTarget(),
        message -> {});
  }

  private RestorationClientSmoke() {}
}
