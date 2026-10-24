package com.igoryok95.ic2raddons.bridge.client;

import com.igoryok95.ic2raddons.bridge.bc.*;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Developer-only visual and real Jade network smoke; invoked only by -PclientSmoke. */
public final class EngineClientSmoke {
  public static void tick(Minecraft mc, int ticks, BlockPos machine) {
    if (ticks == 340) {
      mc.setScreen(null);
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                if (level.getBlockEntity(machine)
                    instanceof com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer t) {
                  t.inputSlot.put(ItemStack.EMPTY);
                  t.outputSlot.put(ItemStack.EMPTY);
                }
                level.setBlockAndUpdate(machine.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                // Isolate the LV/MV engine from the existing extreme-voltage solar demonstration.
                level.setBlockAndUpdate(machine.west(), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(machine, BcContent.ENGINE.get().defaultBlockState());
                var engine = (ElectricEngine) level.getBlockEntity(machine);
                ((ic2.api.tile.IWrenchAble) BcContent.ENGINE.get())
                    .setFacing(level, machine, Direction.EAST, player);
                engine.energy.addEnergy(9000);
                var quarry =
                    BuiltInRegistries.BLOCK.get(
                        ResourceLocation.parse("buildcraftbuilders:quarry"));
                level.setBlockAndUpdate(machine.east(), quarry.defaultBlockState());
                ((buildcraft.builders.tile.TileQuarry) level.getBlockEntity(machine.east()))
                    .onPlacedBy(player, new ItemStack(quarry));
                player.connection.teleport(
                    machine.getX() + 0.5, machine.getY() + 0.1, machine.getZ() - 2.5, 0, 22);
              });
    }
    if (ticks > 350 && ticks < 430) {
      mc.player.setYRot(0);
      mc.player.setXRot(22);
    }
    if (ticks == 410) {
      net.minecraft.client.Screenshot.grab(
          mc.gameDirectory,
          "ic2r-addons-engine-jade-en.png",
          mc.getMainRenderTarget(),
          message -> {});
      if (net.neoforged.fml.ModList.get().isLoaded("jade")) {
        var data = snownee.jade.impl.WailaClientRegistration.instance().getServerData();
        if (data == null || !data.contains("ic2r_addons_engine"))
          throw new IllegalStateException("Real Jade engine response missing: " + data);
        IC2RAddons.LOGGER.info(
            "IC2R_ADDONS_JADE_NETWORK_PASS {}", data.getCompound("ic2r_addons_engine"));
      }
    }
  }

  private EngineClientSmoke() {}
}
