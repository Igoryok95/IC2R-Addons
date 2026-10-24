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

public class ItemCardAdvGenerators extends ItemCardMain {

  public ItemCardAdvGenerators(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockEntity te = level.getBlockEntity(target);
    CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.ADV_GENERATORS).getCardData(te);
    if (tag == null) return CardState.NO_TARGET;
    reader.reset();
    reader.copyFrom(tag);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    if (reader.hasField(DataHelper.HEAT) && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelHeat",
              reader.getDouble(DataHelper.HEAT),
              "HU",
              showLabels));
    if (reader.hasField(DataHelper.OUTPUT) && (settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutput",
              reader.getDouble(DataHelper.OUTPUT),
              "FE/t",
              showLabels));
    if (reader.hasField(DataHelper.OUTPUTMB) && (settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutput",
              reader.getDouble(DataHelper.OUTPUTMB),
              "mB/t",
              showLabels));
    if (reader.hasField("steamOut") && (settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutputSteam",
              reader.getDouble("steamOut"),
              "mB/t",
              showLabels));
    if (reader.hasField("obsidianOut") && (settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelOutputObsidian",
              reader.getDouble("obsidianOut"),
              "mB/t",
              showLabels));
    if (reader.hasField(DataHelper.CONSUMPTIONMB) && (settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelConsumption",
              reader.getDouble(DataHelper.CONSUMPTIONMB),
              "mB/t",
              showLabels));
    if (reader.hasField("carbonIn") && (settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelConsumptionCarbon",
              reader.getDouble("carbonIn"),
              "mB/t",
              showLabels));
    if (reader.hasField("steamIn") && (settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelConsumptionSteam",
              reader.getDouble("steamIn"),
              "mB/t",
              showLabels));
    if (reader.hasField("waterIn") && (settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelConsumptionWater",
              reader.getDouble("waterIn"),
              "mB/t",
              showLabels));
    if (reader.hasField(DataHelper.ENERGY) && (settings & 4) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelEnergy",
              reader.getDouble(DataHelper.ENERGY),
              "FE",
              showLabels));
    if (reader.hasField(DataHelper.CAPACITY) && (settings & 8) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCapacity",
              reader.getDouble(DataHelper.CAPACITY),
              "FE",
              showLabels));
    if (reader.hasField("speed") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelSpeed",
              reader.getDouble("speed"),
              "RPM",
              showLabels));
    if (reader.hasField("turbines") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTurbines",
              reader.getInt("turbines"),
              showLabels));
    if (reader.hasField("heatingChambers") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelHeatingChambers",
              reader.getInt("heatingChambers"),
              showLabels));
    if (reader.hasField("mixingChambers") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelMixingChambers",
              reader.getInt("mixingChambers"),
              showLabels));
    if (reader.hasField("carbon") && (settings & 32) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCarbon",
              reader.getDouble("carbon"),
              "mB",
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
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelOther", 32));
    return result;
  }
}
