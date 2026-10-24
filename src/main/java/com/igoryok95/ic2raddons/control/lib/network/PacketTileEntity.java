// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.lib.network;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.tileentities.ITilePacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTileEntity(BlockPos pos, CompoundTag tag) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<PacketTileEntity> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "tile_entity"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTileEntity> STREAM_CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC,
          PacketTileEntity::pos,
          ByteBufCodecs.COMPOUND_TAG,
          PacketTileEntity::tag,
          PacketTileEntity::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  // server
  public static void handleServer(PacketTileEntity message, IPayloadContext ctx) {
    Level level = ctx.player().level();
    if (!level.isLoaded(message.pos) || !mayEdit(ctx.player(), message.pos)) return;
    BlockEntity te = level.getBlockEntity(message.pos);
    if (te instanceof ITilePacketHandler handler) handler.onServerMessageReceived(message.tag);
  }

  // client
  public static boolean mayEdit(net.minecraft.world.entity.player.Player player, BlockPos pos) {
    return player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= 64
        && !player.isSpectator()
        && player.level().mayInteract(player, pos);
  }

  // client
  public static void handleClient(PacketTileEntity message, IPayloadContext ctx) {
    Level level = ctx.player().level();
    BlockEntity te = level.getBlockEntity(message.pos);
    if (te instanceof ITilePacketHandler handler) handler.onClientMessageReceived(message.tag);
  }
}
