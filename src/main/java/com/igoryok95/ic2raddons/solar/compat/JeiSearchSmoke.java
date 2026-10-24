package com.igoryok95.ic2raddons.solar.compat;

import com.google.gson.GsonBuilder;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Real JEI regression checks, enabled exclusively by the isolated client smoke command. */
public final class JeiSearchSmoke {
  private static IJeiRuntime runtime;

  public static void verify(IJeiRuntime jei) {
    if (!Boolean.getBoolean("ic2r_addons.clientSmoke")
        && !Boolean.getBoolean("ic2r_addons.machinesSmoke")) return;
    runtime = jei;
    var filter = jei.getIngredientFilter();
    String previous = filter.getFilterText();
    Map<String, Object> result = new TreeMap<>();
    try {
      Set<String> expected =
          BuiltInRegistries.ITEM.keySet().stream()
              .filter(id -> id.getNamespace().equals(IC2RAddons.MODID))
              .map(Object::toString)
              .collect(Collectors.toCollection(TreeSet::new));
      if (expected.size() < 40) throw new IllegalStateException("Combined item set is incomplete");
      for (String query : List.of("@ic2r_addons", "@addons", "@IC2R", "@Addons")) {
        filter.setFilterText(query);
        Set<String> actual =
            filter.getFilteredItemStacks().stream()
                .map(ItemStack::getItem)
                .map(BuiltInRegistries.ITEM::getKey)
                .map(Object::toString)
                .collect(Collectors.toCollection(TreeSet::new));
        result.put(query, actual);
        if (!expected.equals(actual))
          throw new IllegalStateException("JEI search mismatch: " + query);
      }
      result.put("success", true);
      result.put("unique_items", expected.size());
      result.put("mod_id", IC2RAddons.MODID);
      IC2RAddons.LOGGER.info("IC2R_ADDONS_JEI_SEARCH_PASS items={} queries={}", expected.size(), 4);
    } catch (RuntimeException error) {
      result.put("success", false);
      result.put("error", error.toString());
      throw error;
    } finally {
      filter.setFilterText(previous);
      try {
        var path =
            Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("screenshots/jei-search-smoke.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(result));
      } catch (java.io.IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public static void show(String query) {
    if (runtime == null) throw new IllegalStateException("JEI smoke runtime was not initialized");
    var mc = Minecraft.getInstance();
    mc.setScreen(new InventoryScreen(java.util.Objects.requireNonNull(mc.player)));
    runtime.getIngredientFilter().setFilterText(query);
  }

  private JeiSearchSmoke() {}
}
