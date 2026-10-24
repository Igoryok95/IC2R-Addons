/* Adapted from ForestryCE RenderEngine, thedarkcolour and Forestry contributors.
 * SPDX-License-Identifier: LGPL-3.0-or-later. See LICENSES/ForestryCE-LGPL-3.0.txt. */
package com.igoryok95.ic2raddons.bridge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ElectricEngineRenderer
    implements BlockEntityRenderer<com.igoryok95.ic2raddons.bridge.bc.ElectricEngine> {
  private static final float[] ANGLE_MAP = new float[6];

  private enum Textures {
    BASE,
    PISTON,
    EXTENSION,
    TRUNK_HIGHEST,
    TRUNK_HIGHER,
    TRUNK_HIGH,
    TRUNK_MEDIUM,
    TRUNK_LOW
  }

  private final ResourceLocation[] textures;
  private final ModelPart boiler;
  private final ModelPart trunk;
  private final ModelPart piston;
  private final ModelPart extension;

  static {
    ANGLE_MAP[Direction.EAST.ordinal()] = -Mth.HALF_PI;
    ANGLE_MAP[Direction.NORTH.ordinal()] = -Mth.HALF_PI;
    ANGLE_MAP[Direction.WEST.ordinal()] = Mth.HALF_PI;
    ANGLE_MAP[Direction.SOUTH.ordinal()] = Mth.HALF_PI;
    ANGLE_MAP[Direction.UP.ordinal()] = 0;
    ANGLE_MAP[Direction.DOWN.ordinal()] = Mth.PI;
  }

  public ElectricEngineRenderer(BlockEntityRendererProvider.Context ctx) {
    ModelPart root = createBodyLayer().bakeRoot();

    this.boiler = root.getChild("boiler");
    this.trunk = root.getChild("trunk");
    this.piston = root.getChild("piston");
    this.extension = root.getChild("extension");

    this.textures =
        new ResourceLocation[] {
          texture("engine_tin_base"),
          texture("engine_tin_piston"),
          texture("engine_tin_extension"),
          texture("engine_trunk_highest"),
          texture("engine_trunk_higher"),
          texture("engine_trunk_high"),
          texture("engine_trunk_medium"),
          texture("engine_trunk_low"),
        };
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    root.addOrReplaceChild(
        "boiler",
        CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 16, 6, 16),
        PartPose.offset(0, 0, 0));
    root.addOrReplaceChild(
        "trunk",
        CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 8, 12, 8),
        PartPose.offset(4, 4, 4));
    root.addOrReplaceChild(
        "piston",
        CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 12, 4, 12),
        PartPose.offset(2, 6, 2));
    root.addOrReplaceChild(
        "extension",
        CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 10, 2, 10),
        PartPose.offset(3, 5, 3));

    return LayerDefinition.create(mesh, 64, 32);
  }

  @Override
  public void render(
      com.igoryok95.ic2raddons.bridge.bc.ElectricEngine engine,
      float partialTick,
      PoseStack stack,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    stack.pushPose();

    // apply direction rotation
    stack.translate(0.5, 0.5, 0.5);
    Direction orientation = engine == null ? Direction.UP : engine.getFacing();
    switch (orientation) {
      case EAST, WEST, DOWN -> stack.mulPose(Axis.ZP.rotation(ANGLE_MAP[orientation.ordinal()]));
      default -> stack.mulPose(Axis.XP.rotation(ANGLE_MAP[orientation.ordinal()]));
    }
    stack.translate(-0.5, -0.5, -0.5);

    // render base
    this.boiler.render(
        stack,
        buffers.getBuffer(RenderType.entityCutout(this.textures[Textures.BASE.ordinal()])),
        light,
        overlay,
        0xFFFFFFFF);

    // render piston with smooth lerp
    float step = getPistonStep(engine, partialTick);
    float tfactor = step / 16;
    stack.translate(0, tfactor, 0);
    this.piston.render(
        stack,
        buffers.getBuffer(RenderType.entityCutout(this.textures[Textures.PISTON.ordinal()])),
        light,
        overlay,
        0xFFFFFFFF);
    stack.translate(0, -tfactor, 0);

    // render trunk with color based on heat
    ResourceLocation texture =
        this.textures[
            engine != null && engine.getActive()
                ? Textures.TRUNK_MEDIUM.ordinal()
                : Textures.TRUNK_LOW.ordinal()];
    this.trunk.render(
        stack, buffers.getBuffer(RenderType.entityCutout(texture)), light, overlay, 0xFFFFFFFF);

    // render piston sleeve
    float chamberf = 2F / 16F;
    if (step > 0) {
      VertexConsumer buffer =
          buffers.getBuffer(RenderType.entityCutout(this.textures[Textures.EXTENSION.ordinal()]));

      for (int i = 0; i <= step + 2; i += 2) {
        this.extension.render(stack, buffer, light, overlay, 0xFFFFFFFF);
        stack.translate(0, chamberf, 0);
      }
    }

    stack.popPose();
  }

  private static ResourceLocation texture(String name) {
    return com.igoryok95.ic2raddons.solar.IC2RAddons.id("textures/block/engine/" + name + ".png");
  }

  private static float getPistonStep(
      com.igoryok95.ic2raddons.bridge.bc.ElectricEngine engine, float partialTick) {
    if (engine == null) return 2.0F;
    if (!engine.getActive()) return 0;
    double progress = ((engine.getLevel().getGameTime() + partialTick) % 20.0) / 20.0;
    return (float) ((progress <= 0.5 ? progress : 1.0 - progress) * 12.0);
  }
}
