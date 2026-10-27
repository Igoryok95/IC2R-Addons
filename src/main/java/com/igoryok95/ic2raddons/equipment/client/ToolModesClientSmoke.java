package com.igoryok95.ic2raddons.equipment.client;

import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.network.GraviNetwork;
import ic2.core.IC2;
import ic2.core.util.Keyboard;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** Real server tool actions and their actual client chat packets, once in each supported locale. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class ToolModesClientSmoke {
  private static final String MODE = "item.ic2.mining_laser.tooltip.mode";
  private static List<Change> cases;
  private static int ticks, index, countBefore, languages;
  private static CompletableFuture<Void> reload;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.toolModesSmoke")) return;
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    if (mc.player == null
        || mc.level == null
        || server == null
        || mc.getOverlay() != null
        || reload != null && !reload.isDone()) return;
    mc.options.pauseOnLostFocus = false;
    mc.options.hideGui = false;
    if (cases == null) {
      cases = cases();
      var id = mc.player.getUUID();
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            player.setGameMode(GameType.CREATIVE);
            player.getInventory().clearContent();
          });
      language(mc, "ru_ru");
      return;
    }
    int phase = ++ticks;
    if (phase == 10) {
      var change = cases.get(index);
      countBefore = messages(mc).size();
      var id = mc.player.getUUID();
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            var stack =
                new ItemStack(
                    BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("ic2r_addons:" + change.item)));
            ItemState.update(
                stack,
                tag -> {
                  if (change.key.equals("tree") || change.key.equals("enabled"))
                    tag.putBoolean(change.key, change.before != 0);
                  else tag.putInt(change.key, change.before);
                });
            player.getInventory().setItem(0, stack);
            player.getInventory().selected = 0;
            player.setShiftKeyDown(change.sneak);
            IC2.keyboard.processKeyUpdate(
                player,
                change.alt
                    ? 1 << Keyboard.Key.alt.ordinal()
                    : 1 << Keyboard.Key.modeSwitch.ordinal());
            if (change.hotkey) GraviNetwork.apply(player, new GraviNetwork.Action(1, 0, ""));
            else stack.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            IC2.keyboard.processKeyUpdate(player, 0);
            player.setShiftKeyDown(false);
            if (!change.key.equals("shear")) {
              int actual =
                  change.key.equals("tree") || change.key.equals("enabled")
                      ? (ItemState.enabled(stack, change.key) ? 1 : 0)
                      : ItemState.read(stack).getInt(change.key);
              require(actual == change.after, "Tool setting was not changed: " + change);
            }
          });
    }
    if (phase == 17) {
      var change = cases.get(index);
      var received = messages(mc);
      var expected = Component.translatable(change.format, Component.translatable(change.value));
      require(
          received.size() == countBefore + 1,
          "Expected exactly one chat message: " + change + ", count=" + received.size());
      var actual = received.getFirst().content();
      require(
          actual.getString().equals(expected.getString()),
          "Wrong localized chat: " + actual.getString() + ", expected=" + expected.getString());
      LoggerFactory.getLogger("IC2R-Tool-Modes")
          .info(
              "TOOL_MODE_CHAT_CHECK: {} {} -> {}",
              mc.options.languageCode,
              change.item,
              actual.getString());
      if (++index < cases.size()) ticks = 9;
      else mc.setScreen(new ChatScreen(""));
    }
    if (index == cases.size() && phase == 22)
      Screenshot.grab(
          mc.gameDirectory,
          "tool-mode-chat-" + mc.options.languageCode + ".png",
          mc.getMainRenderTarget(),
          ignored -> {});
    if (index == cases.size() && phase == 25) {
      if (++languages == 1) {
        index = ticks = 0;
        mc.setScreen(null);
        language(mc, "en_us");
      } else {
        LoggerFactory.getLogger("IC2R-Tool-Modes")
            .info(
                "IC2R_ADDONS_TOOL_MODE_CHAT_PASS: {} real server switches in each of RU/EN, exactly one localized chat notification per action",
                cases.size());
        mc.stop();
      }
    }
  }

  private static void language(Minecraft mc, String code) {
    mc.gui.getChat().clearMessages(false);
    mc.options.languageCode = code;
    mc.getLanguageManager().setSelected(code);
    reload = mc.reloadResourcePacks();
  }

  @SuppressWarnings("unchecked")
  private static List<GuiMessage> messages(Minecraft mc) throws ReflectiveOperationException {
    var chat = mc.gui.getChat();
    var field = chat.getClass().getDeclaredField("allMessages");
    field.setAccessible(true);
    return (List<GuiMessage>) field.get(chat);
  }

  private static List<Change> cases() {
    var cases = new ArrayList<Change>();
    for (String item : List.of("advanced_iridium_drill", "vajra", "advanced_diamond_drill")) {
      String[] mining =
          item.equals("vajra")
              ? new String[] {"normal", "vein", "extended"}
              : new String[] {"normal", "aoe"};
      cycle(cases, item, "mining", mining, MODE, false, false);
      cycle(
          cases,
          item,
          "efficiency",
          new String[] {"normal", "low_power", "fine"},
          "tooltip.ic2r_addons.efficiency",
          true,
          false);
      if (!item.equals("advanced_diamond_drill")) {
        cases.add(
            new Change(
                item,
                "enchantment",
                0,
                1,
                false,
                true,
                false,
                "tooltip.ic2r_addons.enchantment",
                "enchantment.minecraft.silk_touch"));
        cases.add(
            new Change(
                item,
                "enchantment",
                1,
                0,
                false,
                true,
                false,
                "tooltip.ic2r_addons.enchantment",
                "mode.ic2r_addons.fortune"));
      }
    }
    cycle(
        cases,
        "gravitool",
        "tool",
        new String[] {"wrench", "treetap", "hoe", "screwdriver"},
        MODE,
        false,
        false);
    cycle(
        cases,
        "relocator",
        "relocator",
        new String[] {"personal", "translocator", "portal"},
        MODE,
        false,
        false);
    for (int before = 0; before <= 1; before++) {
      cases.add(
          new Change(
              "advanced_chainsaw",
              "tree",
              before,
              1 - before,
              false,
              true,
              false,
              "tooltip.ic2r_addons.tree",
              before == 0 ? "ic2r_addons.on" : "ic2r_addons.off"));
      cases.add(
          new Change(
              "magnet",
              "enabled",
              before,
              1 - before,
              false,
              false,
              false,
              "tooltip.ic2r_addons.enabled",
              before == 0 ? "ic2r_addons.on" : "ic2r_addons.off"));
      cases.add(
          new Change(
              "magnet",
              "enabled",
              before,
              1 - before,
              false,
              false,
              true,
              "tooltip.ic2r_addons.enabled",
              before == 0 ? "ic2r_addons.on" : "ic2r_addons.off"));
      cases.add(
          new Change(
              "voider",
              "enabled",
              before,
              1 - before,
              false,
              false,
              false,
              "tooltip.ic2r_addons.enabled",
              before == 0 ? "ic2r_addons.on" : "ic2r_addons.off"));
    }
    // Finish the screenshot with the reported drill's notifications.
    cycle(
        cases,
        "advanced_diamond_drill",
        "mining",
        new String[] {"normal", "aoe"},
        MODE,
        false,
        false);
    cycle(
        cases,
        "advanced_diamond_drill",
        "efficiency",
        new String[] {"normal", "low_power", "fine"},
        "tooltip.ic2r_addons.efficiency",
        true,
        false);
    return cases;
  }

  private static void cycle(
      List<Change> cases,
      String item,
      String key,
      String[] names,
      String format,
      boolean alt,
      boolean sneak) {
    for (int before = 0; before < names.length; before++) {
      int after = (before + 1) % names.length;
      cases.add(
          new Change(
              item,
              key,
              before,
              after,
              alt,
              sneak,
              false,
              format,
              "mode.ic2r_addons." + names[after]));
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }

  private record Change(
      String item,
      String key,
      int before,
      int after,
      boolean alt,
      boolean sneak,
      boolean hotkey,
      String format,
      String value) {}
}
