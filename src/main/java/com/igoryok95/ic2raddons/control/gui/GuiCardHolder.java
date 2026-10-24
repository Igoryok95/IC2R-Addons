// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.containers.ContainerCardHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCardHolder extends AbstractContainerScreen<ContainerCardHolder> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.parse("textures/gui/container/generic_54.png");
  private final int inventoryRows;

  public GuiCardHolder(ContainerCardHolder container, Inventory inventory, Component title) {
    super(container, inventory, title);
    imageHeight = 114 + 6 * 18;
    inventoryLabelY = imageHeight - 94;
    inventoryRows = 6;
  }

  @Override
  public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    renderTooltip(matrixStack, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {
    matrixStack.drawString(font, title, 8, 6, 0x404040, false);
    matrixStack.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040, false);
  }

  @Override
  protected void renderBg(GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
    matrixStack.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, inventoryRows * 18 + 17, 256, 256);
    matrixStack.blit(
        TEXTURE, leftPos, topPos + inventoryRows * 18 + 17, 0, 126, imageWidth, 96, 256, 256);
  }
}
