// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.*;
import java.util.List;
import net.minecraft.world.level.Level;

public final class ItemCardCounter extends ItemCardIC2 {
  public ItemCardCounter(Properties properties) {
    super(properties);
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean server, boolean labels) {
    var lines = reader.getTitleList();
    lines.add(
        new PanelString(
            "message.ic2r_addons.control.counter.total", reader.getDouble("total"), labels));
    lines.add(
        new PanelString(
            "message.ic2r_addons.control.counter.average", reader.getDouble("average"), labels));
    lines.add(
        new PanelString(
            "message.ic2r_addons.control.counter.current", reader.getDouble("current"), labels));
    return lines;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    return List.of();
  }
}
