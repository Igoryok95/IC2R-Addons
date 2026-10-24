// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui.controls;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityRangeTrigger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class GuiRangeTriggerInvertRedstone extends AbstractButton {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          EnergyControl.MODID, "textures/gui/gui_range_trigger.png");

  TileEntityRangeTrigger trigger;
  private boolean checked;

  public GuiRangeTriggerInvertRedstone(int x, int y, TileEntityRangeTrigger trigger) {
    super(x, y, 0, 0, CommonComponents.EMPTY);
    height = 15;
    width = 18;
    this.trigger = trigger;
    checked = trigger.getInvertRedstone();
  }

  @Override
  protected void renderWidget(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    matrixStack.blit(TEXTURE, getX(), getY() + 1, 176, checked ? 15 : 0, 18, 15, 256, 256);
  }

  @Override
  public void onPress() {
    checked = !checked;

    if (trigger.getLevel().isClientSide() && trigger.getInvertRedstone() != checked) {
      NetworkHelper.updateSeverTileEntity(trigger.getBlockPos(), 2, checked ? 1 : 0);
      trigger.setInvertRedstone(checked);
    }
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {}
}
