package reforged.mods.gravisuite.items;

import ic2.api.item.ElectricItem;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import reforged.mods.gravisuite.GraviConfig;
import reforged.mods.gravisuite.ItemState;

public final class UtilityItem extends ElectricUtility {
    public final boolean voider;
    public UtilityItem(boolean voider) { super(10_000, 500, 1); this.voider = voider; }
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (voider && player.isShiftKeyDown()) purge(stack, player);
            else ItemState.toggle(stack, "enabled");
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    public InteractionResult useOn(UseOnContext context) {
        if (!voider) return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        if (!context.getLevel().isClientSide) {
            var block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
            var item = block.asItem();
            if (item == net.minecraft.world.item.Items.AIR || !ElectricItem.manager.canUse(context.getItemInHand(), 500)) return InteractionResult.FAIL;
            ItemState.update(context.getItemInHand(), tag -> { tag.putString("filter", BuiltInRegistries.ITEM.getKey(item).toString()); tag.putBoolean("enabled", true); });
            ElectricItem.manager.use(context.getItemInHand(), 500, context.getPlayer());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    public boolean matches(ItemStack tool, ItemStack candidate) {
        String filter = ItemState.read(tool).getString("filter");
        return !candidate.isEmpty() && !filter.isEmpty() && BuiltInRegistries.ITEM.getKey(candidate.getItem()).toString().equals(filter);
    }
    public void purge(ItemStack tool, Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack != tool && matches(tool, stack)) {
                int count = Math.min(stack.getCount(), (int) (ElectricItem.manager.getCharge(tool) / 5));
                if (count > 0 && ElectricItem.manager.use(tool, count * 5.0, player)) stack.shrink(count);
            }
        }
    }
    public void nearby(ItemStack tool, Player player) {
        if (!ItemState.enabled(tool, "enabled")) return;
        int range = voider ? 2 : GraviConfig.MAGNET_RANGE.get();
        int visited = 0;
        for (ItemEntity entity : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range))) {
            if (++visited > 200) break;
            if (entity.hasPickUpDelay() || entity.getTarget() != null && !entity.getTarget().equals(player.getUUID())) continue;
            ItemStack stack = entity.getItem();
            if (voider) {
                if (!matches(tool, stack)) continue;
                int amount = Math.min(stack.getCount(), (int) (ElectricItem.manager.getCharge(tool) / 8));
                if (amount > 0 && ElectricItem.manager.use(tool, amount * 8.0, player)) {
                    stack.shrink(amount);
                    if (stack.isEmpty()) entity.discard();
                }
            } else if (player.getInventory().getFreeSlot() < 0 && !canMerge(player, stack)) {
                continue;
            } else if (ElectricItem.manager.use(tool, 1, player)) {
                var offset = player.position().add(0, 0.75, 0).subtract(entity.position());
                if (offset.lengthSqr() < 1) entity.playerTouch(player);
                else { entity.setDeltaMovement(offset.normalize().scale(0.35)); entity.hurtMarked = true; }
            }
        }
    }
    private boolean canMerge(Player player, ItemStack stack) {
        for (ItemStack own : player.getInventory().items)
            if (ItemStack.isSameItemSameComponents(own, stack) && own.getCount() < own.getMaxStackSize()) return true;
        return false;
    }
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.gravisuite.enabled", Component.translatable(ItemState.enabled(stack, "enabled") ? "gravisuite.on" : "gravisuite.off")));
        if (voider) {
            ResourceLocation filter = ResourceLocation.tryParse(ItemState.read(stack).getString("filter"));
            Component name = filter != null && BuiltInRegistries.ITEM.containsKey(filter) ? BuiltInRegistries.ITEM.get(filter).getDescription() : Component.translatable("gravisuite.none");
            tooltip.add(Component.translatable("tooltip.gravisuite.filter", name));
            tooltip.add(Component.translatable("tooltip.gravisuite.voider_keys"));
        } else tooltip.add(Component.translatable("tooltip.gravisuite.magnet_keys"));
    }
}
