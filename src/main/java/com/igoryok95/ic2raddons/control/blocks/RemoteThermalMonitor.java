// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.blocks;

import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingHorizontal;
import com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityRemoteThermalMonitor;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityThermalMonitor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RemoteThermalMonitor extends FacingHorizontal {
  public static final MapCodec<RemoteThermalMonitor> CODEC = simpleCodec(RemoteThermalMonitor::new);

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  public RemoteThermalMonitor(Properties properties) {
    super(FacingBlock.metal(properties));
  }

  @Override
  protected BlockEntityFacing createBlockEntity(BlockPos pos, BlockState state) {
    return ModTileEntityTypes.remote_thermo.get().create(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    BlockEntity te = level.getBlockEntity(pos);
    if (!(te instanceof TileEntityRemoteThermalMonitor)) return InteractionResult.PASS;
    if (!level.isClientSide())
      player.openMenu((TileEntityRemoteThermalMonitor) te, buf -> buf.writeBlockPos(pos));
    return InteractionResult.SUCCESS;
  }

  @Override
  protected int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
    BlockEntity te = blockAccess.getBlockEntity(pos);
    if (!(te instanceof TileEntityThermalMonitor)) return 0;
    return ((TileEntityThermalMonitor) te).getPowered() ? 15 : 0;
  }

  @Override
  protected boolean isSignalSource(BlockState state) {
    return true;
  }
}
