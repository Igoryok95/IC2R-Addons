// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.IItemCard;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.containers.ContainerPortablePanel;
import com.igoryok95.ic2raddons.control.gui.controls.ToggleButton;
import com.igoryok95.ic2raddons.control.items.InventoryPortablePanel;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardMain;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardReader;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.network.PacketPortableBars;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GuiPortablePanel extends AbstractContainerScreen<ContainerPortablePanel> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.parse(EnergyControl.MODID + ":textures/gui/gui_portable_panel.png");
  private static final int ROWS_PER_PAGE = 14;
  private int page;
  private Button previousPage;
  private Button nextPage;
  private ToggleButton toggleBars;
  private boolean showBars;
  private Player player;

  private InventoryPortablePanel te;

  public GuiPortablePanel(ContainerPortablePanel container, Inventory inventory, Component title) {
    super(container, inventory, title);
    this.imageWidth = 226;
    this.imageHeight = 226;
    this.te = container.te;
    this.showBars = container.getShowBars();
    this.player = inventory.player;
  }

  @Override
  protected void init() {
    super.init();
    previousPage =
        addRenderableWidget(
            Button.builder(
                    Component.translatable("screen.ic2r_addons.control.previous"), button -> page--)
                .bounds(leftPos + 174, topPos + 53, 16, 16)
                .build());
    nextPage =
        addRenderableWidget(
            Button.builder(
                    Component.translatable("screen.ic2r_addons.control.next"), button -> page++)
                .bounds(leftPos + 174, topPos + 71, 16, 16)
                .build());
    previousPage.visible = nextPage.visible = false;
    toggleBars =
        addRenderableWidget(
            new ToggleButton(
                leftPos + 174,
                topPos + 89,
                16,
                16,
                Component.translatable("screen.ic2r_addons.control.bars"),
                showBars,
                button -> {
                  showBars = !showBars;
                  toggleBars.setPressed(showBars);
                  menu.setShowBars(showBars);
                  NetworkHelper.sendToServer(new PacketPortableBars(showBars));
                  button.setTooltip(Tooltip.create(barButtonTitle()));
                }));
    toggleBars.setTooltip(Tooltip.create(barButtonTitle()));
    toggleBars.visible = false;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean result = super.mouseClicked(mouseX, mouseY, button);
    // a clicked button keeps focus and stays highlighted after the mouse leaves it
    if (getFocused() instanceof Button) setFocused(null);
    return result;
  }

  private Component barButtonTitle() {
    return Component.translatable(showBars ? "gui.ec.portableBarsOn" : "gui.ec.portableBarsOff");
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
    super.render(context, mouseX, mouseY, partialTicks);
    renderTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics context, float partialTicks, int x, int y) {
    int left = (width - imageWidth) / 2;
    int top = (height - imageHeight) / 2;
    context.blit(TEXTURE, left, top, 0, 0, imageWidth, imageHeight, 256, 256);
  }

  @Override
  protected void renderLabels(GuiGraphics context, int x, int y) {
    ItemStack stack = te.getItem(InventoryPortablePanel.SLOT_CARD);
    if (ItemCardMain.isCard(stack)) {
      ItemCardReader reader = new ItemCardReader(stack);

      CardState state = reader.getState();
      List<PanelString> joinedData;
      if (state != CardState.OK && state != CardState.CUSTOM_ERROR)
        joinedData = ItemCardReader.getStateMessage(state);
      else
        joinedData =
            ((IItemCard) stack.getItem())
                .getStringData(player.level(), Integer.MAX_VALUE, reader, false, true);

      int pageCount = joinedData.isEmpty() ? 1 : (joinedData.size() - 1) / ROWS_PER_PAGE + 1;
      page = Math.max(0, Math.min(page, pageCount - 1));
      previousPage.visible = nextPage.visible = pageCount > 1;
      previousPage.active = page > 0;
      nextPage.active = page < pageCount - 1;
      Map<Integer, Double> bars = PortableProgressBars.forRows(stack, reader);
      toggleBars.visible = !bars.isEmpty();

      int firstRow = page * ROWS_PER_PAGE;
      int lastRow = Math.min(firstRow + ROWS_PER_PAGE, joinedData.size());
      for (int index = firstRow; index < lastRow; index++) {
        PanelString panelString = joinedData.get(index);
        int row = index - firstRow;
        if (showBars && bars.containsKey(index)) {
          int top = row * 10 + 9;
          context.fill(8, top, 168, top + 10, ARGB32.opaque(0x20343A));
          context.fill(
              8,
              top,
              8 + (int) Math.round(160 * bars.get(index)),
              top + 10,
              ARGB32.opaque(0x205E36));
        }
        if (panelString.textLeft != null)
          context.drawString(
              font, panelString.textLeft, 9, row * 10 + 10, ARGB32.opaque(0x06aee4), false);
        if (panelString.textCenter != null)
          context.drawString(
              font,
              panelString.textCenter,
              (168 - font.width(panelString.textCenter)) / 2,
              row * 10 + 10,
              ARGB32.opaque(0x06aee4),
              false);
        if (panelString.textRight != null)
          context.drawString(
              font,
              panelString.textRight,
              168 - font.width(panelString.textRight),
              row * 10 + 10,
              ARGB32.opaque(0x06aee4),
              false);
      }
      if (pageCount > 1)
        context.drawString(
            font,
            Component.translatable("message.ic2r_addons.control.pages", page + 1, pageCount),
            9,
            150,
            ARGB32.opaque(0x06aee4),
            false);
    } else {
      page = 0;
      previousPage.visible = nextPage.visible = false;
      toggleBars.visible = false;
    }
  }
}
