// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.api;

import com.igoryok95.ic2raddons.control.renderers.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

/** Used to draw (progress) bars on Info Panels */
public interface IHasBars {

  boolean enableBars(ItemStack stack);

  void renderBars(
      float displayWidth,
      float displayHeight,
      ICardReader reader,
      PoseStack matrixStack,
      MultiBufferSource buffer);

  static void drawTransparentRect(
      PoseStack matrixStack,
      MultiBufferSource buffer,
      float left,
      float top,
      float right,
      float bottom,
      float zLevel,
      int color) {
    RenderHelper.fillRect(matrixStack, buffer, left, top, right, bottom, zLevel, color);
  }
}
