package reforged.mods.gravisuite;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ItemState {
    private ItemState() {}
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(GraviSuite.ID);
    }
    public static void update(ItemStack stack, Consumer<CompoundTag> operation) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag own = root.getCompound(GraviSuite.ID);
            operation.accept(own);
            root.put(GraviSuite.ID, own);
        });
    }
    public static int mode(ItemStack stack, String key, int count) { return Math.floorMod(read(stack).getInt(key), count); }
    public static int cycle(ItemStack stack, String key, int count) {
        int next = (mode(stack, key, count) + 1) % count;
        update(stack, tag -> tag.putInt(key, next));
        return next;
    }
    public static boolean enabled(ItemStack stack, String key) { return read(stack).getBoolean(key); }
    public static boolean toggle(ItemStack stack, String key) {
        boolean next = !enabled(stack, key);
        update(stack, tag -> tag.putBoolean(key, next));
        return next;
    }
}
