// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.crossmod.ModIDs;
import com.igoryok95.ic2raddons.control.utils.DataHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCardMekanism extends ItemCardMain {

  public ItemCardMekanism(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockEntity te = level.getBlockEntity(target);
    CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.MEKANISM).getCardData(te);
    if (tag == null) tag = CrossModLoader.getCrossMod(ModIDs.MEKANISM_GENERATORS).getCardData(te);
    if (tag == null) return CardState.NO_TARGET;
    reader.reset();
    reader.copyFrom(tag);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    String euType = reader.getString(DataHelper.EUTYPE);
    if (reader.hasField(DataHelper.STATUS) && (settings & 64) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelStatus",
              reader.getString(DataHelper.STATUS),
              showLabels));
    if (reader.hasField("burn_rate") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelBurnRate",
              reader.getDouble("burn_rate"),
              showLabels));
    if (reader.hasField("heat_rate") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelHeatRate",
              reader.getDouble("heat_rate"),
              showLabels));
    if (reader.hasField("rate_limit") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelRateLimit",
              reader.getDouble("rate_limit"),
              showLabels));
    if (reader.hasField("boil_rate") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelBoilRate",
              reader.getDouble("boil_rate"),
              showLabels));
    if (reader.hasField("production") && (settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutput",
              reader.getDouble("production"),
              euType + "/t",
              showLabels));
    if (reader.hasField("usage") && (settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelUsing",
              reader.getDouble("usage"),
              euType,
              showLabels));
    if (reader.hasField(DataHelper.ENERGY) && (settings & 4) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelEnergy",
              reader.getDouble(DataHelper.ENERGY),
              euType,
              showLabels));
    if (reader.hasField(DataHelper.CAPACITY) && (settings & 8) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCapacity",
              reader.getDouble(DataHelper.CAPACITY),
              euType,
              showLabels));
    if (reader.hasField("flow_rate") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelFlowRate",
              reader.getDouble("flow_rate"),
              showLabels));
    if (reader.hasField("injection_rate") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelInjectionRate",
              reader.getDouble("injection_rate"),
              showLabels));
    if (reader.hasField("temp") && (settings & 64) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTemperature",
              reader.getString("temp"),
              showLabels));
    if (reader.hasField("plasma") && (settings & 64) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelPlasma",
              reader.getString("plasma"),
              showLabels));
    if (reader.hasField(DataHelper.TANK) && (settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTank",
              reader.getString(DataHelper.TANK),
              showLabels));
    if (reader.hasField(DataHelper.TANK2) && (settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTank",
              reader.getString(DataHelper.TANK2),
              showLabels));
    if (reader.hasField(DataHelper.TANK3) && (settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTank",
              reader.getString(DataHelper.TANK3),
              showLabels));
    if (reader.hasField(DataHelper.TANK4) && (settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTank",
              reader.getString(DataHelper.TANK4),
              showLabels));
    if (reader.hasField(DataHelper.TANK5) && (settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTank",
              reader.getString(DataHelper.TANK5),
              showLabels));
    if (reader.hasField("input") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelInput",
              reader.getDouble("input"),
              euType + "/t",
              showLabels));
    if (reader.hasField(DataHelper.OUTPUT) && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutput",
              reader.getDouble(DataHelper.OUTPUT),
              euType + "/t",
              showLabels));
    if (reader.hasField(DataHelper.ACTIVE))
      addOnOff(result, isServer, reader.getBoolean(DataHelper.ACTIVE));
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(4);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelOutput", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelUsing", 2));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelEnergy", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelCapacity", 8));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelTank", 16));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelRate", 32));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelOther", 64));
    return result;
  }
}
