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
  private static final ElectricalJadeProvider ELECTRICAL = new ElectricalJadeProvider();
  private static final com.igoryok95.ic2raddons.control.ControlJadeProvider CONTROL =
      new com.igoryok95.ic2raddons.control.ControlJadeProvider();
  private static final com.igoryok95.ic2raddons.machines.compat.MachineJadeProvider MACHINES =
      new com.igoryok95.ic2raddons.machines.compat.MachineJadeProvider();

  public void register(IWailaCommonRegistration r) {
    r.registerBlockDataProvider(ELECTRICAL, Ic2TileEntity.class);
    r.registerBlockDataProvider(
        CONTROL, com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing.class);
    r.registerBlockDataProvider(ENGINE, Ic2TileEntity.class);
    r.registerBlockDataProvider(
        MACHINES, com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine.class);
  }

  public void registerClient(IWailaClientRegistration r) {
    r.registerBlockComponent(ELECTRICAL, Ic2TileEntityBlock.class);
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
      var elements = snownee.jade.api.ui.IElementHelper.get();
      var box = snownee.jade.api.ui.BoxStyle.getNestedBox();
      tip.add(
          elements.progress(
              (float) Math.clamp(n.getDouble("eu") / Math.max(1, n.getDouble("capacity")), 0, 1),
              Component.translatable(
                  "jade.ic2r_addons.engine.buffer",
                  number(n.getDouble("eu")),
                  number(n.getDouble("capacity"))),
              elements.progressStyle().color(0xFF318BD1, 0xFF1765A2).textColor(0xFFFFFFFF),
              box,
              true));
      tip.add(
          elements.progress(
              (float)
                  Math.clamp(
                      (double) n.getLong("mjBuffer") / Math.max(1, n.getLong("mjCapacity")), 0, 1),
              Component.translatable(
                  "jade.ic2r_addons.engine.mj_buffer",
                  number(n.getLong("mjBuffer") / 1_000_000.0),
                  number(n.getLong("mjCapacity") / 1_000_000.0)),
              elements.progressStyle().color(0xFFE34C3A, 0xFFB51F1F).textColor(0xFFFFFFFF),
              box,
              true));
      int stage = Math.clamp(n.getInt("heatStage"), 0, 4);
      String[] stages = {"blue", "green", "yellow", "orange", "red"};
      net.minecraft.ChatFormatting[] colors = {
        net.minecraft.ChatFormatting.BLUE,
        net.minecraft.ChatFormatting.GREEN,
        net.minecraft.ChatFormatting.YELLOW,
        net.minecraft.ChatFormatting.GOLD,
        net.minecraft.ChatFormatting.RED
      };
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.stage",
              Component.translatable("stage.ic2r_addons.engine." + stages[stage])
                  .withStyle(colors[stage])));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.redstone",
              Component.translatable(n.getBoolean("redstone") ? "gui.yes" : "gui.no")
                  .withStyle(
                      n.getBoolean("redstone")
                          ? net.minecraft.ChatFormatting.GREEN
                          : net.minecraft.ChatFormatting.RED)));
      tip.add(
          Component.translatable(
              "screen.ic2r_addons.engine.network_input", number(n.getDouble("networkEuPerTick"))));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.rate",
              number(n.getDouble("euPerTick")),
              number(n.getDouble("mjPerTick"))));
      tip.add(Component.translatable("jade.ic2r_addons.engine.voltage", n.getInt("voltage")));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.engine.heat",
              number(n.getDouble("temperature")),
              number(n.getDouble("heatPercent"))));
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
