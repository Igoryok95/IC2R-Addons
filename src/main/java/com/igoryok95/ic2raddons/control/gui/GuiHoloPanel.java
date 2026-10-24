// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.api.IItemCard;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.containers.ContainerHoloPanel;
import com.igoryok95.ic2raddons.control.gui.controls.GuiInfoPanelCheckBox;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardMain;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardReader;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardText;
import com.igoryok95.ic2raddons.control.lib.gui.controls.GuiButtonGeneral;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiHoloPanel extends GuiPanelBase<ContainerHoloPanel> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "textures/gui/gui_holo_panel.png");
  private GuiButtonGeneral colorButton;

  public GuiHoloPanel(ContainerHoloPanel container, Inventory inventory, Component title) {
    super(container, inventory, title, TEXTURE, 201);
    panel = container.te;
    name = I18n.get("block.ic2r_addons.holo_panel");
  }

  protected void initButtons() {
    addRenderableWidget(
        new GuiButtonGeneral(
                leftPos + imageWidth - 24,
                topPos + 42,
                16,
                16,
                TEXTURE,
                176,
                panel.getShowLabels() ? 15 : 31,
                (button) -> {
                  actionPerformed(button, ID_LABELS);
                })
            .setGradient());
    colorButton = null;
    updateColorButton();
    addRenderableWidget(
        new GuiButtonGeneral(
                leftPos + imageWidth - 24,
                topPos + 42 + 17 * 3,
                16,
                16,
                Component.literal(Integer.toString(panel.getTickRate())),
                (button) -> {
                  actionPerformed(button, ID_TICKRATE);
                })
            .setGradient());
  }

  // the color upgrade slot can change while the GUI is open
  private void updateColorButton() {
    boolean colored = panel.isColoredEval();
    if (colored && colorButton == null)
      colorButton =
          addRenderableWidget(
              new GuiButtonGeneral(
                      leftPos + imageWidth - 24,
                      topPos + 42 + 17,
                      16,
                      16,
                      TEXTURE,
                      192,
                      0,
                      (button) -> {
                        actionPerformed(button, ID_COLORS);
                      })
                  .setGradient()
                  .setScale(2));
    else if (!colored && colorButton != null) {
      removeWidget(colorButton);
      colorButton = null;
    }
  }

  protected void initControls() {
    ItemStack stack = panel.getCards().get(activeTab);
    if (ItemStack.isSameItem(stack, oldStack)) {
      updateColorButton();
      return;
    }
    if (!oldStack.isEmpty() && stack.isEmpty()) updateTitle();
    oldStack = stack.copy();
    clearWidgets();
    initButtons();
    if (ItemCardMain.isCard(stack)) {
      int slot = panel.getCardSlot(stack);
      if (stack.getItem() instanceof ItemCardText)
        addRenderableWidget(
            new GuiButtonGeneral(
                    leftPos + imageWidth - 24,
                    topPos + 42 + 17 * 2,
                    16,
                    16,
                    Component.translatable("screen.ic2r_addons.control.text"),
                    (button) -> {
                      actionPerformed(button, ID_TEXT);
                    })
                .setGradient());
      List<PanelSetting> settingsList = ((IItemCard) stack.getItem()).getSettingsList();

      int hy = font.lineHeight + 1;
      int y = 1;
      if (settingsList != null)
        for (PanelSetting panelSetting : settingsList) {
          addRenderableWidget(
              new GuiInfoPanelCheckBox(
                  leftPos + 28, topPos + 28 + hy * y, panelSetting, panel, slot, font));
          y++;
        }
      if (!modified) {
        textboxTitle =
            new EditBox(font, leftPos + 7, topPos + 16, 162, 18, null, CommonComponents.EMPTY);
        textboxTitle.setValue(new ItemCardReader(stack).getTitle());
        addWidget(textboxTitle);
        setInitialFocus(textboxTitle);
      }
    } else {
      modified = false;
      textboxTitle = null;
    }
  }

  @Override
  protected void renderBg(GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
    super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
    if (textboxTitle != null) textboxTitle.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
