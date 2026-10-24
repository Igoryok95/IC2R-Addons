// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Base for the mod's block entity renderers. */
public abstract class TileRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

  @Override
  public void render(
      T te,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay) {
    if (te == null || te.isRemoved()) return;
    render(te, poseStack, buffer, packedLight, packedOverlay);
  }

  protected abstract void render(
      T te,
      PoseStack matrixStack,
      MultiBufferSource buffer,
      int combinedLight,
      int combinedOverlay);

  @Override
  public int getViewDistance() {
    return 65536;
  }
}
