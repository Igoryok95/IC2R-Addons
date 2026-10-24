// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.config.ConfigHandler;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.init.ModCapabilities;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.network.ChannelHandler;
import com.igoryok95.ic2raddons.control.tileentities.ScreenManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnergyControl {
  public static final String NAME = "IC2R Addons";
  public static final String MODID = "ic2r_addons";
  public static final String VERSION = "1.4.0";

  public static EnergyControl INSTANCE;
  public static final Logger LOGGER = LogManager.getLogger(MODID);
  public static final ScreenManager screenManager = new ScreenManager();

  public List<String> availableAlarms; // on client
  public List<String> serverAllowedAlarms; // will be loaded from server
  public static Map<Player, Boolean> altPressed = new HashMap<Player, Boolean>();

  public EnergyControl(IEventBus modEventBus, ModContainer container) {
    INSTANCE = this;
    modEventBus.addListener(ChannelHandler::register);
    modEventBus.addListener(ModCapabilities::register);
    modEventBus.addListener(CrossModLoader::onRegister);
    ModItems.BLOCKS.register(modEventBus);
    ModItems.ITEMS.register(modEventBus);
    ModItems.RECIPE_TYPES.register(modEventBus);
    ModItems.RECIPE_SERIALIZERS.register(modEventBus);
    ECCreativeTab.CREATIVE_TABS.register(modEventBus);
    ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
    ModTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
    NeoForge.EVENT_BUS.register(ServerTickHandler.instance);
    container.registerConfig(
        ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG, "ic2r_addons-control.toml");
    CrossModLoader.init();
  }
}
