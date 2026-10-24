// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.blocks;

import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingBlock;
import com.igoryok95.ic2raddons.control.lib.blocks.FacingHorizontalActive;
import com.igoryok95.ic2raddons.control.lib.tileentities.BlockEntityFacing;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityKitAssembler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class KitAssembler extends FacingHorizontalActive {
  public static final MapCodec<KitAssembler> CODEC = simpleCodec(KitAssembler::new);

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  public KitAssembler(Properties properties) {
    super(FacingBlock.metal(properties));
  }

  @Override
  protected BlockEntityFacing createBlockEntity(BlockPos pos, BlockState state) {
    return ModTileEntityTypes.kit_assembler.get().create(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    BlockEntity te = level.getBlockEntity(pos);
    if (!(te instanceof TileEntityKitAssembler)) return InteractionResult.PASS;
    if (!level.isClientSide())
      player.openMenu((TileEntityKitAssembler) te, buf -> buf.writeBlockPos(pos));
    return InteractionResult.SUCCESS;
  }
}
