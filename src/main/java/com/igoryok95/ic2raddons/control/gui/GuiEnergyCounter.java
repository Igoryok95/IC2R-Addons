// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.containers.ContainerEnergyCounter;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GuiEnergyCounter extends AbstractContainerScreen<ContainerEnergyCounter> {
  public GuiEnergyCounter(ContainerEnergyCounter menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageHeight = 196;
    inventoryLabelY = 103;
  }

  @Override
  protected void init() {
    super.init();
    addRenderableWidget(
        Button.builder(
                Component.translatable("screen.ic2r_addons.control.reset"),
                button -> NetworkHelper.updateSeverTileEntity(menu.counter.getBlockPos(), 1, 0))
            .bounds(leftPos + 98, topPos + 68, 70, 20)
            .build());
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
    graphics.fill(
        leftPos - 2, topPos - 2, leftPos + imageWidth + 2, topPos + imageHeight + 2, 0xff202020);
    graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
    for (var slot : menu.slots) {
      int x = leftPos + slot.x, y = topPos + slot.y;
      graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff373737);
      graphics.fill(x, y, x + 17, y + 17, 0xffffffff);
      graphics.fill(x, y, x + 16, y + 16, 0xff8b8b8b);
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    String[] keys = {"total", "average", "current"};
    for (int i = 0; i < 3; i++)
      graphics.drawString(
          font,
          Component.translatable(
              "message.ic2r_addons.control.counter." + keys[i],
              String.format(Locale.ROOT, "%.2f", menu.number(i))),
          8,
          23 + i * 13,
          0xff404040,
          false);
    graphics.drawString(
        font,
        Component.translatable("message.ic2r_addons.control.counter.tier", menu.maximumPacket()),
        8,
        93,
        0xff404040,
        false);
  }

  @Override
  public void render(GuiGraphics graphics, int x, int y, float partial) {
    super.render(graphics, x, y, partial);
    renderTooltip(graphics, x, y);
  }
}
