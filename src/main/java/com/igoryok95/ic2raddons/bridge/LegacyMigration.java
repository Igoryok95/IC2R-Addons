package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import java.nio.file.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class LegacyMigration {
  public static void aliases(RegisterEvent event) {
    var registry = event.getRegistry();
    for (var id : java.util.List.copyOf(registry.keySet())) {
      if (id.getNamespace().equals(IC2RAddons.MODID))
        registry.addAlias(
            ResourceLocation.fromNamespaceAndPath("solar_gravisuite", id.getPath()), id);
    }
    var quantum = IC2RAddons.id("quantum_solar_panel");
    if (registry.containsKey(quantum)) {
      for (String tier : java.util.List.of("spectral", "singular", "light_absorbing", "photonic")) {
        for (String namespace : java.util.List.of(IC2RAddons.MODID, "solar_gravisuite")) {
          registry.addAlias(
              ResourceLocation.fromNamespaceAndPath(namespace, tier + "_solar_panel"), quantum);
        }
      }
    }
  }

  public static void configs() {
    for (String part : new String[] {"solar", "equipment"}) {
      Path old = FMLPaths.CONFIGDIR.get().resolve("solar_gravisuite-" + part + ".toml");
      Path target = old.resolveSibling("ic2r_addons-" + part + ".toml");
      if (Files.isRegularFile(old) && !Files.exists(target)) {
        try {
          Files.copy(old, target);
        } catch (java.io.IOException e) {
          throw new IllegalStateException("Cannot migrate addon configuration: " + old, e);
        }
      }
    }
  }

  private LegacyMigration() {}
}
