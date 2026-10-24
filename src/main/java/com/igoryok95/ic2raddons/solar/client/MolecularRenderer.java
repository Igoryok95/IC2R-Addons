package com.igoryok95.ic2raddons.solar.client;

import com.igoryok95.ic2raddons.solar.*;
import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

public class MolecularRenderer implements BlockEntityRenderer<TileEntityMolecularTransformer> {
  private final ModelPart model;

  public MolecularRenderer(BlockEntityRendererProvider.Context c) {
    var mesh = new MeshDefinition();
    var root = mesh.getRoot();
    root.addOrReplaceChild(
        "coreBottom",
        CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 4.0F, -5.0F, 10, 3, 10),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "coreWorkZone",
        CubeListBuilder.create().texOffs(0, 44).mirror().addBox(-3.0F, -4.0F, -3.0F, 6, 9, 6),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "coreTopElectr",
        CubeListBuilder.create().texOffs(25, 44).mirror().addBox(-2.0F, -8.0F, -1.466667F, 3, 2, 3),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "coreTopPlate",
        CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-5.0F, -7.0F, -4.5F, 9, 3, 9),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "firstElTop",
        CubeListBuilder.create().texOffs(20, 16).mirror().addBox(3.0F, -8.0F, -5.0F, 4, 3, 10),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "firstElBottom",
        CubeListBuilder.create().texOffs(49, 16).mirror().addBox(4.0F, 3.0F, -3.0F, 3, 5, 6),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    root.addOrReplaceChild(
        "secondElTop",
        CubeListBuilder.create().texOffs(20, 16).mirror().addBox(3.0F, -8.0F, -5.0F, 4, 3, 10),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
    root.addOrReplaceChild(
        "secondElBottom",
        CubeListBuilder.create().texOffs(49, 16).mirror().addBox(4.0F, 3.0F, -3.0F, 3, 5, 6),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
    root.addOrReplaceChild(
        "thirdElTop",
        CubeListBuilder.create().texOffs(20, 16).mirror().addBox(3.0F, -8.0F, -5.0F, 4, 3, 10),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
    root.addOrReplaceChild(
        "thirdElBottom",
        CubeListBuilder.create().texOffs(49, 16).mirror().addBox(4.0F, 3.0F, -3.0F, 3, 5, 6),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
    model = LayerDefinition.create(mesh, 128, 64).bakeRoot();
  }

  public void render(
      TileEntityMolecularTransformer m,
      float pt,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    pose.pushPose();
    pose.translate(.5, 1.5, .5);
    pose.scale(-1, -1, 1);
    model.render(
        pose,
        buffers.getBuffer(
            RenderType.entityTranslucent(
                IC2RAddons.id("textures/models/texturemoleculartransformer.png"))),
        light,
        overlay);
    pose.popPose();
    if (m != null && m.getActive() && com.igoryok95.ic2raddons.solar.AspConfig.ACTIVE_CORE.get()) {
      pose.pushPose();
      pose.translate(.5, .5, .5);
      pose.mulPose(
          net.minecraft.client.Minecraft.getInstance()
              .getEntityRenderDispatcher()
              .cameraOrientation());
      float time = m.getLevel().getGameTime() + pt;
      int frame = (int) time % 16;
      billboard(pose, buffers, overlay, "plazma", frame, 4, .35f, 192, 255, 255);
      billboard(
          pose,
          buffers,
          overlay,
          "particles",
          frame + 24,
          8,
          .4f + (float) Math.sin(time / 10) * .1f,
          255,
          255,
          255);
      pose.popPose();
    }
  }

  private static void billboard(
      PoseStack pose,
      MultiBufferSource buffers,
      int overlay,
      String texture,
      int frame,
      int grid,
      float size,
      int red,
      int green,
      int blue) {
    float u = (frame % grid) / (float) grid,
        v = (frame / grid) / (float) grid,
        edge = 1f / grid - .00001f;
    var out =
        buffers.getBuffer(
            RenderType.entityTranslucentEmissive(
                IC2RAddons.id("textures/models/" + texture + ".png")));
    var mat = pose.last();
    out.addVertex(mat, -size, -size, 0)
        .setColor(red, green, blue, 255)
        .setUv(u, v + edge)
        .setOverlay(overlay)
        .setLight(15728880)
        .setNormal(mat, 0, 0, 1);
    out.addVertex(mat, size, -size, 0)
        .setColor(red, green, blue, 255)
        .setUv(u + edge, v + edge)
        .setOverlay(overlay)
        .setLight(15728880)
        .setNormal(mat, 0, 0, 1);
    out.addVertex(mat, size, size, 0)
        .setColor(red, green, blue, 255)
        .setUv(u + edge, v)
        .setOverlay(overlay)
        .setLight(15728880)
        .setNormal(mat, 0, 0, 1);
    out.addVertex(mat, -size, size, 0)
        .setColor(red, green, blue, 255)
        .setUv(u, v)
        .setOverlay(overlay)
        .setLight(15728880)
        .setNormal(mat, 0, 0, 1);
  }
}
