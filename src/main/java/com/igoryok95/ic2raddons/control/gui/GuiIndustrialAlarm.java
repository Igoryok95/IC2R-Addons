// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.gui.controls.GuiHowlerAlarmSlider;
import com.igoryok95.ic2raddons.control.lib.gui.GuiBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityHowlerAlarm;
import net.minecraft.client.gui.GuiGraphics;

public class GuiIndustrialAlarm extends GuiBase {
  private TileEntityHowlerAlarm alarm;
  private GuiHowlerAlarmSlider slider;

  public GuiIndustrialAlarm(TileEntityHowlerAlarm alarm) {
    super(
        "block.ic2r_addons.industrial_alarm",
        131,
        64,
        EnergyControl.MODID + ":textures/gui/gui_industrial_alarm.png");
    this.alarm = alarm;
  }

  @Override
  public void init() {
    super.init();
    slider = new GuiHowlerAlarmSlider(guiLeft + 12, guiTop + 33, alarm);
    addRenderableWidget(slider);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(GuiGraphics matrixStack, int mouseX, int mouseY) {
    drawTitle(matrixStack);
  }
}
