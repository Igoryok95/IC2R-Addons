package com.igoryok95.ic2raddons.equipment.client;

import com.igoryok95.ic2raddons.equipment.EquipmentTooltips;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.mojang.blaze3d.platform.InputConstants;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Items;
import ic2.core.util.KeyboardClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** One opt-in worn-armor and live-binding pass; never saves rebound keys to user options. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class EquipmentPresentationClientSmoke {
  private static int ticks, locale;
  private static CompletableFuture<Void> pending;
  private static final Map<KeyMapping, InputConstants.Key> original = new LinkedHashMap<>();

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.equipmentPresentationSmoke")) return;
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
    if (phase == 1 && locale == 0) {
      mc.options.guiScale().set(2);
      mc.resizeDisplay();
      var id = mc.player.getUUID();
      pending =
          CompletableFuture.runAsync(
              () -> {
                var player = server.getPlayerList().getPlayer(id);
                player.setGameMode(GameType.CREATIVE);
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                var level = player.serverLevel();
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                for (var pos : BlockPos.betweenClosed(97, 126, 94, 107, 133, 104))
                  level.setBlockAndUpdate(
                      pos,
                      pos.getY() == 126
                          ? Blocks.SMOOTH_STONE.defaultBlockState()
                          : Blocks.AIR.defaultBlockState());
                for (int i = 0; i < 2; i++) {
                  var stand = new ArmorStand(level, 100 + i * 2.5, 128, 100);
                  stand.setNoGravity(true);
                  stand.setShowArms(true);
                  stand.setYRot(180);
                  stand.yBodyRot = stand.yBodyRotO = 180;
                  stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Ic2Items.NANO_HELMET));
                  stand.setItemSlot(
                      EquipmentSlot.CHEST,
                      new ItemStack(
                          i == 0 ? Ic2Items.NANO_CHESTPLATE : GraviItems.ADVANCED_NANO.get()));
                  stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Ic2Items.NANO_LEGGINGS));
                  stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Ic2Items.NANO_BOOTS));
                  level.addFreshEntity(stand);
                }
                player.connection.teleport(101.25, 128, 96.5, 0, 0);
              },
              server);
      language(mc, "ru_ru");
    }
    if (phase == 25) {
      verifyTooltipsAndBindings(mc);
      mc.options.hideGui = true;
    }
    if (locale == 0 && phase == 35) capture(mc, "equipment-armor-front");
    if (locale == 0 && phase == 37) camera(mc, 101.25, 131, 98, 55);
    if (locale == 0 && phase == 48) capture(mc, "equipment-armor-shoulders");
    if (locale == 0 && phase == 50) camera(mc, 101.25, 127, 97, -20);
    if (locale == 0 && phase == 61) capture(mc, "equipment-armor-wrists");
    if (phase == 65) {
      mc.options.hideGui = false;
      mc.setScreen(new EquipmentScreen(new ItemStack(GraviItems.ADVANCED_NANO.get())));
    }
    if (phase == 70) capture(mc, "equipment-tooltip-" + mc.options.languageCode);
    if (phase == 73) {
      mc.setScreen(new KeyBindsScreen(null, mc.options));
      var field = KeyBindsScreen.class.getDeclaredField("keyBindsList");
      field.setAccessible(true);
      var list = (KeyBindsList) field.get(mc.screen);
      for (var entry : list.children()) {
        if (!(entry instanceof KeyBindsList.KeyEntry)) continue;
        var keyField = entry.getClass().getDeclaredField("key");
        keyField.setAccessible(true);
        if (keyField.get(entry) == GraviClient.ENGINE)
          list.setScrollAmount(list.children().indexOf(entry) * 20.0 - 35);
      }
    }
    if (phase == 76) {
      var screen = (KeyBindsScreen) mc.screen;
      var field = KeyBindsScreen.class.getDeclaredField("keyBindsList");
      field.setAccessible(true);
      var list = (KeyBindsList) field.get(screen);
      for (var entry : list.children()) {
        if (!(entry instanceof KeyBindsList.KeyEntry)) continue;
        var keyField = entry.getClass().getDeclaredField("key");
        keyField.setAccessible(true);
        var key = (KeyMapping) keyField.get(entry);
        if (key != GraviClient.MAGNET && key != GraviClient.ENGINE) continue;
        var buttonField = entry.getClass().getDeclaredField("changeButton");
        buttonField.setAccessible(true);
        var button = (net.minecraft.client.gui.components.Button) buttonField.get(entry);
        require(
            list.getRowLeft() + mc.font.width(Component.translatable(key.getName())) + 8
                < button.getX(),
            "key label overlaps binding: " + key.getName());
      }
    }
    if (phase == 80) capture(mc, "equipment-key-settings-" + mc.options.languageCode);
    if (phase == 85) {
      mc.setScreen(null);
      if (++locale == 1) {
        ticks = 1;
        language(mc, "en_us");
      } else {
        LoggerFactory.getLogger("IC2R-Equipment-Presentation")
            .info(
                "IC2R_EQUIPMENT_PRESENTATION_PASS: all equipment tooltips gray, live rebound/unbound keys, native key-list spacing, worn Nano armor, RU/EN");
        mc.stop();
      }
    }
  }

  private static void verifyTooltipsAndBindings(Minecraft mc) {
    int checked = 0;
    for (var item : BuiltInRegistries.ITEM) {
      var id = BuiltInRegistries.ITEM.getKey(item);
      if (!id.getNamespace().equals("ic2r_addons") || !(item instanceof ic2.api.item.IElectricItem))
        continue;
      var lines = new ArrayList<Component>();
      var stack = new ItemStack(item);
      item.appendHoverText(stack, Item.TooltipContext.of(mc.level), lines, TooltipFlag.NORMAL);
      for (var line : lines) {
        require(!line.getString().contains("tooltip.ic2r_addons"), "missing translation: " + id);
        if (line.getContents() instanceof TranslatableContents content
            && content.getKey().startsWith("tooltip.ic2r_addons"))
          require(
              ChatFormatting.GRAY.getColor().equals(line.getStyle().getColor().getValue()),
              "white equipment description: " + id);
      }
      checked++;
    }
    try {
      rebind(GraviClient.ENGINE, "key.keyboard.p");
      rebind(GraviClient.MAGNET, "key.keyboard.l");
      rebind(KeyboardClient.modeSwitchKey, "key.keyboard.k");
      rebind(KeyboardClient.altKey, "key.keyboard.o");
      rebind(mc.options.keyUse, "key.mouse.4");
      rebind(mc.options.keyShift, "key.keyboard.right.shift");
      rebind(mc.options.keyJump, "key.keyboard.j");
      var hints =
          Map.of(
              "engine_key", List.of(GraviClient.ENGINE),
              "hover_keys", List.of(mc.options.keyJump, KeyboardClient.modeSwitchKey),
              "mining_keys",
                  List.of(KeyboardClient.modeSwitchKey, mc.options.keyUse, KeyboardClient.altKey),
              "enchantment_keys",
                  List.of(mc.options.keyShift, KeyboardClient.modeSwitchKey, mc.options.keyUse),
              "mode_key", List.of(KeyboardClient.modeSwitchKey, mc.options.keyUse),
              "voider_keys", List.of(mc.options.keyUse, mc.options.keyShift),
              "magnet_keys", List.of(mc.options.keyUse, GraviClient.MAGNET),
              "relocator_keys",
                  List.of(mc.options.keyShift, mc.options.keyUse, KeyboardClient.modeSwitchKey),
              "tree_keys",
                  List.of(mc.options.keyShift, KeyboardClient.modeSwitchKey, mc.options.keyUse));
      for (var entry : hints.entrySet())
        for (var key : entry.getValue())
          require(
              EquipmentTooltips.hint("tooltip.ic2r_addons." + entry.getKey())
                  .getString()
                  .contains(key.getTranslatedKeyMessage().getString()),
              "stale key hint: " + entry.getKey());
      var nativeLines = new ArrayList<Component>();
      Ic2Items.NANO_HELMET.appendHoverText(
          new ItemStack(Ic2Items.NANO_HELMET),
          Item.TooltipContext.of(mc.level),
          nativeLines,
          TooltipFlag.NORMAL);
      var nativeText = nativeLines.toString();
      require(
          nativeLines.stream()
              .anyMatch(
                  line ->
                      line.getString()
                              .contains(KeyboardClient.altKey.getTranslatedKeyMessage().getString())
                          && line.getString()
                              .contains(
                                  KeyboardClient.modeSwitchKey
                                      .getTranslatedKeyMessage()
                                      .getString())),
          "native helmet key hint is stale: " + nativeText);
      GraviClient.ENGINE.setKey(InputConstants.UNKNOWN);
      require(
          EquipmentTooltips.hint("tooltip.ic2r_addons.engine_key")
              .getString()
              .contains(GraviClient.ENGINE.getTranslatedKeyMessage().getString()),
          "unbound engine key uses a default label");
    } finally {
      original.forEach(KeyMapping::setKey);
      original.clear();
      KeyMapping.resetMapping();
    }
    LoggerFactory.getLogger("IC2R-Equipment-Presentation")
        .info(
            "EQUIPMENT_LOCALE_CHECK: {} {} electric items, nine live key hints, native helmet and unbound engine",
            mc.options.languageCode,
            checked);
  }

  private static void rebind(KeyMapping key, String name) {
    original.put(key, key.getKey());
    key.setKey(InputConstants.getKey(name));
  }

  private static void camera(Minecraft mc, double x, double y, double z, float pitch) {
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    pending =
        CompletableFuture.runAsync(
            () -> server.getPlayerList().getPlayer(id).connection.teleport(x, y, z, 0, pitch),
            server);
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

  private static void capture(Minecraft mc, String file) {
    Screenshot.grab(mc.gameDirectory, file + ".png", mc.getMainRenderTarget(), ignored -> {});
  }

  private static void require(boolean valid, String error) {
    if (!valid) throw new IllegalStateException(error);
  }

  private static final class EquipmentScreen extends Screen {
    private final ItemStack stack;

    private EquipmentScreen(ItemStack stack) {
      super(Component.empty());
      this.stack = ElectricItemManager.getCharged(stack.getItem(), 1_000_000);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTick) {
      graphics.fill(0, 0, width, height, 0xCC20252A);
      graphics.renderItem(stack, 25, 25);
      graphics.renderTooltip(font, stack, 35, 65);
    }
  }

  private EquipmentPresentationClientSmoke() {}
}
