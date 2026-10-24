package com.igoryok95.ic2raddons.solar;

import ic2.api.energy.EnergyNet;

/**
 * Generation and storage stats for the four solar panel tiers. Shared by block entities and item
 * tooltips.
 */
public enum SolarPanelType {
  ADVANCED(8, 1, 32_000, 1),
  HYBRID(64, 8, 100_000, 2),
  ULTIMATE(512, 64, 1_000_000, 3),
  QUANTUM(4096, 2048, 10_000_000, 5);

  public final int dayPower;
  public final int nightPower;
  public final int maxStorage;
  public final int tier;

  SolarPanelType(int dayPower, int nightPower, int maxStorage, int tier) {
    this.dayPower = dayPower;
    this.nightPower = nightPower;
    this.maxStorage = maxStorage;
    this.tier = tier;
  }

  public int getMaxOutput() {
    return (int) EnergyNet.instance.getPowerFromTier(this.tier);
  }
}
