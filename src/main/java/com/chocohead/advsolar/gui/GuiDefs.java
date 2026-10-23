package com.chocohead.advsolar.gui;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.Util;

public final class GuiDefs {
  static {
    ProgressBars.addStyles();
  }

  private GuiDefs() {}

  public static GuiParser.GuiNode parse(Ic2TileEntity te) {
    return GuiParser.parse(Util.getName(te.getBlockType()), te.getClass());
  }
}
