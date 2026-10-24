// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityHowlerAlarm;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityIndustrialAlarm;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityThermalMonitor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class ScreenHandler {

  // alarms present on the client and allowed by the server
  public static List<String> getAlarms() {
    List<String> items =
        EnergyControl.INSTANCE.availableAlarms == null
            ? new ArrayList<>()
            : new ArrayList<>(EnergyControl.INSTANCE.availableAlarms);
    if (EnergyControl.INSTANCE.serverAllowedAlarms != null)
      items.retainAll(EnergyControl.INSTANCE.serverAllowedAlarms);
    return items;
  }

  public static void openHowlerAlarmScreen(TileEntityHowlerAlarm be) {
    List<String> items = getAlarms();
    Minecraft.getInstance().setScreen(new GuiHowlerAlarm(be, items.size() > 10));
  }

  public static void openIndustrialAlarmScreen(TileEntityIndustrialAlarm be) {
    Minecraft.getInstance().setScreen(new GuiIndustrialAlarm(be));
  }

  public static void openThermalMonitorScreen(TileEntityThermalMonitor be) {
    Minecraft.getInstance().setScreen(new GuiThermalMonitor(be));
  }
}
