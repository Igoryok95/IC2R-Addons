// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.blocks;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlockActive;
import com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanel;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InfoPanel extends FacingBlockActive {
  public static final MapCodec<InfoPanel> CODEC = simpleCodec(InfoPanel::new);

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  public InfoPanel(Properties properties) {
    super(metal(properties).lightLevel(state -> state.getValue(ACTIVE) ? 10 : 0));
  }

  @Override
  protected BlockEntityFacing createBlockEntity(BlockPos pos, BlockState state) {
    return ModTileEntityTypes.info_panel.get().create(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    BlockEntity te = level.getBlockEntity(pos);
    if (!(te instanceof TileEntityInfoPanel)) return InteractionResult.PASS;
    if (!level.isClientSide()
        && EnergyControl.altPressed.get(player)
        && ((TileEntityInfoPanel) te).getFacing() == hit.getDirection())
      if (((TileEntityInfoPanel) te)
          .runTouchAction(player.getMainHandItem(), pos, hit.getLocation()))
        return InteractionResult.SUCCESS;
    if (!level.isClientSide())
      player.openMenu((TileEntityInfoPanel) te, buf -> buf.writeBlockPos(pos));
    return InteractionResult.SUCCESS;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return super.getStateForPlacement(context)
        .setValue(ACTIVE, context.getLevel().hasNeighborSignal(context.getClickedPos()));
  }

  @Override
  protected void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block block,
      BlockPos fromPos,
      boolean isMoving) {
    if (level.isClientSide()) return;

    boolean flag = state.getValue(ACTIVE);
    if (flag == level.hasNeighborSignal(pos)) return;

    if (flag) level.scheduleTick(pos, this, 4);
    else {
      level.setBlock(pos, state.cycle(ACTIVE), 2);
      updateExtenders(state, level, pos);
    }
  }

  @Override
  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (state.getValue(ACTIVE) && !level.hasNeighborSignal(pos)) {
      level.setBlock(pos, state.cycle(ACTIVE), 2);
      updateExtenders(state, level, pos);
    }
  }

  private void updateExtenders(BlockState state, Level level, BlockPos pos) { // server
    BlockEntity be = level.getBlockEntity(pos);
    if (be instanceof TileEntityInfoPanel)
      ((TileEntityInfoPanel) be).updateExtenders(level, !state.getValue(ACTIVE));
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }
}
