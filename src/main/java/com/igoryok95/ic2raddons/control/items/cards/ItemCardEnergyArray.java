// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.utils.DataHelper;
import com.igoryok95.ic2raddons.control.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCardEnergyArray extends ItemCardMain {

  public ItemCardEnergyArray(Properties properties) {
    super(properties);
  }

  private static final int STATUS_NOT_FOUND = Integer.MIN_VALUE;
  private static final int STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    int cardCount = reader.getCardCount();
    if (cardCount == 0) return CardState.INVALID_CARD;

    double totalEnergy = 0.0;
    boolean foundAny = false;
    boolean outOfRange = false;
    for (int i = 0; i < cardCount; i++) {
      BlockPos target = getCoordinates(reader, i);
      int dx = target.getX() - pos.getX();
      int dy = target.getY() - pos.getY();
      int dz = target.getZ() - pos.getZ();
      if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
        BlockEntity te = level.getBlockEntity(target);
        if (te != null) {
          CompoundTag tag = CrossModLoader.getEnergyData(te);
          if (tag != null) {
            double stored = tag.getDouble(DataHelper.ENERGY);
            double capacity = tag.getDouble(DataHelper.CAPACITY);
            totalEnergy += stored;
            reader.setDouble(String.format("_%denergy", i), stored);
            reader.setDouble(String.format("_%dmaxCapacity", i), capacity);
            foundAny = true;
          } else reader.setDouble(String.format("_%denergy", i), (double) STATUS_NOT_FOUND);
        } else reader.setDouble(String.format("_%denergy", i), (double) STATUS_NOT_FOUND);
      } else {
        reader.setDouble(String.format("_%denergy", i), (double) STATUS_OUT_OF_RANGE);
        outOfRange = true;
      }
    }
    reader.setDouble("energy", totalEnergy);
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
    double totalEnergy = 0;
    double totalStorage = 0;
    boolean showEach = (settings & 1) > 0;
    boolean showSummary = (settings & 2) > 0;
    boolean showEnergy = (settings & 4) > 0;
    boolean showFree = (settings & 8) > 0;
    boolean showStorage = (settings & 16) > 0;
    boolean showPercentage = (settings & 32) > 0;
    for (int i = 0; i < reader.getCardCount(); i++) {
      double energy = reader.getDouble(String.format("_%denergy", i));
      double storage = reader.getDouble(String.format("_%dmaxCapacity", i));
      boolean isOutOfRange = energy == STATUS_OUT_OF_RANGE;
      boolean isNotFound = energy == STATUS_NOT_FOUND;
      if (showSummary && !isOutOfRange && !isNotFound) {
        totalEnergy += energy;
        totalStorage += storage;
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
          if (showEnergy) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelEnergyN",
                          i + 1,
                          StringUtils.getFormatted("", energy, false))));
            else result.add(new PanelString(StringUtils.getFormatted("", energy, false)));
          }
          if (showFree) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelFreeN",
                          i + 1,
                          StringUtils.getFormatted("", storage - energy, false))));
            else result.add(new PanelString(StringUtils.getFormatted("", storage - energy, false)));
          }
          if (showStorage) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelStorageN",
                          i + 1,
                          StringUtils.getFormatted("", storage, false))));
            else result.add(new PanelString(StringUtils.getFormatted("", storage, false)));
          }
          if (showPercentage) {
            if (showLabels)
              result.add(
                  new PanelString(
                      StringUtils.getFormattedKey(
                          "message.ic2r_addons.control.InfoPanelPercentageN",
                          i + 1,
                          StringUtils.getFormatted(
                              "",
                              storage == 0 ? 100 : (int) (((double) energy / storage) * 100D),
                              false))));
            else
              result.add(
                  new PanelString(
                      StringUtils.getFormatted(
                          "",
                          storage == 0 ? 100 : (int) (((double) energy / storage) * 100D),
                          false)));
          }
        }
      }
    }
    if (showSummary) {
      if (showEnergy)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelEnergy", totalEnergy, showLabels));
      if (showFree)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelFree",
                totalStorage - totalEnergy,
                showLabels));
      if (showStorage)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelCapacity", totalStorage, showLabels));
      if (showPercentage)
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelPercentage",
                totalStorage == 0 ? 100 : ((totalEnergy / totalStorage) * 100),
                showLabels));
    }
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(6);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelEachCard", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelEnergy", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelFree", 8));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelStorage", 16));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelPercentage", 32));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelTotal", 2));
    return result;
  }

  @Override
  public boolean isRemoteCard() {
    return false;
  }
}
