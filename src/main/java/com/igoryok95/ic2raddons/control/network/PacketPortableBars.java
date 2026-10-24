// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.network;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.containers.ContainerPortablePanel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// the portable panel GUI switched its progress bars on or off; the setting is saved in the panel
// stack
public record PacketPortableBars(boolean showBars) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<PacketPortableBars> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "portable_bars"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketPortableBars> STREAM_CODEC =
      ByteBufCodecs.BOOL
          .<RegistryFriendlyByteBuf>cast()
          .map(PacketPortableBars::new, PacketPortableBars::showBars);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  // server, main thread
  public static void handle(PacketPortableBars message, IPayloadContext ctx) {
    if (ctx.player().containerMenu instanceof ContainerPortablePanel container)
      container.setShowBars(message.showBars());
  }
}
