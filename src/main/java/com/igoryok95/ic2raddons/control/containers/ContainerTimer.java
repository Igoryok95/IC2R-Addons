// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.google.common.collect.Lists;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityTimer;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ContainerTimer extends AbstractContainerMenu {
  public TileEntityTimer te;
  private int lastTime;
  private boolean lastIsWorking;
  public List<ServerPlayer> listeners = Lists.newArrayList();

  public ContainerTimer(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityTimer) ContainerBase.getBlockEntity(inventory, data));
  }

  public ContainerTimer(int windowId, Inventory inventory, TileEntityTimer te) {
    super(ModContainerTypes.timer.get(), windowId);
    this.te = te;
    lastTime = -1;
    if (inventory.player instanceof ServerPlayer player) listeners.add(player);
  }

  @Override
  public boolean stillValid(Player player) {
    return player.distanceToSqr(
            te.getBlockPos().getX() + 0.5D,
            te.getBlockPos().getY() + 0.5D,
            te.getBlockPos().getZ() + 0.5D)
        <= 64.0D;
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    int time = te.getTime();
    boolean isWorking = te.getIsWorking();
    for (ServerPlayer listener : listeners) {
      if (lastTime != time)
        NetworkHelper.updateClientTileEntity(listener, te.getBlockPos(), 1, time);
      if (lastIsWorking != isWorking)
        NetworkHelper.updateClientTileEntity(listener, te.getBlockPos(), 2, isWorking ? 1 : 0);
    }
    lastTime = time;
    lastIsWorking = isWorking;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    listeners.clear();
  }
}
