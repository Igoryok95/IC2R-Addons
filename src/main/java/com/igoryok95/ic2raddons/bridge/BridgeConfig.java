package com.igoryok95.ic2raddons.bridge;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BridgeConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLED;
  public static final ModConfigSpec.IntValue CAPACITY, INPUT_TIER, EU_PER_TICK;
  public static final ModConfigSpec.DoubleValue MJ_PER_EU;
  public static final ModConfigSpec.BooleanValue REQUIRE_REDSTONE;

  static {
    var b = new ModConfigSpec.Builder();
    ENABLED = b.translation("config.ic2r_addons.bridge.enabled").define("enabled", true);
    CAPACITY =
        b.translation("config.ic2r_addons.bridge.capacity")
            .defineInRange("euCapacity", 10000, 128, 100000000);
    INPUT_TIER =
        b.translation("config.ic2r_addons.bridge.tier").defineInRange("inputTier", 2, 1, 5);
    EU_PER_TICK =
        b.translation("config.ic2r_addons.bridge.rate").defineInRange("euPerTick", 32, 1, 8192);
    MJ_PER_EU =
        b.translation("config.ic2r_addons.bridge.ratio")
            .defineInRange("mjPerEu", 0.4, 0.000001, 100.0);
    REQUIRE_REDSTONE =
        b.translation("config.ic2r_addons.bridge.redstone").define("requireRedstone", true);
    SPEC = b.build();
  }

  private BridgeConfig() {}
}
