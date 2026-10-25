package com.igoryok95.ic2raddons.solar.gui;

import ic2.core.gui.Gauge;
import ic2.core.gui.Gauge.GaugePropertyBuilder.GaugeOrientation;
import ic2.core.gui.Gauge.GaugeStyle;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

/**
 * The custom gauge styles used by this addon's guidef GUIs (the molecular transformer progress
 * arrow and the solar panel energy bar). Registered during client setup via {@link #addStyles()}.
 */
public enum ProgressBars implements Gauge.IGaugeStyle {
  PROGRESS_MOLECULAR_TRANSFORMER(
      new Gauge.GaugePropertyBuilder(221, 7, 10, 15, GaugeOrientation.Down)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/molecular_transformer.png"))),
  PROGRESS_JEI_MOLECULAR_TRANSFORMER(
      new Gauge.GaugePropertyBuilder(176, 2, 12, 11, GaugeOrientation.Down)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/molecular_transformer_jei.png"))),
  ENGINE_CHARGE_0(
      new Gauge.GaugePropertyBuilder(176, 0, 4, 46, GaugeOrientation.Up)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/electricalengine.png"))),
  ENGINE_CHARGE_1(
      new Gauge.GaugePropertyBuilder(180, 0, 4, 46, GaugeOrientation.Up)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/electricalengine.png"))),
  ENGINE_CHARGE_2(
      new Gauge.GaugePropertyBuilder(184, 0, 4, 46, GaugeOrientation.Up)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/electricalengine.png"))),
  ENGINE_CHARGE_3(
      new Gauge.GaugePropertyBuilder(188, 0, 4, 46, GaugeOrientation.Up)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/electricalengine.png"))),
  ENGINE_CHARGE_4(
      new Gauge.GaugePropertyBuilder(192, 0, 4, 46, GaugeOrientation.Up)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/electricalengine.png"))),
  ENERGY_ADVANCED_SOLAR(
      new Gauge.GaugePropertyBuilder(195, 0, 24, 14, GaugeOrientation.Right)
          .withTexture(
              ResourceLocation.fromNamespaceAndPath(
                  "ic2r_addons", "textures/gui/advanced_solar_panel.png")));

  private final String name = this.name().toLowerCase(Locale.ENGLISH);
  private final Gauge.GaugeProperties properties;

  ProgressBars(Gauge.GaugePropertyBuilder properties) {
    this.properties = properties.build();
  }

  @Override
  public Gauge.GaugeProperties getProperties() {
    return this.properties;
  }

  private static boolean registered;

  public static synchronized void addStyles() {
    if (registered) return;
    for (ProgressBars bar : values()) {
      GaugeStyle.addStyle(bar.name, bar);
    }
    registered = true;
  }
}
