package com.igoryok95.ic2raddons.bridge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;

public final class EngineItemRenderer extends BlockEntityWithoutLevelRenderer {
  private final ElectricEngineRenderer renderer = new ElectricEngineRenderer(null);

  public EngineItemRenderer() {
    super(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void renderByItem(
      ItemStack item,
      ItemDisplayContext context,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    renderer.render(null, 0, pose, buffers, light, overlay);
  }
}
