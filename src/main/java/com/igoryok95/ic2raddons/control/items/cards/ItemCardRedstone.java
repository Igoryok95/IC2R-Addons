// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.items.cards;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.api.PanelSetting;
import com.igoryok95.ic2raddons.control.api.PanelString;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemCardRedstone extends ItemCardMain {

  public ItemCardRedstone(Properties properties) {
    super(properties);
  }

  @Override
  public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
    BlockPos target = reader.getTarget();
    if (target == null) return CardState.NO_TARGET;

    BlockState state = level.getBlockState(target);
    Block block = state.getBlock();
    if (block != Blocks.AIR) {
      reader.setString("name", block.getDescriptionId());
      reader.setBoolean("isPowered", level.hasNeighborSignal(target));
      reader.setInt("indirect", level.getBestNeighborSignal(target));
      reader.setBoolean("canProvide", state.isSignalSource());
      reader.setString(
          "powered",
          String.format(
              "D:%d U:%d N:%d S:%d W:%d E:%d",
              level.getSignal(target.below(), Direction.DOWN),
              level.getSignal(target.above(), Direction.UP),
              level.getSignal(target.north(), Direction.NORTH),
              level.getSignal(target.south(), Direction.SOUTH),
              level.getSignal(target.west(), Direction.WEST),
              level.getSignal(target.east(), Direction.EAST)));
      reader.setString(
          "week",
          String.format(
              "D:%d U:%d N:%d S:%d W:%d E:%d",
              state.getSignal(level, target, Direction.UP),
              state.getSignal(level, target, Direction.DOWN),
              state.getSignal(level, target, Direction.SOUTH),
              state.getSignal(level, target, Direction.NORTH),
              state.getSignal(level, target, Direction.EAST),
              state.getSignal(level, target, Direction.WEST)));
      reader.setString(
          "strong",
          String.format(
              "D:%d U:%d N:%d S:%d W:%d E:%d",
              state.getDirectSignal(level, target, Direction.UP),
              state.getDirectSignal(level, target, Direction.DOWN),
              state.getDirectSignal(level, target, Direction.SOUTH),
              state.getDirectSignal(level, target, Direction.NORTH),
              state.getDirectSignal(level, target, Direction.EAST),
              state.getDirectSignal(level, target, Direction.WEST)));
      return CardState.OK;
    }
    return CardState.NO_TARGET;
  }

  @Override
  public List<PanelString> getStringData(
      Level level, int settings, ICardReader reader, boolean isServer, boolean showLabels) {
    List<PanelString> result = new LinkedList<>();
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelName", reader.getString("name"), showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelBlockPowered",
            com.igoryok95.ic2raddons.control.utils.Translator.get(
                reader.getBoolean("isPowered")
                    ? "message.ic2r_addons.control.yes"
                    : "message.ic2r_addons.control.no"),
            showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelIndirectPower",
            reader.getInt("indirect"),
            showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelCanProvidePower",
            com.igoryok95.ic2raddons.control.utils.Translator.get(
                reader.getBoolean("canProvide")
                    ? "message.ic2r_addons.control.yes"
                    : "message.ic2r_addons.control.no"),
            showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelRedstonePower",
            reader.getString("powered"),
            showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelSendWeekPower",
            reader.getString("week"),
            showLabels));
    result.add(
        new PanelString(
            "message.ic2r_addons.control.InfoPanelSendStrongPower",
            reader.getString("strong"),
            showLabels));
    return result;
  }

  @Override
  public List<PanelSetting> getSettingsList() {
    return null;
  }
}
