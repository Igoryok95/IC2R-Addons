package com.igoryok95.ic2raddons.solar;

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
@Mod(IC2RAddons.MODID)
public final class IC2RAddons {
  public static final String MODID = "ic2r_addons";

  public static net.minecraft.resources.ResourceLocation id(String path) {
    return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, path);
  }

  public static final Logger LOGGER = LogUtils.getLogger();

  public IC2RAddons(IEventBus modBus, net.neoforged.fml.ModContainer container) {
    com.igoryok95.ic2raddons.bridge.LegacyMigration.configs();
    modBus.addListener(
        net.neoforged.bus.api.EventPriority.LOWEST,
        com.igoryok95.ic2raddons.bridge.LegacyMigration::aliases);
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.COMMON,
        com.igoryok95.ic2raddons.bridge.BridgeConfig.SPEC,
        "ic2r_addons-buildcraft.toml");
    if (net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy")) {
      com.igoryok95.ic2raddons.bridge.bc.BcContent.register(modBus);
      com.igoryok95.ic2raddons.bridge.bc.WrenchBridge.register();
      com.igoryok95.ic2raddons.bridge.bc.WrenchBridge.registerAdapter();
    }
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.COMMON, AspConfig.SPEC, "ic2r_addons-solar.toml");
    com.igoryok95.ic2raddons.equipment.GraviSuite.initialize(modBus, container);
    AdvSolarBlocks.register(modBus);
    new com.igoryok95.ic2raddons.control.EnergyControl(modBus, container);
    com.igoryok95.ic2raddons.machines.AdvMachinesBlocks.register(modBus);
    com.igoryok95.ic2raddons.solar.recipe.MolecularTransformerRecipes.TYPES.register(modBus);
    com.igoryok95.ic2raddons.solar.recipe.MolecularTransformerRecipes.SERIALIZERS.register(modBus);
    modBus.addListener(this::commonSetup);
  }

  private void commonSetup(FMLCommonSetupEvent event) {

    LOGGER.info("IC2R Addons {} loaded.", MODID);
  }
}
