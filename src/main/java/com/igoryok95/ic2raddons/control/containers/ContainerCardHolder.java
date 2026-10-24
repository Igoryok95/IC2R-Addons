// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.containers;

import com.igoryok95.ic2raddons.control.init.ModContainerTypes;
import com.igoryok95.ic2raddons.control.items.InventoryCardHolder;
import com.igoryok95.ic2raddons.control.lib.containers.ContainerBase;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotFilter;
import com.igoryok95.ic2raddons.control.lib.containers.slots.SlotLocked;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class ContainerCardHolder extends ContainerBase<InventoryCardHolder> {
  // inventory slot of the open card holder; its cards are saved into that stack, so it is locked
  private final int holderSlot;

  // client side: the server sends which hand holds the card holder
  public ContainerCardHolder(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
    this(
        windowId,
        inventory,
        data.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
  }

  public ContainerCardHolder(int windowId, Inventory inventory, InteractionHand hand) {
    super(
        new InventoryCardHolder(inventory.player.getItemInHand(hand), hand),
        ModContainerTypes.card_holder.get(),
        windowId);
    this.holderSlot =
        hand == InteractionHand.OFF_HAND
            ? ContainerPortablePanel.OFF_HAND_SLOT
            : inventory.selected;
    for (int i = 0; i < 6; i++)
      for (int j = 0; j < 9; j++) addSlot(new SlotFilter(te, j + i * 9, 8 + j * 18, 18 + i * 18));

    addPlayerInventorySlots(inventory, 167 + 18 * 3);
  }

  @Override
  protected void addPlayerInventoryTopSlots(Inventory inventory, int width, int height) {
    for (int col = 0; col < 9; col++)
      if (col == holderSlot) addSlot(new SlotLocked(inventory, col, width + col * 18, height - 24));
      else addSlot(new Slot(inventory, col, width + col * 18, height - 24));
  }

  @Override
  public void clicked(int slotIndex, int button, ClickType action, Player player) {
    // number keys (or F for the off hand) would swap the card holder out of its slot
    if (action == ClickType.SWAP && button == holderSlot) return;
    super.clicked(slotIndex, button, action, player);
  }
}
