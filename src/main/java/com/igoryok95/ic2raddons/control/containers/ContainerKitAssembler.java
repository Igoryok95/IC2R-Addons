// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.containers.slots.SlotCard;
import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotDischargeable;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotFilter;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotTransformer;
import com.igoryok95.ic2raddons.control.network.NetworkHelper;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityKitAssembler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerKitAssembler extends ContainerBase<TileEntityKitAssembler> {
  private double lastEnergy = -1;
  private double lastProduction = -1;

  public ContainerKitAssembler(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(windowId, inventory, (TileEntityKitAssembler) getBlockEntity(inventory, data));
  }

  public ContainerKitAssembler(int windowId, Inventory inventory, TileEntityKitAssembler te) {
    super(
        te,
        ModContainerTypes.kit_assembler.get(),
        windowId,
        ModItems.kit_assembler.get(),
        ContainerLevelAccess.create(te.getLevel(), te.getBlockPos()));
    if (inventory.player instanceof ServerPlayer player) listeners.add(player);
    // info card
    addSlot(new SlotCard(te, 0, 8, 17));

    addSlot(new SlotFilter(te, 1, 62, 17));
    addSlot(new SlotFilter(te, 2, 62, 17 + 18));
    addSlot(new SlotFilter(te, 3, 62, 17 + 18 * 2));
    addSlot(new SlotFilter(te, 4, 121, 35));

    addSlot(new SlotDischargeable(te, 5, 8, 17 + 18 * 2));
    addSlot(new SlotTransformer(te, 6, 8, 17 + 18));
    // inventory
    addPlayerInventorySlots(inventory, 166);
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    double energy = te.getEnergy();
    double production = te.getProduction();
    for (ServerPlayer listener : listeners)
      if (lastEnergy != energy || lastProduction != production) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", 1);
        tag.putDouble("energy", energy);
        tag.putDouble("production", production);
        tag.putInt("time", te.getRecipeTime());
        NetworkHelper.updateClientTileEntity(listener, te.getBlockPos(), tag);
      }
    lastEnergy = energy;
    lastProduction = production;
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    listeners.clear();
  }
}
