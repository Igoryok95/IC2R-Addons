// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.google.common.collect.Lists;
import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.containers.ContainerKitAssembler;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardMain;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardReader;
import com.igoryok95.ic2raddons.control.lib.gui.GuiContainerBase;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityKitAssembler;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class GuiKitAssembler extends GuiContainerBase<ContainerKitAssembler> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          EnergyControl.MODID, "textures/gui/gui_kit_assembler.png");

  private ContainerKitAssembler container;

  public GuiKitAssembler(ContainerKitAssembler container, Inventory inventory, Component title) {
    super(container, inventory, title, TEXTURE, 176, 182);
    this.container = container;
  }

  @Override
  protected void renderTooltip(GuiGraphics matrixStack, int mouseX, int mouseY) {
    Slot slot = container.getSlot(TileEntityKitAssembler.SLOT_INFO);
    if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.isActive())
      renderInfoToolTip(matrixStack, slot, mouseX, mouseY);
    else super.renderTooltip(matrixStack, mouseX, mouseY);
    if (isHovering(165, 16, 4, 52, mouseX, mouseY))
      matrixStack.renderTooltip(
          font,
          Component.translatable(
              "tooltip.ic2r_addons.control.assembler_energy",
              (int) container.te.getEnergy(),
              TileEntityKitAssembler.CAPACITY),
          mouseX,
          mouseY);
  }

  private void renderInfoToolTip(GuiGraphics matrixStack, Slot slot, int x, int y) {
    ItemStack stack = slot.getItem();
    if (!ItemCardMain.isCard(stack)) return;
    List<Component> stackList =
        stack.getTooltipLines(
            Item.TooltipContext.of(minecraft.level),
            minecraft.player,
            minecraft.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED
                : TooltipFlag.Default.NORMAL);
    List<Component> list = Lists.<Component>newArrayList();
    if (stackList.size() > 0) list.add(stackList.get(0));
    List<PanelString> data = new ItemCardReader(stack).getAllData();
    if (data != null)
      for (PanelString panelString : data) {
        if (panelString.textLeft != null)
          list.add(Component.literal(ChatFormatting.GRAY + panelString.textLeft));
      }
    matrixStack.renderTooltip(font, list, stack.getTooltipImage(), x, y);
  }

  @Override
  protected void renderBg(GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
    super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

    int energyHeight = container.te.getEnergyFactor();
    if (energyHeight > 0)
      matrixStack.blit(
          TEXTURE,
          leftPos + 165,
          topPos + 16 + (52 - energyHeight),
          176,
          17 + 52 - energyHeight,
          4,
          energyHeight,
          256,
          256);
    int productionWidth = container.te.getProductionFactor();
    if (productionWidth > 0)
      matrixStack.blit(TEXTURE, leftPos + 86, topPos + 35, 176, 0, productionWidth, 17, 256, 256);
  }

  @Override
  protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {
    drawCenteredText(matrixStack, title, imageWidth, 6);
  }
}
