package com.chocohead.advsolar.client;

import com.chocohead.advsolar.AdvancedSolarPanels;
import com.chocohead.advsolar.gui.ProgressBars;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup: registers this addon's custom gauge styles for the dynamic GUIs. */
@EventBusSubscriber(
    modid = AdvancedSolarPanels.MODID,
    bus = EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class AdvSolarClient {
  private AdvSolarClient() {}

  @SubscribeEvent
  public static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(ProgressBars::addStyles);
  }

  @SubscribeEvent
  public static void renderers(
      net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(
        com.chocohead.advsolar.AdvSolarBlocks.BE_MOLECULAR_TRANSFORMER.get(),
        MolecularRenderer::new);
  }

  @SubscribeEvent
  public static void extensions(
      net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
    event.registerItem(
        new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
          private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

          public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new MolecularItemRenderer();
            return renderer;
          }
        },
        com.chocohead.advsolar.AdvSolarBlocks.MOLECULAR_TRANSFORMER.get().asItem());
  }
}
