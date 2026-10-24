// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.gui.controls.GuiHowlerAlarmListBox;
import com.igoryok95.ic2raddons.control.gui.controls.GuiHowlerAlarmSlider;
import com.igoryok95.ic2raddons.control.lib.gui.GuiBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityHowlerAlarm;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;

public class GuiHowlerAlarm extends GuiBase {
  private TileEntityHowlerAlarm alarm;
  private GuiHowlerAlarmSlider slider;
  private GuiHowlerAlarmListBox listBox;
  private boolean isBig;

  public GuiHowlerAlarm(TileEntityHowlerAlarm alarm, boolean isBig) {
    super(
        "block.ic2r_addons.howler_alarm",
        131,
        isBig ? 236 : 136,
        isBig
            ? EnergyControl.MODID + ":textures/gui/gui_howler_alarm_big.png"
            : EnergyControl.MODID + ":textures/gui/gui_howler_alarm.png");
    this.alarm = alarm;
    this.isBig = isBig;
  }

  @Override
  public void init() {
    super.init();
    slider = new GuiHowlerAlarmSlider(guiLeft + 12, guiTop + 33, alarm);

    List<String> items = ScreenHandler.getAlarms();

    listBox =
        new GuiHowlerAlarmListBox(guiLeft + 13, guiTop + 63, 105, isBig ? 165 : 65, items, alarm);
    addRenderableWidget(slider);
    addRenderableWidget(listBox);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(GuiGraphics matrixStack, int mouseX, int mouseY) {
    drawTitle(matrixStack);
    matrixStack.drawString(
        font,
        Component.translatable("message.ic2r_addons.control.HowlerAlarmSound"),
        12,
        53,
        ARGB32.opaque(0x404040),
        false);
  }
}
