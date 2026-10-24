// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui.controls;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityThermalMonitor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class GuiThermoInvertRedstone extends AbstractButton {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.parse(EnergyControl.MODID + ":textures/gui/gui_thermal_monitor.png");

  TileEntityThermalMonitor thermo;
  private boolean checked;

  public GuiThermoInvertRedstone(int x, int y, TileEntityThermalMonitor thermo) {
    super(x, y, 0, 0, CommonComponents.EMPTY);
    height = 15;
    width = 51;
    this.thermo = thermo;
    checked = thermo.getInvertRedstone();
  }

  @Override
  protected void renderWidget(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    int delta = checked ? 15 : 0;
    matrixStack.blit(TEXTURE, getX(), getY() + 1, 199, delta, 51, 15, 256, 256);
  }

  @SuppressWarnings("resource")
  @Override
  public void onPress() {
    checked = !checked;
    if (thermo.getLevel().isClientSide() && thermo.getInvertRedstone() != checked) {
      NetworkHelper.updateSeverTileEntity(thermo.getBlockPos(), 2, checked ? (int) 1 : (int) 0);
      thermo.setInvertRedstone(checked);
    }
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {}
}
