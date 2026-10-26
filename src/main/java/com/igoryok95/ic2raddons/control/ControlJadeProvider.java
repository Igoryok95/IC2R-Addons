// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.tileentities.*;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public final class ControlJadeProvider
    implements IServerDataProvider<BlockAccessor>, IComponentProvider<BlockAccessor> {
  @Override
  public ResourceLocation getUid() {
    return IC2RAddons.id("control");
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof TileEntityEnergyCounter counter) {
      tag.putDouble("controlTotal", counter.total());
      tag.putDouble("controlAverage", counter.average());
      tag.putDouble("controlCurrent", counter.current());
      tag.putInt("controlPacket", counter.maximumPacket());
    }
    if (accessor.getBlockEntity() instanceof TileEntityThermalMonitor monitor) {
      tag.putInt("controlThreshold", monitor.getHeatLevel());
      tag.putBoolean("controlPowered", monitor.getPowered());
    }
  }

  @Override
  public void appendTooltip(ITooltip tip, BlockAccessor accessor, IPluginConfig config) {
    var tag = accessor.getServerData();
    if (tag.contains("controlTotal")) {
      tip.add(
          Component.translatable(
              "message.ic2r_addons.control.counter.total",
              String.format(Locale.ROOT, "%.2f", tag.getDouble("controlTotal"))));
      tip.add(
          Component.translatable(
              "message.ic2r_addons.control.counter.average",
              String.format(Locale.ROOT, "%.2f", tag.getDouble("controlAverage"))));
      tip.add(
          Component.translatable(
              "message.ic2r_addons.control.counter.current",
              String.format(Locale.ROOT, "%.2f", tag.getDouble("controlCurrent"))));
      tip.add(
          Component.translatable(
              "message.ic2r_addons.control.counter.tier", tag.getInt("controlPacket")));
    }
    if (tag.contains("controlThreshold")) {
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.control.threshold", tag.getInt("controlThreshold")));
      tip.add(
          Component.translatable(
              "message.ic2r_addons.control.InfoPanelBlockPowered",
              Component.translatable(tag.getBoolean("controlPowered") ? "gui.yes" : "gui.no")));
    }
  }
}
