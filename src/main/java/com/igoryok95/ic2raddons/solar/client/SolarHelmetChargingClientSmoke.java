package com.igoryok95.ic2raddons.solar.client;

import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Items;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** Opt-in real server/client check for empty worn helmets, including an open inventory. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class SolarHelmetChargingClientSmoke {
  private static int stage, settled;
  private static CompletableFuture<Double> pending;
  private static boolean equipped, checking;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.solarHelmetChargingSmoke")) return;
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    if (mc.player == null || mc.level == null || server == null || mc.getOverlay() != null) return;
    mc.options.pauseOnLostFocus = false;
    if (pending != null) {
      if (!pending.isDone()) return;
      double serverCharge = pending.join();
      pending = null;
      if (checking) {
        double clientCharge =
            ElectricItem.manager.getCharge(mc.player.getItemBySlot(EquipmentSlot.HEAD));
        require(
            serverCharge > 0 && clientCharge > 0,
            "stage " + stage + ": server=" + serverCharge + ", client=" + clientCharge);
        LoggerFactory.getLogger("IC2R-Solar-Helmet")
            .info(
                "SOLAR_HELMET_CHARGE_STAGE_PASS: stage={}, server={}, client={}",
                stage,
                serverCharge,
                clientCharge);
        equipped = checking = false;
        if (++stage == 16) {
          LoggerFactory.getLogger("IC2R-Solar-Helmet")
              .info(
                  "SOLAR_HELMET_CHARGING_PASS: all four helmets, day/night, empty inventory/full tools, live inventory synchronization");
          mc.stop();
          return;
        }
      } else {
        mc.setScreen(new InventoryScreen(mc.player));
      }
    }
    if (!equipped) {
      var id = mc.player.getUUID();
      int current = stage;
      pending =
          CompletableFuture.supplyAsync(
              () -> {
                equip(server.getPlayerList().getPlayer(id), current);
                return 0.0;
              },
              server);
      equipped = true;
      settled = 0;
      return;
    }
    if (++settled < 30) return;
    var id = mc.player.getUUID();
    pending =
        CompletableFuture.supplyAsync(
            () ->
                ElectricItem.manager.getCharge(
                    server.getPlayerList().getPlayer(id).getItemBySlot(EquipmentSlot.HEAD)),
            server);
    checking = true;
  }

  private static void equip(ServerPlayer player, int stage) {
    Item[] helmets = {
      AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
      AdvSolarBlocks.HYBRID_SOLAR_HELMET.get(),
      AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
      AdvSolarBlocks.QUANTUM_SOLAR_HELMET.get()
    };
    var level = player.serverLevel();
    player.setGameMode(GameType.SURVIVAL);
    player.getInventory().clearContent();
    player.removeAllEffects();
    player.setAirSupply(player.getMaxAirSupply());
    player.getFoodData().setFoodLevel(20);
    player.setHealth(20);
    player.getAbilities().flying = false;
    player.onUpdateAbilities();
    for (var slot : EquipmentSlot.values()) {
      if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
        player.setItemSlot(slot, ItemStack.EMPTY);
    }
    level.setBlockAndUpdate(new BlockPos(305, 129, 305), Blocks.SMOOTH_STONE.defaultBlockState());
    for (int y = 130; y < level.getMaxBuildHeight(); y++)
      level.setBlockAndUpdate(new BlockPos(305, y, 305), Blocks.AIR.defaultBlockState());
    player.connection.teleport(305.5, 130, 305.5, 0, 0);
    level.setDayTime((stage / 4 & 1) == 0 ? 6000 : 18000);
    level.updateSkyBrightness();
    level.setWeatherParameters(10000, 0, false, false);
    player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmets[stage % 4]));
    if (stage >= 8) {
      player
          .getInventory()
          .setItem(
              0,
              ElectricItemManager.getCharged(
                  GraviItems.ADVANCED_IRIDIUM_DRILL.get(), Double.POSITIVE_INFINITY));
      player
          .getInventory()
          .setItem(
              1,
              ElectricItemManager.getCharged(
                  GraviItems.ADVANCED_CHAINSAW.get(), Double.POSITIVE_INFINITY));
      player.setItemSlot(
          EquipmentSlot.OFFHAND,
          ElectricItemManager.getCharged(Ic2Items.NANO_SABER, Double.POSITIVE_INFINITY));
    }
    require(level.canSeeSky(player.blockPosition().above()), "test column must see the sky");
    player.inventoryMenu.broadcastChanges();
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
