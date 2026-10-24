// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.utils.StringUtils;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCardInventory extends ItemCardMain {

  public ItemCardInventory(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockEntity te = level.getBlockEntity(target);
    CompoundTag tag = CrossModLoader.getInventoryData(te);
    if (tag == null) return CardState.NO_TARGET;
    reader.reset();
    reader.copyFrom(tag);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = new LinkedList<PanelString>();
    if (isServer) {
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTotalItems",
              reader.getInt("items"),
              showLabels));
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelSlotsUsed",
              String.format("%s/%s", reader.getInt("used"), reader.getInt("size")),
              showLabels));
    } else {
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelName", reader.getString("name"), showLabels));
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelTotalItems",
              reader.getInt("items"),
              showLabels));
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelSlotsUsed",
              String.format("%s/%s", reader.getInt("used"), reader.getInt("size")),
              showLabels));
      result.add(
          new PanelString(
              "message.ic2r_addons.control.InfoPanelSidedInventory",
              com.igoryok95.ic2raddons.control.utils.Translator.get(
                  reader.getBoolean("sided")
                      ? "message.ic2r_addons.control.yes"
                      : "message.ic2r_addons.control.no"),
              showLabels));
      for (int i = 0; i < 6; i++)
        if (reader.hasField("slot" + Integer.toString(i))) {
          ItemStack stack =
              ItemStack.parseOptional(
                  level.registryAccess(), reader.getTag("slot" + Integer.toString(i)));
          result.add(
              new PanelString(
                  String.format("message.ic2r_addons.control.InfoPanelSlot%d", i + 1),
                  StringUtils.getItemName(stack) + " x" + Integer.toString(stack.getCount()),
                  showLabels));
        }
    }
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    return null;
  }
}
