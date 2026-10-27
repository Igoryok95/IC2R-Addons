// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.network;

import com.igoryok95.ic2raddons.control.lib.network.PacketTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class NetworkHelper {

  public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
    if (player.connection == null || !player.connection.hasChannel(message)) return;
    PacketDistributor.sendToPlayer(player, message);
  }

  // client
  public static void sendToServer(CustomPacketPayload message) {
    PacketDistributor.sendToServer(message);
  }

  // server
  public static void sendPacketToAllAround(Level level, BlockPos pos, CustomPacketPayload message) {
    if (level instanceof ServerLevel serverLevel)
      PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), message);
  }

  // server
  public static void updateClientTileEntity(
      ServerPlayer crafter, BlockPos pos, int type, int value) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("type", type);
    tag.putInt("value", value);
    sendToPlayer(crafter, new PacketTileEntity(pos, tag));
  }

  public static void updateClientTileEntity(
      ServerPlayer crafter, BlockPos pos, int type, double value) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("type", type);
    tag.putDouble("value", value);
    sendToPlayer(crafter, new PacketTileEntity(pos, tag));
  }

  public static void updateClientTileEntity(ServerPlayer crafter, BlockPos pos, CompoundTag tag) {
    sendToPlayer(crafter, new PacketTileEntity(pos, tag));
  }

  public static void updateClientTileEntity(Level level, BlockPos pos, CompoundTag tag) {
    sendPacketToAllAround(level, pos, new PacketTileEntity(pos, tag));
  }

  // client
  public static void updateSeverTileEntity(BlockPos pos, int type, String string) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("type", type);
    tag.putString("string", string);
    sendToServer(new PacketTileEntity(pos, tag));
  }

  public static void updateSeverTileEntity(BlockPos pos, int type, int value) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("type", type);
    tag.putInt("value", value);
    sendToServer(new PacketTileEntity(pos, tag));
  }

  public static void updateSeverTileEntity(BlockPos pos, CompoundTag tag) {
    sendToServer(new PacketTileEntity(pos, tag));
  }
}
