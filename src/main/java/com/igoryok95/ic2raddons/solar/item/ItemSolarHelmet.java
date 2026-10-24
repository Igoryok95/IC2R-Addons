package com.igoryok95.ic2raddons.solar.item;

import ic2.api.item.*;
import ic2.core.item.armor.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public final class ItemSolarHelmet {
  private ItemSolarHelmet() {}

  private static Holder<ArmorMaterial> material(String name) {
    var id = ResourceLocation.fromNamespaceAndPath("ic2r_addons", name + "_solar_helmet");
    List<ArmorMaterial.Layer> layers = new ArrayList<>();
    layers.add(new ArmorMaterial.Layer(id));
    if (!name.equals("advanced")) layers.add(new ArmorMaterial.Layer(id, "_overlay", false));
    return Holder.direct(
        new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 0),
            0,
            SoundEvents.ARMOR_EQUIP_IRON,
            Ingredient::of,
            layers,
            2,
            0));
  }

  public static int generation(Level world, net.minecraft.core.BlockPos pos, int day, int night) {
    if (!world.dimensionType().hasSkyLight()
        || !world.canSeeSky(pos.above())
        || world.getBlockState(pos.above()).getLightBlock(world, pos.above()) > 0) return 0;
    boolean rain =
        world.isRainingAt(pos.above())
            || world.isThundering() && world.getBiome(pos).value().hasPrecipitation();
    return (int)
        ((world.isDay() && !rain ? day : night)
            * com.igoryok95.ic2raddons.solar.AspConfig.GENERATION.get());
  }

  public static double generateAndCharge(Player player, ItemStack helmet, int day, int night) {
    if (player.level().isClientSide || player.getItemBySlot(EquipmentSlot.HEAD) != helmet) return 0;
    double budget = generation(player.level(), player.blockPosition(), day, night),
        produced = budget;
    IElectricItem electric = (IElectricItem) helmet.getItem();
    for (var slot : List.of(EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
      budget -= charge(player.getItemBySlot(slot), budget, electric.getTier(helmet));
    if (!com.igoryok95.ic2raddons.solar.AspConfig.ARMOR_ONLY.get()) {
      budget -= charge(player.getOffhandItem(), budget, electric.getTier(helmet));
      for (ItemStack stack : player.getInventory().items)
        if (stack != helmet && budget > 0)
          budget -= charge(stack, budget, electric.getTier(helmet));
    }
    budget -= ElectricItem.manager.charge(helmet, budget, Integer.MAX_VALUE, true, false);
    return produced - budget;
  }

  private static double charge(ItemStack stack, double amount, int tier) {
    return amount > 0 && stack.getItem() instanceof IElectricItem
        ? ElectricItem.manager.charge(stack, amount, tier, false, false)
        : 0;
  }

  private static void tooltip(List<Component> list, int day, int night) {
    list.add(Component.translatable("ic2r_addons.tooltip.generation.day", day));
    list.add(Component.translatable("ic2r_addons.tooltip.generation.night", night));
  }

  public static final class Nano extends ItemArmorNanoSuit {
    public Nano() {
      super(
          material("advanced"),
          EquipmentSlot.HEAD,
          new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    public double getMaxCharge(ItemStack stack) {
      return 8_000_000;
    }

    public double getTransferLimit(ItemStack stack) {
      return 5_000;
    }

    public void inventoryTick(
        ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, level, entity, slot, selected);
      if (entity instanceof Player player) generateAndCharge(player, stack, 8, 1);
    }

    public void appendHoverText(
        ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(stack, context, list, flag);
      tooltip(list, 8, 1);
    }
  }

  public static final class Quantum extends ItemArmorQuantumSuit {
    private final boolean ultimate;

    public Quantum(boolean ultimate) {
      super(
          material(ultimate ? "ultimate" : "hybrid"),
          EquipmentSlot.HEAD,
          new Item.Properties().stacksTo(1));
      this.ultimate = ultimate;
    }

    public double getMaxCharge(ItemStack stack) {
      return ultimate ? 100_000_000 : 40_000_000;
    }

    public double getTransferLimit(ItemStack stack) {
      return ultimate ? 50_000 : 12_000;
    }

    public void inventoryTick(
        ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, level, entity, slot, selected);
      if (entity instanceof Player player)
        generateAndCharge(player, stack, ultimate ? 512 : 64, ultimate ? 64 : 8);
    }

    public void appendHoverText(
        ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(stack, context, list, flag);
      tooltip(list, ultimate ? 512 : 64, ultimate ? 64 : 8);
    }
  }
}
