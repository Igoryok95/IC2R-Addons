// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.tileentities.TileEntityEnergyCounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ContainerEnergyCounter extends AbstractContainerMenu {
  public final TileEntityEnergyCounter counter;
  private final ContainerData data;

  public ContainerEnergyCounter(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        (TileEntityEnergyCounter) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
  }

  public ContainerEnergyCounter(int id, Inventory inventory, TileEntityEnergyCounter counter) {
    super(ModContainerTypes.energy_counter.get(), id);
    this.counter = counter;
    data =
        inventory.player.level().isClientSide
            ? new SimpleContainerData(13)
            : new ContainerData() {
              public int getCount() {
                return 13;
              }

              public void set(int index, int value) {}

              public int get(int index) {
                if (index == 12) return counter.maximumPacket();
                double number =
                    switch (index / 4) {
                      case 0 -> counter.total();
                      case 1 -> counter.average();
                      default -> counter.current();
                    };
                return (int) (Double.doubleToRawLongBits(number) >>> (16 * (index % 4))) & 65535;
              }
            };
    addDataSlots(data);
    addSlot(
        new Slot(counter, 0, 8, 69) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return counter.canPlaceItem(0, stack);
          }

          @Override
          public int getMaxStackSize() {
            return 4;
          }
        });
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 9; col++)
        addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
    for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 172));
  }

  public double number(int value) {
    long bits = 0;
    for (int i = 0; i < 4; i++) bits |= ((long) data.get(value * 4 + i) & 65535) << (16 * i);
    return Double.longBitsToDouble(bits);
  }

  public int maximumPacket() {
    return data.get(12);
  }

  @Override
  public boolean stillValid(Player player) {
    return counter != null && counter.stillValid(player);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    var slot = slots.get(index);
    if (!slot.hasItem()) return ItemStack.EMPTY;
    var stack = slot.getItem();
    var original = stack.copy();
    if (index == 0
        ? !moveItemStackTo(stack, 1, slots.size(), true)
        : !moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
    if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
    else slot.setChanged();
    slot.onTake(player, stack);
    return original;
  }
}
