package com.igoryok95.ic2raddons.solar;

import ic2.api.energy.EnergyNet;

/**
 * Generation and storage stats for the eight solar panel tiers. Shared by block entities and item
 * tooltips.
 */
public enum SolarPanelType {
  ADVANCED(8, 1, 32_000, 1),
  HYBRID(64, 8, 100_000, 2),
  ULTIMATE(512, 64, 1_000_000, 3),
  QUANTUM(4096, 2048, 10_000_000, 5),
  SPECTRAL(8192, 4096, 100_000_000, 5),
  SINGULAR(32768, 16384, 1_000_000_000, 6),
  LIGHT_ABSORBING(131072, 65536, 1_000_000_000, 6),
  PHOTONIC(524288, 262144, 1_000_000_000, 6);

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

  public int getPacketCount() {
    return Math.max(1, (dayPower + getMaxOutput() - 1) / getMaxOutput());
  }
}
