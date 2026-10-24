package com.igoryok95.ic2raddons.equipment;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ItemState {
  private ItemState() {}

  public static CompoundTag read(ItemStack stack) {
    CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    return root.getCompound(root.contains(GraviSuite.ID) ? GraviSuite.ID : "solar_gravisuite");
  }

  public static void update(ItemStack stack, Consumer<CompoundTag> operation) {
    CustomData.update(
        DataComponents.CUSTOM_DATA,
        stack,
        root -> {
          CompoundTag own =
              root.getCompound(root.contains(GraviSuite.ID) ? GraviSuite.ID : "solar_gravisuite");
          operation.accept(own);
          root.put(GraviSuite.ID, own);
          root.remove("solar_gravisuite");
        });
  }

  public static int mode(ItemStack stack, String key, int count) {
    return Math.floorMod(read(stack).getInt(key), count);
  }

  public static int cycle(ItemStack stack, String key, int count) {
    int next = (mode(stack, key, count) + 1) % count;
    update(stack, tag -> tag.putInt(key, next));
    return next;
  }

  public static boolean enabled(ItemStack stack, String key) {
    return read(stack).getBoolean(key);
  }

  public static boolean toggle(ItemStack stack, String key) {
    boolean next = !enabled(stack, key);
    update(stack, tag -> tag.putBoolean(key, next));
    return next;
  }
}
