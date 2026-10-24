// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import com.igoryok95.ic2raddons.control.crossmod.CrossModLoader;
import com.igoryok95.ic2raddons.control.crossmod.ModIDs;
import com.igoryok95.ic2raddons.control.utils.Translator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCardAppEng extends ItemCardMain {

  public ItemCardAppEng(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockEntity te = level.getBlockEntity(target);
    CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.APPLIED_ENERGISTICS).getCardData(te);
    if (tag == null) return CardState.NO_TARGET;
    reader.reset();
    reader.copyFrom(tag);
    return CardState.OK;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = reader.getTitleList();
    switch (reader.getInt("type")) {
      case 1:
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelTotalNodes",
                reader.getInt("nodes"),
                showLabels));
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelStorageCells",
                reader.getInt("cells"),
                showLabels));
        if (isServer) {
          result.add(
              new PanelString(
                  String.format(
                      "%d of %d Bytes Used",
                      reader.getInt("bytesUsed"), reader.getInt("bytesTotal"))));
          result.add(
              new PanelString(
                  String.format(
                      "%d of %d Types", reader.getInt("typesUsed"), reader.getInt("typesTotal"))));
        } else {
          result.add(
              new PanelString(
                  Translator.get(
                      "message.ic2r_addons.control.InfoPanelBytesUsed",
                      reader.getInt("bytesUsed"),
                      reader.getInt("bytesTotal"))));
          result.add(
              new PanelString(
                  Translator.get(
                      "message.ic2r_addons.control.InfoPanelTypes",
                      reader.getInt("typesUsed"),
                      reader.getInt("typesTotal"))));
        }
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelTotalItems",
                reader.getInt("items"),
                showLabels));
        break;
      case 2:
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelName", reader.getString("name"), showLabels));
        result.add(
            new PanelString(
                "message.ic2r_addons.control.InfoPanelSize", reader.getInt("size"), showLabels));
        break;
    }
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    return null;
  }
}
