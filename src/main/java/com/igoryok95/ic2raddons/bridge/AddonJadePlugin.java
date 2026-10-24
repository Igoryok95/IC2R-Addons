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

  public void register(IWailaCommonRegistration r) {
    r.registerBlockDataProvider(ENGINE, Ic2TileEntity.class);
  }

  public void registerClient(IWailaClientRegistration r) {
    r.registerBlockComponent(ENGINE, Ic2TileEntityBlock.class);
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
