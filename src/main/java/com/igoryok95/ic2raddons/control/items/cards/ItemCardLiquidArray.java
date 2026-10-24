// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import com.igoryok95.ic2raddons.control.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemCardLiquidArray extends ItemCardMain {

  public ItemCardLiquidArray(Properties properties) {
    super(properties);
  }

  private static final long STATUS_NOT_FOUND = Integer.MIN_VALUE;
  private static final long STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    int cardCount = reader.getCardCount();
    if (cardCount == 0) return CardState.INVALID_CARD;

    double totalAmount = 0.0;

    boolean foundAny = false;
    boolean outOfRange = false;
    for (int i = 0; i < cardCount; i++) {
      BlockPos target = getCoordinates(reader, i);
      int dx = target.getX() - pos.getX();
      int dy = target.getY() - pos.getY();
      int dz = target.getZ() - pos.getZ();
      if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
        FluidInfo storage = CrossModLoader.getTankAt(level, target);
        if (storage != null) {
          storage.write(reader, i);
          foundAny = true;
        } else reader.setLong(String.format("_%damount", i), STATUS_NOT_FOUND);
      } else {
        reader.setLong(String.format("_%damount", i), STATUS_OUT_OF_RANGE);
        outOfRange = true;
      }
    }
    reader.setDouble("energy", totalAmount);
    if (!foundAny) {
      if (outOfRange) return CardState.OUT_OF_RANGE;
      return CardState.NO_TARGET;
    }
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    double totalAmount = 0;
    double totalCapacity = 0;
    boolean showName = (settings & 1) > 0;
    boolean showAmount = true;
    boolean showFree = (settings & 2) > 0;
    boolean showCapacity = (settings & 4) > 0;
    boolean showPercentage = (settings & 8) > 0;
    boolean showEach = (settings & 16) > 0;
    boolean showSummary = (settings & 32) > 0;
    for (int i = 0; i < reader.getInt("cardCount"); i++) {
      long amount = reader.getLong(String.format("_%damount", i));
      long capacity = reader.getLong(String.format("_%dcapacity", i));
      boolean isOutOfRange = amount == STATUS_OUT_OF_RANGE;
      boolean isNotFound = amount == STATUS_NOT_FOUND;
      if (showSummary && !isOutOfRange && !isNotFound) {
        totalAmount += amount;
        totalCapacity += capacity;
      }

      if (showEach) {
        if (isOutOfRange) {
          result.add(
              new PanelString(
                  StringUtils.getFormattedKey(
                      "message.ic2r_addons.control.InfoPanelOutOfRangeN", i + 1)));
        } else if (isNotFound) {
          result.add(
              new PanelString(
                  StringUtils.getFormattedKey(
                      "message.ic2r_addons.control.InfoPanelNotFoundN", i + 1)));
        } else {
          if (showName) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelLiquidNameN",
                          i + 1,
                          reader.getString(String.format("_%dname", i)))));
            else result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
          }
          if (showAmount) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelLiquidN",
                          i + 1,
                          StringUtils.getFormatted("", amount, false))));
            else result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
          }
          if (showFree) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelLiquidFreeN",
                          i + 1,
                          StringUtils.getFormatted("", capacity - amount, false))));
            else
              result.add(new PanelString(StringUtils.getFormatted("", capacity - amount, false)));
          }
          if (showCapacity) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelLiquidCapacityN",
                          i + 1,
                          StringUtils.getFormatted("", capacity, false))));
            else result.add(new PanelString(StringUtils.getFormatted("", capacity, false)));
          }
          if (showPercentage) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelLiquidPercentageN",
                          i + 1,
                          StringUtils.getFormatted(
                              "",
                              capacity == 0 ? 100 : (((double) amount / capacity) * 100),
                              false))));
            else
              result.add(
                  new PanelString(
                      StringUtils.getFormatted(
                          "", capacity == 0 ? 100 : (((double) amount / capacity) * 100), false)));
          }
        }
      }
    }
    if (showSummary) {
      if (showAmount)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelLiquidAmount", totalAmount, showLabels));
      if (showFree)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelLiquidFree",
                totalCapacity - totalAmount,
                showLabels));
      if (showName)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelLiquidCapacity", totalCapacity, showLabels));
      if (showPercentage)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelLiquidPercentage",
                totalCapacity == 0 ? 100 : ((totalAmount / totalCapacity) * 100),
                showLabels));
    }
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(6);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidName", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidFree", 2));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidCapacity", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidPercentage", 8));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelEachCard", 16));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelTotal", 32));
    return result;
  }

  @Override
  public boolean isRemoteCard() {
    return false;
  }
}
