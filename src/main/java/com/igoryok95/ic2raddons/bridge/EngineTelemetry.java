package com.igoryok95.ic2raddons.bridge;

import net.minecraft.nbt.CompoundTag;

/** No optional-mod types: safe for Jade to load without BuildCraft. */
public interface EngineTelemetry {
  CompoundTag telemetry();
}
