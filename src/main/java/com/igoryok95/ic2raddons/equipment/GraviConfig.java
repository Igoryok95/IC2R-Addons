package com.igoryok95.ic2raddons.equipment;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GraviConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.IntValue MAGNET_RANGE;
  public static final ModConfigSpec.IntValue VEIN_LIMIT;
  public static final ModConfigSpec.IntValue TREE_LIMIT;
  public static final ModConfigSpec.IntValue PORTAL_LIFETIME;
  public static final ModConfigSpec.BooleanValue HUD;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    MAGNET_RANGE =
        builder
            .translation("config.ic2r_addons.magnet_range")
            .defineInRange("magnetRange", 8, 1, 16);
    VEIN_LIMIT =
        builder.translation("config.ic2r_addons.vein_limit").defineInRange("veinLimit", 64, 1, 256);
    TREE_LIMIT =
        builder
            .translation("config.ic2r_addons.tree_limit")
            .defineInRange("treeLimit", 128, 1, 512);
    PORTAL_LIFETIME =
        builder
            .translation("config.ic2r_addons.portal_lifetime")
            .defineInRange("portalLifetimeTicks", 400, 20, 1200);
    HUD = builder.translation("config.ic2r_addons.hud").define("showHud", true);
    SPEC = builder.build();
  }

  private GraviConfig() {}
}
