package reforged.mods.gravisuite;

import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import reforged.mods.gravisuite.network.GraviNetwork;

public final class GraviSuite {
  public static final String ID = com.chocohead.advsolar.AdvancedSolarPanels.MODID;
  public static final String MODID = com.chocohead.advsolar.AdvancedSolarPanels.MODID;
  public static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

  static {
    TABS.register(
        "equipment",
        () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.solar_gravisuite.equipment"))
                .icon(() -> new ItemStack(GraviItems.GRAVI_CHESTPLATE.get()))
                .displayItems(
                    (parameters, output) ->
                        GraviItems.ITEMS
                            .getEntries()
                            .forEach(
                                entry -> {
                                  Item item = entry.get();
                                  output.accept(item);
                                  if (item instanceof IElectricItem)
                                    output.accept(
                                        ElectricItemManager.getCharged(
                                            item, Double.POSITIVE_INFINITY));
                                }))
                .build());
  }

  public static void initialize(IEventBus bus, ModContainer container) {
    GraviItems.ITEMS.register(bus);
    PortalContent.BLOCKS.register(bus);
    PortalContent.BLOCK_ENTITIES.register(bus);
    PortalContent.ENTITIES.register(bus);
    GraviNetwork.MENUS.register(bus);
    TABS.register(bus);
    bus.addListener(GraviNetwork::register);
    container.registerConfig(
        ModConfig.Type.COMMON, GraviConfig.SPEC, "solar_gravisuite-equipment.toml");
    NeoForge.EVENT_BUS.addListener(GraviEvents::tick);
    NeoForge.EVENT_BUS.addListener(GraviEvents::fall);
  }

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(ID, path);
  }
}
