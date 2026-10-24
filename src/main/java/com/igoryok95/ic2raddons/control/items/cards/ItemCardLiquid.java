// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.*;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.renderers.RenderHelper;
import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class ItemCardLiquid extends ItemCardMain implements IHasBars {

  public ItemCardLiquid(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) {
      reader.reset();
      return CardState.NO_TARGET;
    }

    List<FluidInfo> list = CrossModLoader.getAllTanks(level, target);
    if (list == null || list.size() < 1) {
      reader.reset();
      return CardState.NO_TARGET;
    }

    FluidInfo tank = list.get(0);
    tank.write(reader);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    long capacity = reader.getLong("capacity");
    long amount = reader.getLong("amount");

    if ((settings & 1) > 0) {
      String name = reader.getString("name");
      if (name.isEmpty())
        name =
            com.igoryok95.ic2raddons.control.utils.Translator.get(
                "message.ic2r_addons.control.None");
      result.add(new PanelString("message.ic2r_addons.control.InfoPanelName", name, showLabels));
    }
    if ((settings & 2) > 0)
      result.add(
          new PanelString("message.ic2r_addons.control.InfoPanelAmount", amount, "mB", showLabels));
    if ((settings & 4) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelFree",
              (double) capacity - amount,
              "mB",
              showLabels));
    if ((settings & 8) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCapacity", capacity, "mB", showLabels));
    if ((settings & 16) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelPercentage",
              capacity == 0 ? 100 : (amount * 100 / capacity),
              showLabels));
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(5);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidName", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidAmount", 2));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidFree", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidCapacity", 8));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelLiquidPercentage", 16));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelShowBar", 1024));
    return result;
  }

  @Override
  public boolean isRemoteCard() {
    return true;
  }

  // IHasBars
  @Override
  public boolean enableBars(ItemStack stack) {
    return true;
  }

  @Override
  public void renderBars(
      float displayWidth,
      float displayHeight,
      ICardReader reader,
      PoseStack matrixStack,
      MultiBufferSource buffer) {
    float x = -0.5F + 1 / 16.0F;
    float y = -0.5F + 1 / 16.0F;
    float z = 0;

    String fluidName = reader.getString("fluidName");
    long capacity = reader.getLong("capacity");
    if (fluidName.isEmpty() || capacity <= 0) return;

    ResourceLocation id = ResourceLocation.tryParse(fluidName);
    if (id == null) return;
    Fluid fluid = BuiltInRegistries.FLUID.get(id);
    if (fluid == Fluids.EMPTY) return;
    IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(fluid);
    TextureAtlasSprite sprite =
        Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(fluidExt.getStillTexture());
    if (sprite == null) return;

    float width = 14 / 16.0F * Math.min(reader.getLong("amount"), capacity) / capacity;
    float height = 0.4375F;

    int color = fluidExt.getTintColor();
    if ((color >>> 24) == 0) color |= 0xFF000000;

    matrixStack.pushPose();
    matrixStack.scale(displayWidth / 0.875f, displayHeight / 0.875f, 1);
    RenderHelper.texturedRect(
        matrixStack,
        buffer,
        InventoryMenu.BLOCK_ATLAS,
        x,
        y + 0.4375F / 2,
        x + 0.875F,
        y + 0.4375F / 2 + height,
        z,
        sprite.getU0(),
        sprite.getV0(),
        sprite.getU1(),
        sprite.getV1(),
        color);
    IHasBars.drawTransparentRect(
        matrixStack,
        buffer,
        x + 0.875F - width,
        y + height + 0.4375F / 2,
        x,
        y + 0.4375F / 2,
        -0.0001F,
        0xB0000000);
    matrixStack.popPose();
  }
}
