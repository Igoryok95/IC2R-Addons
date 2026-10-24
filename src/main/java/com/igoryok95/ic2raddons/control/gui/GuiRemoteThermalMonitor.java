// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.containers.ContainerRemoteThermalMonitor;
import com.igoryok95.ic2raddons.control.gui.controls.CompactButton;
import com.igoryok95.ic2raddons.control.gui.controls.GuiThermoInvertRedstone;
import com.igoryok95.ic2raddons.control.lib.gui.GuiContainerBase;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityRemoteThermalMonitor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRemoteThermalMonitor extends GuiContainerBase<ContainerRemoteThermalMonitor> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          EnergyControl.MODID, "textures/gui/gui_remote_thermo.png");

  private TileEntityRemoteThermalMonitor te;
  private EditBox textboxHeat;

  public GuiRemoteThermalMonitor(
      ContainerRemoteThermalMonitor container, Inventory inventory, Component title) {
    super(container, inventory, title, TEXTURE, 178, 166);
    this.te = container.te;
  }

  @Override
  public void init() {
    super.init();
    addRenderableWidget(
        new CompactButton(
            0,
            leftPos + 40,
            topPos - 5 + 20,
            22,
            12,
            Component.translatable("screen.ic2r_addons.control.subtract1"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            1,
            leftPos + 40,
            topPos - 5 + 31,
            22,
            12,
            Component.translatable("screen.ic2r_addons.control.subtract10"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            2,
            leftPos + 5,
            topPos - 5 + 20,
            36,
            12,
            Component.translatable("screen.ic2r_addons.control.subtract100"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            3,
            leftPos + 5,
            topPos - 5 + 31,
            36,
            12,
            Component.translatable("screen.ic2r_addons.control.subtract1000"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            4,
            leftPos + 5,
            topPos - 5 + 42,
            57,
            12,
            Component.translatable("screen.ic2r_addons.control.subtract10000"),
            (button) -> {
              actionPerformed(button);
            }));

    addRenderableWidget(
        new CompactButton(
            5,
            leftPos + 115,
            topPos - 5 + 20,
            22,
            12,
            Component.translatable("screen.ic2r_addons.control.add1"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            6,
            leftPos + 115,
            topPos - 5 + 31,
            22,
            12,
            Component.translatable("screen.ic2r_addons.control.add10"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            7,
            leftPos + 136,
            topPos - 5 + 20,
            36,
            12,
            Component.translatable("screen.ic2r_addons.control.add100"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            8,
            leftPos + 136,
            topPos - 5 + 31,
            36,
            12,
            Component.translatable("screen.ic2r_addons.control.add1000"),
            (button) -> {
              actionPerformed(button);
            }));
    addRenderableWidget(
        new CompactButton(
            9,
            leftPos + 115,
            topPos - 5 + 42,
            57,
            12,
            Component.translatable("screen.ic2r_addons.control.add10000"),
            (button) -> {
              actionPerformed(button);
            }));

    addRenderableWidget(new GuiThermoInvertRedstone(leftPos + 63, topPos + 33, te));

    textboxHeat = addTextFieldWidget(63, 16, 51, 12, true, Integer.toString(te.getHeatLevel()));
  }

  @Override
  public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    textboxHeat.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  @SuppressWarnings("resource")
  private void updateHeat(int delta) {
    if (textboxHeat == null) return;
    int heat = 0;
    try {
      String value = textboxHeat.getValue();
      if (!"".equals(value)) heat = Integer.parseInt(value);
    } catch (NumberFormatException e) {
    }
    heat += delta;
    if (heat < 0) heat = 0;
    if (heat >= 1000000) heat = 1000000;
    if (te.getLevel().isClientSide() && te.getHeatLevel() != heat) {
      NetworkHelper.updateSeverTileEntity(te.getBlockPos(), 1, heat);
      te.setHeatLevel(heat);
    }
    textboxHeat.setValue(Integer.toString(heat));
  }

  // Enter applies the heat level, the other keys go to the focused field through GuiContainerBase
  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (textboxHeat.isFocused()
        && (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)) {
      updateHeat(0);
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {
    drawCenteredText(matrixStack, title, imageWidth, 6);
    drawLeftAlignedText(matrixStack, I18n.get("container.inventory"), 8, (imageHeight - 96) + 2);
  }

  @Override
  public void onClose() {
    updateHeat(0);
    super.onClose();
  }

  protected void actionPerformed(Button button) {
    if (((CompactButton) button).getId() >= 10) return;

    int delta = Integer.parseInt(button.getMessage().getString().replace("+", ""));
    updateHeat(delta);
  }
}
