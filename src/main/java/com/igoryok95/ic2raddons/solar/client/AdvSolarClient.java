package com.igoryok95.ic2raddons.solar.client;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import com.igoryok95.ic2raddons.solar.gui.ProgressBars;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup: registers this addon's custom gauge styles for the dynamic GUIs. */
@EventBusSubscriber(modid = IC2RAddons.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AdvSolarClient {
  private AdvSolarClient() {}

  @SubscribeEvent
  public static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(ProgressBars::addStyles);
  }

  @SubscribeEvent
  public static void renderers(
      net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
    if (net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy")) {
      event.registerBlockEntityRenderer(
          com.igoryok95.ic2raddons.bridge.bc.BcContent.ENGINE_TILE.get(),
          com.igoryok95.ic2raddons.bridge.client.ElectricEngineRenderer::new);
    }
    event.registerBlockEntityRenderer(
        com.igoryok95.ic2raddons.solar.AdvSolarBlocks.BE_MOLECULAR_TRANSFORMER.get(),
        MolecularRenderer::new);
  }

  @SubscribeEvent
  public static void extensions(
      net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
    if (net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy")) {
      event.registerItem(
          new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
                getCustomRenderer() {
              if (renderer == null)
                renderer = new com.igoryok95.ic2raddons.bridge.client.EngineItemRenderer();
              return renderer;
            }
          },
          com.igoryok95.ic2raddons.bridge.bc.BcContent.ENGINE_ITEM.get());
    }
    event.registerItem(
        new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
          private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

          public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new MolecularItemRenderer();
            return renderer;
          }
        },
        com.igoryok95.ic2raddons.solar.AdvSolarBlocks.MOLECULAR_TRANSFORMER.get().asItem());
  }
}
