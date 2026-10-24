// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.lib.gui.controls;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GuiTextNumeric extends EditBox {
  int max = Integer.MAX_VALUE;

  public GuiTextNumeric(Font font, int x, int y, int width, int height, Component msg, int max) {
    super(font, x, y, width, height, msg);
    this.max = max;
  }

  @Override
  public void insertText(String textToWrite) {
    String oldValue = getValue();
    super.insertText(textToWrite);
    String fixed = getValue().replaceAll("[^\\d]", "");
    if (!fixed.isEmpty()) {
      int newValue = Integer.parseInt(fixed);
      if (newValue > max) fixed = oldValue;
    }
    setValue(fixed);
  }
}
