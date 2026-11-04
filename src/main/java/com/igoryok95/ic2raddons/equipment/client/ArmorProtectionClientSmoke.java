package com.igoryok95.ic2raddons.equipment.client;

import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Items;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
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

/** One real server/client equipment sequence; runs only in the isolated opt-in world. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class ArmorProtectionClientSmoke {
  private static final EquipmentSlot[] SLOTS = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private static final int[] EXPECTED = {0, 3, 9, 17, 20, 20, 20, 20, 9, 3, 0, 3, 20, 20, 20, 0};
  private static int stage, settled;
  private static CompletableFuture<Void> pending;
  private static boolean changed;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.armorProtectionSmoke")) return;
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    if (mc.player == null || mc.level == null || server == null || mc.getOverlay() != null) return;
    mc.options.pauseOnLostFocus = false;
    if (pending != null) {
      if (!pending.isDone()) return;
      pending.join();
      pending = null;
    }
    if (!changed) {
      if (stage == 0) {
        mc.options.guiScale().set(2);
        mc.options.hideGui = false;
        mc.resizeDisplay();
        mc.setScreen(null);
      }
      var id = mc.player.getUUID();
      int current = stage;
      pending =
          CompletableFuture.runAsync(
              () -> change(server.getPlayerList().getPlayer(id), current), server);
      changed = true;
      settled = 0;
      return;
    }
    if (++settled < 20) return;
    require(
        mc.player.getArmorValue() == EXPECTED[stage],
        "client armor stage "
            + stage
            + ": expected "
            + EXPECTED[stage]
            + ", got "
            + mc.player.getArmorValue());
    if (stage == 4 || stage == 6 || stage == 8 || stage == 12 || stage == 13)
      Screenshot.grab(
          mc.gameDirectory,
          "armor-protection-stage-" + stage + ".png",
          mc.getMainRenderTarget(),
          ignored -> {});
    var id = mc.player.getUUID();
    int current = stage;
    pending =
        CompletableFuture.runAsync(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              require(player.getArmorValue() == EXPECTED[current], "server armor stage " + current);
            },
            server);
    changed = false;
    if (++stage == EXPECTED.length) {
      pending.join();
      LoggerFactory.getLogger("IC2R-Armor-Protection")
          .info(
              "IC2R_ARMOR_PROTECTION_PASS: 16 synchronized server/client states, native Nano/Quantum, upgraded suits, mixed suit, jump/fall energy use, removal and recharge; full suits 20 armor points");
      mc.stop();
    }
  }

  private static void change(ServerPlayer player, int stage) {
    switch (stage) {
      case 0 -> {
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setHealth(20);
        player.getFoodData().setFoodLevel(20);
        var level = player.serverLevel();
        level.setDayTime(6000);
        level.setWeatherParameters(10000, 0, false, false);
        for (var pos : BlockPos.betweenClosed(300, 129, 300, 310, 134, 310))
          level.setBlockAndUpdate(
              pos,
              pos.getY() == 129
                  ? Blocks.SMOOTH_STONE.defaultBlockState()
                  : Blocks.AIR.defaultBlockState());
        player.connection.teleport(305.5, 130, 305.5, 0, 15);
        for (var slot : SLOTS) player.setItemSlot(slot, ItemStack.EMPTY);
      }
      case 1 -> equip(player, EquipmentSlot.FEET, GraviItems.ADVANCED_NANO_BOOTS.get());
      case 2 -> equip(player, EquipmentSlot.LEGS, GraviItems.ADVANCED_NANO_LEGGINGS.get());
      case 3 -> equip(player, EquipmentSlot.CHEST, GraviItems.ADVANCED_NANO.get());
      case 4 -> equip(player, EquipmentSlot.HEAD, AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get());
      case 5 -> {
        consume(player);
        GraviItems.ADVANCED_NANO_BOOTS
            .get()
            .absorbFall(player.getItemBySlot(EquipmentSlot.FEET), 6);
        player.jumpFromGround();
      }
      case 6 ->
          suit(
              player,
              AdvSolarBlocks.HYBRID_SOLAR_HELMET.get(),
              GraviItems.GRAVI_CHESTPLATE.get(),
              GraviItems.ADVANCED_QUANTUM_LEGGINGS.get(),
              GraviItems.ADVANCED_QUANTUM_BOOTS.get());
      case 7 -> {
        consume(player);
        GraviItems.ADVANCED_QUANTUM_BOOTS
            .get()
            .absorbFall(player.getItemBySlot(EquipmentSlot.FEET), 20);
        player.jumpFromGround();
      }
      case 8 -> {
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      }
      case 9 -> player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      case 10 ->
          ElectricItem.manager.discharge(
              player.getItemBySlot(EquipmentSlot.FEET),
              Double.POSITIVE_INFINITY,
              Integer.MAX_VALUE,
              true,
              false,
              false);
      case 11 ->
          ElectricItem.manager.charge(
              player.getItemBySlot(EquipmentSlot.FEET), 100_000, Integer.MAX_VALUE, true, false);
      case 12 ->
          suit(
              player,
              Ic2Items.NANO_HELMET,
              Ic2Items.NANO_CHESTPLATE,
              Ic2Items.NANO_LEGGINGS,
              Ic2Items.NANO_BOOTS);
      case 13 ->
          suit(
              player,
              Ic2Items.QUANTUM_HELMET,
              Ic2Items.QUANTUM_CHESTPLATE,
              Ic2Items.QUANTUM_LEGGINGS,
              Ic2Items.QUANTUM_BOOTS);
      case 14 ->
          suit(
              player,
              AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
              GraviItems.ADVANCED_NANO.get(),
              Ic2Items.QUANTUM_LEGGINGS,
              Ic2Items.QUANTUM_BOOTS);
      case 15 -> {
        for (var slot : SLOTS) player.setItemSlot(slot, ItemStack.EMPTY);
      }
      default -> throw new IllegalArgumentException("stage");
    }
  }

  private static void consume(ServerPlayer player) {
    for (var slot : SLOTS)
      ElectricItem.manager.discharge(
          player.getItemBySlot(slot), 8, Integer.MAX_VALUE, true, false, false);
  }

  private static void suit(ServerPlayer player, Item head, Item chest, Item legs, Item feet) {
    Item[] items = {head, chest, legs, feet};
    for (int i = 0; i < SLOTS.length; i++) equip(player, SLOTS[i], items[i]);
  }

  private static void equip(ServerPlayer player, EquipmentSlot slot, Item item) {
    player.setItemSlot(slot, ElectricItemManager.getCharged(item, 500_000));
  }

  private static void require(boolean success, String message) {
    if (!success) throw new IllegalStateException(message);
  }
}
