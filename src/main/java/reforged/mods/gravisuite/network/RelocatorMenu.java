package reforged.mods.gravisuite.network;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import reforged.mods.gravisuite.GraviItems;

public final class RelocatorMenu extends AbstractContainerMenu {
    public final InteractionHand hand;
    public RelocatorMenu(int id, Inventory inventory, InteractionHand hand) {
        super(GraviNetwork.RELOCATOR_MENU.get(), id); this.hand = hand;
    }
    public boolean stillValid(Player player) { return player.getItemInHand(hand).is(GraviItems.RELOCATOR.get()); }
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
