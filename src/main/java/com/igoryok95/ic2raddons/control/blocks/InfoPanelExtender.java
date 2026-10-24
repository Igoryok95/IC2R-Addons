// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.blocks;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlockActive;
import com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanel;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityInfoPanelExtender;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InfoPanelExtender extends FacingBlockActive {
  public static final MapCodec<InfoPanelExtender> CODEC = simpleCodec(InfoPanelExtender::new);

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  public InfoPanelExtender(Properties properties) {
    super(metal(properties).lightLevel(state -> state.getValue(ACTIVE) ? 10 : 0));
  }

  @Override
  protected BlockEntityFacing createBlockEntity(BlockPos pos, BlockState state) {
    return ModTileEntityTypes.info_panel_extender.get().create(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (level.isClientSide()) return InteractionResult.PASS;
    BlockEntity te = level.getBlockEntity(pos);
    if (!(te instanceof TileEntityInfoPanelExtender)) return InteractionResult.PASS;
    TileEntityInfoPanel panel = ((TileEntityInfoPanelExtender) te).getCore();
    if (panel == null) return InteractionResult.PASS;
    if (EnergyControl.altPressed.get(player) && panel.getFacing() == hit.getDirection())
      if (panel.runTouchAction(player.getMainHandItem(), pos, hit.getLocation()))
        return InteractionResult.SUCCESS;
    player.openMenu(panel, buf -> buf.writeBlockPos(pos));
    return InteractionResult.SUCCESS;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return super.getStateForPlacement(context)
        .setValue(ACTIVE, context.getLevel().hasNeighborSignal(context.getClickedPos()));
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }
}
