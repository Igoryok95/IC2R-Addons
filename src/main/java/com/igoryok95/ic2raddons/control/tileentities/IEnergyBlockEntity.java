// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

import net.minecraft.core.Direction;

public interface IEnergyBlockEntity {

  boolean canAcceptEnergy(Direction side);

  int getRequestedEnergy();

  int getSinkTier();

  int acceptEnergy(Direction directionFrom, int amount, int voltage);

  boolean canEmitEnergy(Direction side);

  void consumeEnergy(int amount);

  int getMaxEnergyOutput();

  int getProvidedEnergy();

  int getSourceTier();
}
