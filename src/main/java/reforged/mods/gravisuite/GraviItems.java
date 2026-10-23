package reforged.mods.gravisuite;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import reforged.mods.gravisuite.items.GraviArmor;
import reforged.mods.gravisuite.items.GraviTool;
import reforged.mods.gravisuite.items.MiningTool;
import reforged.mods.gravisuite.items.UtilityItem;
import reforged.mods.gravisuite.items.Relocator;
import reforged.mods.gravisuite.items.AdvancedChainsaw;

public final class GraviItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GraviSuite.ID);
    public static final DeferredItem<Item> SUPERCONDUCTOR_COVER = component("superconductor_cover");
    public static final DeferredItem<Item> SUPERCONDUCTOR = component("superconductor");
    public static final DeferredItem<Item> COOLING_CORE = component("cooling_core");
    public static final DeferredItem<Item> GRAVI_ENGINE = component("gravi_engine");
    public static final DeferredItem<Item> MAGNETRON = component("magnetron");
    public static final DeferredItem<Item> VAJRA_CORE = component("vajra_core");
    public static final DeferredItem<Item> ENGINE_BOOSTER = component("engine_booster");
    public static final DeferredItem<GraviArmor> ADVANCED_LAPPACK = ITEMS.register("advanced_lappack", () -> new GraviArmor(GraviArmor.Kind.ADVANCED_LAPPACK));
    public static final DeferredItem<GraviArmor> ULTIMATE_LAPPACK = ITEMS.register("ultimate_lappack", () -> new GraviArmor(GraviArmor.Kind.ULTIMATE_LAPPACK));
    public static final DeferredItem<GraviArmor> ADVANCED_JETPACK = ITEMS.register("advanced_jetpack", () -> new GraviArmor(GraviArmor.Kind.JETPACK));
    public static final DeferredItem<GraviArmor> ADVANCED_NANO = ITEMS.register("advanced_nano", () -> new GraviArmor(GraviArmor.Kind.NANO));
    public static final DeferredItem<GraviArmor> GRAVI_CHESTPLATE = ITEMS.register("advanced_quant", () -> new GraviArmor(GraviArmor.Kind.GRAVI));
    public static final DeferredItem<MiningTool> ADVANCED_DRILL = ITEMS.register("advanced_diamond_drill", () -> new MiningTool(false));
    public static final DeferredItem<MiningTool> VAJRA = ITEMS.register("vajra", () -> new MiningTool(true));
    public static final DeferredItem<AdvancedChainsaw> ADVANCED_CHAINSAW = ITEMS.register("advanced_chainsaw", AdvancedChainsaw::new);
    public static final DeferredItem<GraviTool> GRAVITOOL = ITEMS.register("gravitool", GraviTool::new);
    public static final DeferredItem<UtilityItem> MAGNET = ITEMS.register("magnet", () -> new UtilityItem(false));
    public static final DeferredItem<UtilityItem> VOIDER = ITEMS.register("voider", () -> new UtilityItem(true));
    public static final DeferredItem<Relocator> RELOCATOR = ITEMS.register("relocator", Relocator::new);
    private static DeferredItem<Item> component(String name) { return ITEMS.registerSimpleItem(name); }
    private GraviItems() {}
}
