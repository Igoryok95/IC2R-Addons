// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemCardLiquidAdvanced extends ItemCardMain {

  public ItemCardLiquidAdvanced(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    List<FluidInfo> tanks = CrossModLoader.getAllTanks(level, target);
    if (tanks == null || tanks.size() < 1) return CardState.NO_TARGET;

    int i = 0;
    for (FluidInfo tank : tanks) {
      tank.write(reader, i);
      i++;
    }
    reader.setInt("count", i);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    int count = reader.getInt("count");
    for (int i = 0; i < count; i++) addTankData(result, settings, reader, isServer, showLabels, i);
    return result;
  }

  private void addTankData(
      List<PanelString> result,
      int settings,
      ICardReader reader,
      boolean isServer,
      boolean showLabels,
      int i) {
    if (!reader.hasField(String.format("_%dcapacity", i))) return;
    int capacity = reader.getInt(String.format("_%dcapacity", i));
    int amount = reader.getInt(String.format("_%damount", i));

    if ((settings & 1) > 0) {
      String name = reader.getString(String.format("_%dname", i));
      if (name.isEmpty())
        name =
            com.igoryok95.ic2raddons.control.utils.Translator.get(
                "message.ic2r_addons.control.None");
      result.add(new PanelString("message.ic2r_addons.control.InfoPanelName", name, showLabels));
    }
    if ((settings & 2) > 0)
      result.add(
          new PanelString("message.ic2r_addons.control.InfoPanelAmount", amount, "mB", showLabels));
    if ((settings & 4) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelFree", capacity - amount, "mB", showLabels));
    if ((settings & 8) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCapacity", capacity, "mB", showLabels));
    if ((settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelPercentage",
              capacity == 0 ? 100 : (amount * 100 / capacity),
              showLabels));
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(5);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidName", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidAmount", 2));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidFree", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidCapacity", 8));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidPercentage", 16));
    return result;
  }

  @Override
  public boolean isRemoteCard() {
    return true;
  }
}
