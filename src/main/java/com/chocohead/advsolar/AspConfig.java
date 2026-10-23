package com.chocohead.advsolar;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AspConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.DoubleValue GENERATION;
  public static final ModConfigSpec.BooleanValue ARMOR_ONLY;
  public static final ModConfigSpec.BooleanValue ACTIVE_CORE;

  static {
    var b = new ModConfigSpec.Builder();
    GENERATION =
        b.translation("config.advanced_solar_panels.generation_multiplier")
            .defineInRange("generationMultiplier", 1.0, 0.0, 64.0);
    ARMOR_ONLY =
        b.translation("config.advanced_solar_panels.armor_only")
            .define("helmetsChargeArmorOnly", false);
    ACTIVE_CORE =
        b.translation("config.advanced_solar_panels.active_core")
            .define("drawActiveTransformerCore", true);
    SPEC = b.build();
  }

  private AspConfig() {}
}
