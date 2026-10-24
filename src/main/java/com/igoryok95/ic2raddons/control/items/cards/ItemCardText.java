// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemCardText extends ItemCardMain {

  public ItemCardText(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int displaySettings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = new LinkedList<>();
    boolean started = false;
    for (int i = 9; i >= 0; i--) {
      String text = reader.getString("line_" + i);
      if (text.equals("") && !started) continue;
      started = true;
      result.add(0, new PanelString(text.replace("@", "\u00a7").replace("\u00a7\u00a7", "@")));
    }
    String title = reader.getTitle();
    if (title != null && !title.isEmpty()) {
      PanelString titleString = new PanelString();
      titleString.textCenter = title.replace("@", "\u00a7").replace("\u00a7\u00a7", "@");
      result.add(0, titleString);
    }
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    return null;
  }
}
