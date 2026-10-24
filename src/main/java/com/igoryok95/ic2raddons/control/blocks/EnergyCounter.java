// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.blocks;

import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock;
import com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityEnergyCounter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class EnergyCounter extends FacingBlock {
  public static final MapCodec<EnergyCounter> CODEC = simpleCodec(EnergyCounter::new);

  public EnergyCounter(Properties properties) {
    super(metal(properties));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  protected BlockEntityFacing createBlockEntity(BlockPos pos, BlockState state) {
    return new TileEntityEnergyCounter(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide
        ? null
        : (world, pos, block, tile) -> {
          if (tile instanceof TileEntityEnergyCounter counter) counter.tick();
        };
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (level.getBlockEntity(pos) instanceof TileEntityEnergyCounter counter) {
      if (!level.isClientSide) player.openMenu(counter, buffer -> buffer.writeBlockPos(pos));
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }
}
