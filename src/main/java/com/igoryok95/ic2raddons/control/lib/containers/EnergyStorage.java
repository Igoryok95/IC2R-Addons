// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.lib.containers;

public class EnergyStorage extends net.neoforged.neoforge.energy.EnergyStorage {

  public EnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
    super(capacity, maxReceive, maxExtract, energy);
  }

  public void setEnergy(int value) {
    energy = Math.max(0, Math.min(value, capacity));
  }

  public void setMax(int value) {
    maxReceive = value;
    maxExtract = value;
  }
}
