package com.igoryok95.ic2raddons.equipment.client;

import com.igoryok95.ic2raddons.equipment.GraviConfig;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.GraviSuite;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.PortalContent;
import com.igoryok95.ic2raddons.equipment.items.GraviArmor;
import com.igoryok95.ic2raddons.equipment.network.GraviNetwork;
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

@EventBusSubscriber(modid = GraviSuite.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GraviClient {
  public static final KeyMapping ENGINE =
      new KeyMapping(
          "key.ic2r_addons.engine",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_G,
          "key.categories.ic2r_addons");
  public static final KeyMapping MAGNET =
      new KeyMapping(
          "key.ic2r_addons.magnet",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_H,
          "key.categories.ic2r_addons");

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
                Component.translatable("hud.ic2r_addons.energy", percent),
                5,
                5,
                0x64deff);
            if (armor.kind == GraviArmor.Kind.GRAVI
                || armor.kind == GraviArmor.Kind.JETPACK
                || armor.kind == GraviArmor.Kind.NANO)
              graphics.drawString(
                  client.font,
                  Component.translatable(
                      "tooltip.ic2r_addons.engine",
                      Component.translatable(
                          ItemState.enabled(chest, "engine")
                              ? "ic2r_addons.on"
                              : "ic2r_addons.off")),
                  5,
                  17,
                  0x64deff);
          }
        });
  }

  @EventBusSubscriber(modid = GraviSuite.MODID, value = Dist.CLIENT)
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
