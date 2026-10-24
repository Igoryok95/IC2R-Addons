// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.IHasBars;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.utils.DataHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCardEnergy extends ItemCardMain implements IHasBars {
  private static final int BAR_COLOR = 0xFF2ECC40; // filled part
  private static final int EMPTY_COLOR =
      0xB0000000; // darkens the empty part, as on the liquid card

  public ItemCardEnergy(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockEntity te = level.getBlockEntity(target);
    if (te == null) return CardState.NO_TARGET;

    CompoundTag tag = CrossModLoader.getEnergyData(te);
    if (tag != null) {
      reader.setDouble(DataHelper.ENERGY, tag.getDouble(DataHelper.ENERGY));
      reader.setDouble(DataHelper.CAPACITY, tag.getDouble(DataHelper.CAPACITY));
      reader.setString(DataHelper.EUTYPE, tag.getString(DataHelper.EUTYPE));
      return CardState.OK;
    }
    return CardState.NO_TARGET;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();

    double energy = reader.getDouble(DataHelper.ENERGY);
    double storage = reader.getDouble(DataHelper.CAPACITY);
    String euType = reader.getString(DataHelper.EUTYPE);

    if ((settings & 1) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelEnergy", energy, euType, showLabels));
    if ((settings & 4) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelCapacity", storage, euType, showLabels));
    if ((settings & 2) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelFree", storage - energy, euType, showLabels));
    if ((settings & 8) > 0)
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelPercentage",
              storage == 0 ? 100 : ((energy / storage) * 100),
              showLabels));
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    List<PanelSetting> result = new ArrayList<>(5);
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelEnergy", 1));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelFree", 2));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelCapacity", 4));
    result.add(new PanelSetting("message.ic2r_addons.control.cbInfoPanelPercentage", 8));
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

  // same place and size as the liquid card bar: a band across the middle of the screen
  @Override
  public void renderBars(
      float displayWidth,
      float displayHeight,
      ICardReader reader,
      PoseStack matrixStack,
      MultiBufferSource buffer) {
    double storage = reader.getDouble(DataHelper.CAPACITY);
    if (storage <= 0) return;
    float x = -0.5F + 1 / 16.0F;
    float y = -0.5F + 1 / 16.0F + 0.4375F / 2;
    float height = 0.4375F;
    float width =
        14 / 16.0F * (float) (Math.min(reader.getDouble(DataHelper.ENERGY), storage) / storage);

    matrixStack.scale(displayWidth / 0.875F, displayHeight / 0.875F, 1);
    IHasBars.drawTransparentRect(matrixStack, buffer, x + 0.875F, y + height, x, y, 0, BAR_COLOR);
    IHasBars.drawTransparentRect(
        matrixStack, buffer, x + 0.875F - width, y + height, x, y, -0.0001F, EMPTY_COLOR);
    matrixStack.scale(0.875F / displayWidth, 0.875F / displayHeight, 1);
  }
}
