package com.chocohead.advsolar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;

public class MolecularItemRenderer extends BlockEntityWithoutLevelRenderer {
  private final MolecularRenderer renderer = new MolecularRenderer(null);

  public MolecularItemRenderer() {
    super(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void renderByItem(
      ItemStack s,
      ItemDisplayContext c,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    renderer.render(null, 0, pose, buffers, light, overlay);
  }
}
