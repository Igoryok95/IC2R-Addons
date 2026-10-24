package com.igoryok95.ic2raddons.machines.compat;

import com.igoryok95.ic2raddons.machines.te.*;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public final class MachineJadeProvider
    implements IServerDataProvider<BlockAccessor>, IComponentProvider<BlockAccessor> {
  @Override
  public ResourceLocation getUid() {
    return IC2RAddons.id("advanced_machines");
  }

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    if (!(accessor.getBlockEntity() instanceof TileEntityHeatingMachine machine)) return;
    var n = new CompoundTag();
    n.putInt("heat", machine.getRawHeat() / 100);
    n.putInt("progress", machine.progress * 100 / machine.getMaxProgress());
    n.putDouble("eu", machine.lastEuPerTick);
    if (machine instanceof TileEntityHeatingWaterMachine water) {
      n.putInt("water", water.getWaterAmount());
      n.putBoolean(
          "danger",
          machine instanceof TileEntityThermalWasher
              && water.getWaterAmount() == 0
              && machine.getRawHeat() >= 5000);
    }
    data.put("ic2r_addons_machine", n);
  }

  @Override
  public void appendTooltip(ITooltip tip, BlockAccessor accessor, IPluginConfig config) {
    if (!accessor.getServerData().contains("ic2r_addons_machine")) return;
    var n = accessor.getServerData().getCompound("ic2r_addons_machine");
    tip.add(Component.translatable("jade.ic2r_addons.machines.heat", n.getInt("heat")));
    tip.add(Component.translatable("jade.ic2r_addons.machines.progress", n.getInt("progress")));
    tip.add(Component.translatable("jade.ic2r_addons.machines.eu", (int) n.getDouble("eu")));
    if (n.contains("water"))
      tip.add(Component.translatable("jade.ic2r_addons.machines.water", n.getInt("water"), 8000));
    if (n.getBoolean("danger"))
      tip.add(
          Component.translatable("tooltip.ic2r_addons.machines.thermal_shock")
              .withStyle(net.minecraft.ChatFormatting.RED));
  }
}
