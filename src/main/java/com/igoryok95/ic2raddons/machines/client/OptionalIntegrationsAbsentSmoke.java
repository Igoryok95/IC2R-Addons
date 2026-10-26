package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in client launch without JEI, Jade or BuildCraft. Never touches a normal installation. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class OptionalIntegrationsAbsentSmoke {
  private static int ticks;
  private static final BlockPos POS = new BlockPos(0, 128, 48);

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.integrationAbsentSmoke")) return;
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.getSingleplayerServer() == null) return;
    if (ModList.get().isLoaded("jei")
        || ModList.get().isLoaded("jade")
        || ModList.get().isLoaded("buildcraftcore"))
      throw new IllegalStateException("Absence smoke must not load optional integrations");
    mc.options.pauseOnLostFocus = false;
    int index = ticks / 50;
    int phase = ticks++ % 50;
    if (index == 2) {
      IC2RAddons.LOGGER.info("INTEGRATION_ABSENT_SMOKE_PASS: core_and_addon_menus=2");
      mc.stop();
      return;
    }
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    if (phase == 0)
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            player.closeContainer();
            player.serverLevel().setBlockAndUpdate(POS.below(), Blocks.BEDROCK.defaultBlockState());
            player.serverLevel().removeBlock(POS, false);
            player
                .serverLevel()
                .setBlockAndUpdate(
                    POS,
                    (index == 0
                            ? Ic2Blocks.INDUCTION_FURNACE
                            : AdvMachinesBlocks.ROTARY_MACERATOR.get())
                        .defaultBlockState());
            player.teleportTo(.5, 128, 45.5);
          });
    if (phase == 10)
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            ((IHasGui) player.serverLevel().getBlockEntity(POS))
                .openManagedBe(player, InteractionHand.MAIN_HAND);
          });
    if (phase == 40 && !(mc.screen instanceof Ic2Gui<?>))
      throw new IllegalStateException("Core or addon menu failed without optional integrations");
  }
}
