package reforged.mods.gravisuite.client;

import com.mojang.blaze3d.platform.InputConstants;
import ic2.api.item.ElectricItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import reforged.mods.gravisuite.GraviConfig;
import reforged.mods.gravisuite.GraviItems;
import reforged.mods.gravisuite.GraviSuite;
import reforged.mods.gravisuite.ItemState;
import reforged.mods.gravisuite.PortalContent;
import reforged.mods.gravisuite.items.GraviArmor;
import reforged.mods.gravisuite.network.GraviNetwork;

@EventBusSubscriber(modid = GraviSuite.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GraviClient {
  public static final KeyMapping ENGINE =
      new KeyMapping(
          "key.gravisuite.engine",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_G,
          "key.categories.gravisuite");
  public static final KeyMapping MAGNET =
      new KeyMapping(
          "key.gravisuite.magnet",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_H,
          "key.categories.gravisuite");

  @SubscribeEvent
  public static void keys(RegisterKeyMappingsEvent event) {
    event.register(ENGINE);
    event.register(MAGNET);
  }

  @SubscribeEvent
  public static void screens(RegisterMenuScreensEvent event) {
    event.register(GraviNetwork.RELOCATOR_MENU.get(), RelocatorScreen::new);
  }

  @SubscribeEvent
  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(
        PortalContent.RELOCATOR_BALL.get(),
        context -> new ThrownItemRenderer<>(context, 0.6F, true));
  }

  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          ItemBlockRenderTypes.setRenderLayer(PortalContent.PORTAL.get(), RenderType.translucent());
          ItemProperties.register(
              GraviItems.GRAVITOOL.get(),
              GraviSuite.id("mode"),
              (stack, level, entity, seed) -> ItemState.mode(stack, "tool", 4));
        });
  }

  @SubscribeEvent
  public static void hud(RegisterGuiLayersEvent event) {
    event.registerAboveAll(
        GraviSuite.id("hud"),
        (graphics, delta) -> {
          Minecraft client = Minecraft.getInstance();
          if (client.player == null || client.options.hideGui || !GraviConfig.HUD.get()) return;
          var chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
          if (chest.getItem() instanceof GraviArmor armor) {
            int percent =
                (int) (100 * ElectricItem.manager.getCharge(chest) / armor.getMaxCharge(chest));
            graphics.drawString(
                client.font,
                Component.translatable("hud.gravisuite.energy", percent),
                5,
                5,
                0x64deff);
            if (armor.kind == GraviArmor.Kind.GRAVI
                || armor.kind == GraviArmor.Kind.JETPACK
                || armor.kind == GraviArmor.Kind.NANO)
              graphics.drawString(
                  client.font,
                  Component.translatable(
                      "tooltip.gravisuite.engine",
                      Component.translatable(
                          ItemState.enabled(chest, "engine") ? "gravisuite.on" : "gravisuite.off")),
                  5,
                  17,
                  0x64deff);
          }
        });
  }

  @EventBusSubscriber(modid = GraviSuite.ID, value = Dist.CLIENT)
  public static final class Ticks {
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.screen != null) return;
      while (ENGINE.consumeClick()) send(0, 0, "");
      while (MAGNET.consumeClick()) send(1, 0, "");
    }
  }

  public static void send(int action, int index, String text) {
    PacketDistributor.sendToServer(new GraviNetwork.Action(action, index, text));
  }
}
