// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.tileentities;

import com.igoryok95.ic2raddons.control.containers.ContainerEnergyCounter;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.init.ModTileEntityTypes;
import com.igoryok95.ic2raddons.control.lib.tileentities.TileEntityInventory;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.*;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Measures real native EU throughput once per tick, including ticks with zero traffic. */
public final class TileEntityEnergyCounter extends TileEntityInventory
    implements IEnergyConductor, MenuProvider, ITilePacketHandler {
  private boolean registered;
  private double total, current;
  private long samples;

  public TileEntityEnergyCounter(BlockPos pos, BlockState state) {
    super(
        state.is(ModItems.average_counter.get())
            ? ModTileEntityTypes.average_counter.get()
            : ModTileEntityTypes.energy_counter.get(),
        pos,
        state);
  }

  @Override
  public int getContainerSize() {
    return 1;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return slot == 0 && stack.is(Ic2Items.TRANSFORMER_UPGRADE);
  }

  public int maximumPacket() {
    return 32
        << (2
            * Math.min(4, getItem(0).is(Ic2Items.TRANSFORMER_UPGRADE) ? getItem(0).getCount() : 0));
  }

  public double total() {
    return total;
  }

  public double current() {
    return current;
  }

  public double average() {
    return samples == 0 ? 0 : total / samples;
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (!level.isClientSide && !registered) {
      EnergyNet.instance.addBlockEntityTile(this);
      registered = true;
    }
  }

  private void disconnect() {
    if (registered && level != null && !level.isClientSide) EnergyNet.instance.removeTile(this);
    registered = false;
  }

  @Override
  public void setRemoved() {
    disconnect();
    super.setRemoved();
  }

  @Override
  public void onChunkUnloaded() {
    disconnect();
    super.onChunkUnloaded();
  }

  public void tick() {
    if (level == null || level.isClientSide) return;
    var stats = EnergyNet.instance.getNodeStats(this);
    current = stats == null ? 0 : Math.max(stats.getEnergyIn(), stats.getEnergyOut());
    if (!Double.isFinite(current) || current < 0) current = 0;
    total += current;
    samples++;
    setChanged();
  }

  public void reset() {
    total = current = 0;
    samples = 0;
    setChanged();
  }

  @Override
  public void onServerMessageReceived(CompoundTag tag) {
    if (tag.getInt("type") == 1) reset();
  }

  @Override
  public void onClientMessageReceived(CompoundTag tag) {}

  @Override
  protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
    super.readProperties(tag, registries);
    total = tag.getDouble("total");
    if (!Double.isFinite(total) || total < 0) total = 0;
    samples = Math.max(0, tag.getLong("samples"));
  }

  @Override
  protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
    super.writeProperties(tag, registries);
    tag.putDouble("total", total);
    tag.putLong("samples", samples);
  }

  @Override
  public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
    return side != getFacing();
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
    return side == getFacing();
  }

  @Override
  public double getConductionLoss() {
    return 0.025;
  }

  @Override
  public double getInsulationEnergyAbsorption() {
    return 16384;
  }

  @Override
  public double getInsulationBreakdownEnergy() {
    return maximumPacket() + 1;
  }

  @Override
  public double getConductorBreakdownEnergy() {
    return maximumPacket() + 1;
  }

  @Override
  public void removeInsulation() {}

  @Override
  public void removeConductor() {
    level.destroyBlock(worldPosition, false);
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new ContainerEnergyCounter(id, inventory, this);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable(getBlockState().getBlock().getDescriptionId());
  }
}
