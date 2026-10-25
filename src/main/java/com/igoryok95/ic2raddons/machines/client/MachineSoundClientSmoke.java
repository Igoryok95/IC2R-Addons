package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.api.recipe.Recipes;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.sound.Sound;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** One opt-in playback regression pass in the isolated development world. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class MachineSoundClientSmoke {
  private static final BlockPos POS = new BlockPos(0, 128, 48);
  private static int ticks;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.machineSoundSmoke")) return;
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
    mc.options.pauseOnLostFocus = false;
    int tick = ++ticks;
    if (tick == 1) {
      mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(1.0);
      mc.options.getSoundSourceOptionInstance(SoundSource.BLOCKS).set(1.0);
      ic2.core.sound.Ic2AudioSettings.setVolume(1);
      var id = mc.player.getUUID();
      var server = mc.getSingleplayerServer();
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            player.setGameMode(GameType.CREATIVE);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            var level = player.serverLevel();
            level.setBlockAndUpdate(POS.below(), Blocks.BEDROCK.defaultBlockState());
            level.removeBlock(POS, false);
            level.setBlockAndUpdate(
                POS, AdvMachinesBlocks.ROTARY_MACERATOR.get().defaultBlockState());
            var machine = (TileEntityHeatingMachine) level.getBlockEntity(POS);
            var saved = machine.saveWithFullMetadata(level.registryAccess());
            saved.putShort("heat", (short) 3750);
            machine.loadWithComponents(saved, level.registryAccess());
            machine.getComponent(Energy.class).addEnergy(10000);
            machine.setManualWarmup(true);
            player.teleportTo(.5, 128, 45.5);
          });
    } else if (tick == 60) {
      check(mc, false, "manual warmup");
      change(mc, MachineSoundClientSmoke::addInput);
    } else if (tick == 100) {
      check(mc, true, "recipe processing");
      change(mc, machine -> machine.inputSlot.put(ItemStack.EMPTY));
    } else if (tick == 140) {
      check(mc, false, "input exhausted with warmup enabled");
      change(mc, MachineSoundClientSmoke::addInput);
    } else if (tick == 180) {
      check(mc, true, "processing resumed");
      change(
          mc,
          machine -> {
            for (int slot = 0; slot < machine.outputSlot.size(); slot++)
              machine.outputSlot.put(slot, new ItemStack(Items.BEDROCK, 64));
          });
    } else if (tick == 220) {
      check(mc, false, "output blocked with warmup enabled");
      change(
          mc,
          machine -> {
            for (int slot = 0; slot < machine.outputSlot.size(); slot++)
              machine.outputSlot.put(slot, ItemStack.EMPTY);
            machine.getComponent(Energy.class).useEnergy(machine.getEnergy());
          });
    } else if (tick == 260) {
      check(mc, false, "power loss while still hot");
      change(mc, machine -> machine.getComponent(Energy.class).addEnergy(10000));
    } else if (tick == 300) {
      check(mc, true, "power restored");
      change(
          mc,
          machine -> {
            machine.inputSlot.put(ItemStack.EMPTY);
            machine.setManualWarmup(false);
          });
    } else if (tick == 340) {
      check(mc, false, "cooling with residual heat");
      IC2RAddons.LOGGER.info("MACHINE_SOUND_SMOKE_PASS: all eight playback transitions passed");
      mc.stop();
    }
  }

  private static void addInput(TileEntityHeatingMachine machine) {
    for (var recipe : Recipes.macerator.get(machine.getLevel()).getRecipes()) {
      if (recipe.getOutput().isEmpty() || recipe.getInput().getInputs().isEmpty()) continue;
      var input = recipe.getInput().getInputs().getFirst();
      machine.inputSlot.put(input.copyWithCount(input.getMaxStackSize()));
      return;
    }
    throw new IllegalStateException("No macerator recipe for the playback check");
  }

  private static void change(Minecraft mc, Consumer<TileEntityHeatingMachine> action) {
    var server = mc.getSingleplayerServer();
    var dimension = mc.level.dimension();
    server.execute(
        () ->
            action.accept(
                (TileEntityHeatingMachine) server.getLevel(dimension).getBlockEntity(POS)));
  }

  private static void check(Minecraft mc, boolean expected, String phase)
      throws ReflectiveOperationException {
    var machine = (TileEntityHeatingMachine) mc.level.getBlockEntity(POS);
    var field = TileEntityBase.class.getDeclaredField("loopingSound");
    field.setAccessible(true);
    var sound = machine == null ? null : (Sound) field.get(machine);
    boolean playing = sound != null && sound.isPlaying();
    if (machine == null || machine.getActive() != expected || playing != expected)
      throw new IllegalStateException("Machine audio mismatch during " + phase + ": " + playing);
    IC2RAddons.LOGGER.info("MACHINE_SOUND_SMOKE: {} -> playing={}", phase, playing);
  }
}
