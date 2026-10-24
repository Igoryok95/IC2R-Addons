// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.config;

import java.util.UUID;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ConfigHandler {
  public static final String CATEGORY_GENERAL = "General";

  public static ModConfigSpec COMMON_CONFIG;

  public static ModConfigSpec.IntValue HOWLER_ALARM_RANGE;
  public static ModConfigSpec.IntValue MAX_ALARM_RANGE;
  public static ModConfigSpec.ConfigValue<String> ALLOWED_ALARMS;
  public static ModConfigSpec.IntValue SCREEN_REFRESH_PERIOD;
  public static ModConfigSpec.IntValue RANGE_TRIGGER_REFRESH_PERIOD;
  public static ModConfigSpec.IntValue THERMAL_MONITOR_REFRESH_PERIOD;
  public static ModConfigSpec.BooleanValue USE_CUSTOM_SOUNDS;
  public static ModConfigSpec.IntValue ALARM_PAUSE;
  public static ModConfigSpec.BooleanValue DISABLE_RANGE_CHECK;
  public static ModConfigSpec.BooleanValue WS_ENABLED;
  public static ModConfigSpec.ConfigValue<String> WS_HOST;
  public static ModConfigSpec.IntValue WS_PORT;
  public static ModConfigSpec.ConfigValue<String> WS_TOKEN;
  public static ModConfigSpec.IntValue WS_REFRESH_RATE;
  public static ModConfigSpec.ConfigValue<String> WS_SERVER_ID;
  public static ModConfigSpec.ConfigValue<String> SERVER_LANGUAGE;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    // builder.comment("General settings").push(CATEGORY_GENERAL);
    HOWLER_ALARM_RANGE =
        builder
            .comment("Default Howler Alarm Range")
            .translation("config.ic2r_addons.control.howlerAlarmRange")
            .defineInRange("howlerAlarmRange", 64, 0, Integer.MAX_VALUE);
    MAX_ALARM_RANGE =
        builder
            .comment("Max Possible Howler Alarm Range")
            .translation("config.ic2r_addons.control.maxAlarmRange")
            .defineInRange("maxAlarmRange", 128, 0, Integer.MAX_VALUE);
    ALLOWED_ALARMS =
        builder
            .comment("Allowed Alarms")
            .translation("config.ic2r_addons.control.allowedAlarms")
            .define("allowedAlarms", "default,sci-fi,siren");
    SCREEN_REFRESH_PERIOD =
        builder
            .comment("Default Panel Refresh Period")
            .translation("config.ic2r_addons.control.screenRefreshPeriod")
            .defineInRange("screenRefreshPeriod", 20, 1, Integer.MAX_VALUE);
    RANGE_TRIGGER_REFRESH_PERIOD =
        builder
            .comment("Default Range Trigger Refresh Period")
            .translation("config.ic2r_addons.control.rangeTriggerRefreshPeriod")
            .defineInRange("rangeTriggerRefreshPeriod", 20, 1, Integer.MAX_VALUE);
    THERMAL_MONITOR_REFRESH_PERIOD =
        builder
            .comment("Default Thermal Monitor Refresh Period")
            .translation("config.ic2r_addons.control.thermalMonitorRefreshPeriod")
            .defineInRange("thermalMonitorRefreshPeriod", 20, 1, Integer.MAX_VALUE);
    USE_CUSTOM_SOUNDS =
        builder
            .comment("Load Custom Sounds")
            .translation("config.ic2r_addons.control.useCustomSounds")
            .define("useCustomSounds", false);
    ALARM_PAUSE =
        builder
            .comment("Alarm Pause in Ticks")
            .translation("config.ic2r_addons.control.alarmPause")
            .defineInRange("alarmPause", 60, 10, 2000);
    DISABLE_RANGE_CHECK =
        builder
            .comment("Disable Range Check")
            .translation("config.ic2r_addons.control.disableRangeCheck")
            .define("disableRangeCheck", false);

    WS_ENABLED =
        builder
            .comment("Web Socket Enabled")
            .translation("config.ic2r_addons.control.wsEnabled")
            .define("wsEnabled", false);
    WS_HOST =
        builder
            .comment("Web Socket Host")
            .translation("config.ic2r_addons.control.wsHost")
            .define("wsHost", "localhost");
    WS_PORT =
        builder
            .comment("Web Socket Port")
            .translation("config.ic2r_addons.control.wsPort")
            .defineInRange("wsPort", 8000, 0, 65535);
    WS_TOKEN =
        builder
            .comment("Web Socket Token")
            .translation("config.ic2r_addons.control.wsToken")
            .define("wsToken", "");
    WS_REFRESH_RATE =
        builder
            .comment("Web Socket Refresh Rate")
            .translation("config.ic2r_addons.control.wsRefreshRate")
            .defineInRange("wsRefreshRate", 100, 10, 20000);
    WS_SERVER_ID =
        builder
            .comment("Web Socket Server ID")
            .translation("config.ic2r_addons.control.wsServerID")
            .define("wsServerID", UUID.randomUUID().toString());
    SERVER_LANGUAGE =
        builder
            .comment(
                "Language of the text created on a dedicated server, for example en_us or ru_ru")
            .translation("config.ic2r_addons.control.serverLanguage")
            .define("serverLanguage", "en_us");
    // builder.pop();
    COMMON_CONFIG = builder.build();
  }
}
