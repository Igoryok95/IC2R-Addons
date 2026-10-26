package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import com.igoryok95.ic2raddons.solar.te.TileEntitySolarPanel;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public final class ElectricalJadeProvider
    implements IServerDataProvider<BlockAccessor>, IComponentProvider<BlockAccessor> {
  @Override
  public ResourceLocation getUid() {
    return IC2RAddons.id("electrical_status");
  }

  public static CompoundTag collect(Ic2TileEntity tile) {
    var data = new CompoundTag();
    if (!(tile instanceof TileEntitySolarPanel
        || tile instanceof TileEntityMolecularTransformer
        || tile instanceof TileEntityHeatingMachine)) return data;
    var energy = tile.getComponent(Energy.class);
    data.putDouble("energy", energy.getEnergy());
    data.putDouble("capacity", energy.getCapacity());
    if (tile instanceof TileEntitySolarPanel panel) {
      data.putDouble("generation", panel.lastGeneration);
      data.putInt("conditions", panel.generationState);
      data.putDouble(
          "packet", ic2.api.energy.EnergyNet.instance.getPowerFromTier(energy.getSourceTier()));
    } else if (tile instanceof TileEntityMolecularTransformer transformer
        && transformer.totalEU > 0) {
      data.putDouble("progress", transformer.getGuiValue("progress"));
      data.putDouble("used", transformer.energyUsed);
      data.putInt("required", transformer.totalEU);
      data.putDouble("consumption", transformer.lastEnergyIn);
      if (!transformer.currentOutput.isEmpty())
        data.put("result", transformer.currentOutput.save(tile.getLevel().registryAccess()));
    }
    return data;
  }

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof Ic2TileEntity tile)
      data.put("ic2r_addons_electrical", collect(tile));
  }

  @Override
  public void appendTooltip(ITooltip tip, BlockAccessor accessor, IPluginConfig config) {
    if (!accessor.getServerData().contains("ic2r_addons_electrical")) return;
    var data = accessor.getServerData().getCompound("ic2r_addons_electrical");
    if (!data.contains("energy")) return;
    var elements = IElementHelper.get();
    tip.add(
        elements.progress(
            (float)
                Math.clamp(
                    data.getDouble("energy") / Math.max(1, data.getDouble("capacity")), 0, 1),
            Component.translatable(
                "jade.ic2r_addons.electrical.energy",
                number(data.getDouble("energy")),
                number(data.getDouble("capacity"))),
            elements.progressStyle().color(0xFF318BD1).textColor(0xFFFFFFFF),
            BoxStyle.getNestedBox(),
            true));
    if (data.contains("generation")) {
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.electrical.generation", number(data.getDouble("generation"))));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.electrical.packet", number(data.getDouble("packet"))));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.electrical.conditions",
              Component.translatable(
                  "jade.ic2r_addons.electrical.conditions." + data.getInt("conditions"))));
    }
    if (data.contains("progress")) {
      tip.add(
          elements.progress(
              (float) Math.clamp(data.getDouble("progress"), 0, 1),
              Component.translatable(
                  "jade.ic2r_addons.machines.progress",
                  String.format(Locale.ROOT, "%.2f", data.getDouble("progress") * 100)),
              elements.progressStyle().color(0xFF53A653).textColor(0xFFFFFFFF),
              BoxStyle.getNestedBox(),
              true));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.electrical.recipe_energy",
              number(data.getDouble("used")),
              number(data.getInt("required"))));
      tip.add(
          Component.translatable(
              "jade.ic2r_addons.machines.eu", number(data.getDouble("consumption"))));
      if (data.contains("result"))
        tip.add(
            Component.translatable(
                "jade.ic2r_addons.electrical.result",
                ItemStack.parseOptional(
                        accessor.getLevel().registryAccess(), data.getCompound("result"))
                    .getHoverName()));
    }
  }

  private static String number(double value) {
    return String.format(Locale.ROOT, "%,.2f", value).replace(',', ' ').replaceAll("\\.?0+$", "");
  }
}
