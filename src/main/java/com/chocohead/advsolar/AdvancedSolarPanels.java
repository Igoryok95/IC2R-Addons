package com.chocohead.advsolar;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Advanced Solar Panels — advanced/hybrid/ultimate/quantum solar panels, the molecular transformer,
 * the quantum generator and chargeable solar helmets. Ported from the classic 1.12.2 IC2 addon to
 * Minecraft 1.21.1 / IC2R.
 */
@Mod(AdvancedSolarPanels.MODID)
public final class AdvancedSolarPanels {
  public static final String MODID = "advanced_solar_panels";

  public static net.minecraft.resources.ResourceLocation id(String path) {
    return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, path);
  }

  public static final Logger LOGGER = LogUtils.getLogger();

  public AdvancedSolarPanels(IEventBus modBus, net.neoforged.fml.ModContainer container) {
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.COMMON,
        AspConfig.SPEC,
        "advanced_solar_panels-common.toml");
    reforged.mods.gravisuite.GraviSuite.initialize(modBus, container);
    AdvSolarBlocks.register(modBus);
    com.chocohead.advsolar.recipe.MolecularTransformerRecipes.TYPES.register(modBus);
    com.chocohead.advsolar.recipe.MolecularTransformerRecipes.SERIALIZERS.register(modBus);
    modBus.addListener(this::commonSetup);
  }

  private void commonSetup(FMLCommonSetupEvent event) {

    LOGGER.info("Solar GraviSuite {} loaded.", MODID);
  }
}
