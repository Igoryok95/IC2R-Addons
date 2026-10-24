package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.block.tileentity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class AddonJadePlugin implements IWailaPlugin {
  private static final EngineProvider ENGINE = new EngineProvider();
  private static final com.igoryok95.ic2raddons.control.ControlJadeProvider CONTROL =
      new com.igoryok95.ic2raddons.control.ControlJadeProvider();
  private static final com.igoryok95.ic2raddons.machines.compat.MachineJadeProvider MACHINES =
      new com.igoryok95.ic2raddons.machines.compat.MachineJadeProvider();

  public void register(IWailaCommonRegistration r) {
    r.registerBlockDataProvider(
        CONTROL, com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing.class);
    r.registerBlockDataProvider(ENGINE, Ic2TileEntity.class);
    r.registerBlockDataProvider(
        MACHINES, com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine.class);
  }

  public void registerClient(IWailaClientRegistration r) {
    r.registerBlockComponent(
        CONTROL, com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock.class);
    r.registerBlockComponent(ENGINE, Ic2TileEntityBlock.class);
    r.registerBlockComponent(MACHINES, Ic2TileEntityBlock.class);
  }

  public static final class EngineProvider
      implements IServerDataProvider<BlockAccessor>, IComponentProvider<BlockAccessor> {
    public ResourceLocation getUid() {
      return IC2RAddons.id("electric_engine");
    }

    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
      if (accessor.getBlockEntity() instanceof EngineTelemetry engine)
        data.put("ic2r_addons_engine", engine.telemetry());
    }

    public void appendTooltip(ITooltip tip, BlockAccessor accessor, IPluginConfig config) {
      if (accessor.getServerData() == null
          || !accessor.getServerData().contains("ic2r_addons_engine")) return;
      CompoundTag n = accessor.getServerData().getCompound("ic2r_addons_engine");
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.buffer",
              number(n.getDouble("eu")),
              number(n.getDouble("capacity"))));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.rate",
              number(n.getDouble("euPerTick")),
              number(n.getDouble("mjPerTick"))));
      tip.add(Component.translatable("jade.ic2r_addons.engine.voltage", n.getInt("voltage")));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.status",
              Component.translatable("status.ic2r_addons.engine." + n.getString("status"))));
    }

    private static String number(double n) {
      return String.format(java.util.Locale.ROOT, "%.2f", n);
    }
  }
}
