// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

public interface IScreenPart {

  void setScreen(Screen screen);

  Screen getScreen();

  void updateData();

  void updateTileEntity();
}
