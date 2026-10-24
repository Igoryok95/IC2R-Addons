package com.igoryok95.ic2raddons.equipment.items;

import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.Mining;
import ic2.api.item.ElectricItem;
import ic2.api.item.IMiningDrill;
import ic2.core.IC2;
import ic2.core.item.tool.ItemElectricTool;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MiningTool extends ItemElectricTool implements IMiningDrill {
  public final boolean vajra;
  public final boolean iridium;
  private static final int[] DRILL_COST = {160, 80, 50};
  private static final int[] VAJRA_COST = {3200, 1600, 800};
  private static final int[] IRIDIUM_COST = {1000, 600, 400};
  private static final float[] DRILL_SPEED = {48, 24, 16};
  private static final float[] VAJRA_SPEED = {128, 64, 32};
  private static final float[] IRIDIUM_SPEED = {72, 36, 24};

  public MiningTool(boolean vajra) {
    this(vajra, false);
  }

  public MiningTool(boolean vajra, boolean iridium) {
    super(
        new Properties().stacksTo(1),
        vajra ? 3200 : 160,
        Tiers.NETHERITE,
        List.of(
            BlockTags.MINEABLE_WITH_PICKAXE,
            BlockTags.MINEABLE_WITH_SHOVEL,
            BlockTags.MINEABLE_WITH_AXE));
    this.vajra = vajra;
    this.iridium = iridium;
    maxCharge = vajra ? 10_000_000 : iridium ? 1_000_000 : 45_000;
    transferLimit = vajra ? 10_000 : iridium ? 5_000 : 500;
    tier = vajra || iridium ? 3 : 2;
  }

  public int cost(ItemStack stack) {
    return (vajra ? VAJRA_COST : iridium ? IRIDIUM_COST : DRILL_COST)
        [ItemState.mode(stack, "efficiency", 3)];
  }

  public boolean effective(BlockState state) {
    return vajra
        || state.is(BlockTags.MINEABLE_WITH_PICKAXE)
        || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
  }

  public float getDestroySpeed(ItemStack stack, BlockState state) {
    if (!effective(state) || !ElectricItem.manager.canUse(stack, cost(stack))) return 0.1F;
    return (vajra ? VAJRA_SPEED : iridium ? IRIDIUM_SPEED : DRILL_SPEED)
        [ItemState.mode(stack, "efficiency", 3)];
  }

  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    return effective(state) && !state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
  }

  public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
    if (!player.isCreative() && !ElectricItem.manager.canUse(stack, cost(stack))) return true;
    if (player instanceof ServerPlayer server
        && !player.isShiftKeyDown()
        && !Mining.isWorking(player)) {
      int mode = ItemState.mode(stack, "mining", vajra ? 3 : 2);
      if (mode > 0) Mining.extraBlocks(server, stack, pos, this, mode);
    }
    return false;
  }

  public boolean mineBlock(
      ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity user) {
    if (!level.isClientSide
        && state.getDestroySpeed(level, pos) > 0
        && (!(user instanceof Player player) || !player.isCreative()))
      ElectricItem.manager.use(stack, cost(stack), user);
    return true;
  }

  private boolean switchMode(ItemStack stack, Player player) {
    if (IC2.keyboard.isAltKeyDown(player)) {
      if (!player.level().isClientSide) ItemState.cycle(stack, "efficiency", 3);
      return true;
    }
    if (!IC2.keyboard.isModeSwitchKeyDown(player)) return false;
    if (!player.level().isClientSide) {
      if ((vajra || iridium) && player.isShiftKeyDown()) {
        int mode = ItemState.cycle(stack, "enchantment", 2);
        configureEnchantments(stack, player.level(), mode);
      } else ItemState.cycle(stack, "mining", vajra ? 3 : 2);
    }
    return true;
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    return switchMode(stack, player)
        ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        : InteractionResultHolder.pass(stack);
  }

  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    Player player = context.getPlayer();
    return player != null && switchMode(stack, player)
        ? InteractionResult.sidedSuccess(context.getLevel().isClientSide)
        : InteractionResult.PASS;
  }

  public InteractionResult useOn(UseOnContext context) {
    return Mining.placeTorch(context);
  }

  public boolean isEnchantable(ItemStack stack) {
    return true;
  }

  public int getEnchantmentValue() {
    return 20;
  }

  public int energyUse(ItemStack stack, Level level, BlockPos pos, BlockState state) {
    return Math.max(1, cost(stack) / 4);
  }

  public int breakTime(ItemStack stack, Level level, BlockPos pos, BlockState state) {
    return vajra ? 5 : iridium ? 10 : 20;
  }

  public boolean breakBlock(ItemStack stack, Level level, BlockPos pos, BlockState state) {
    return ElectricItem.manager.use(stack, cost(stack), null);
  }

  private void configureEnchantments(ItemStack stack, Level level, int mode) {
    var enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    var silk = enchants.getOrThrow(Enchantments.SILK_TOUCH);
    var fortune = enchants.getOrThrow(Enchantments.FORTUNE);
    EnchantmentHelper.updateEnchantments(
        stack,
        mutable -> {
          mutable.removeIf(holder -> holder.equals(silk) || holder.equals(fortune));
          mutable.set(mode == 1 ? silk : fortune, mode == 1 ? 1 : 3);
        });
    ItemState.update(stack, tag -> tag.putBoolean("enchantment_initialized", true));
  }

  public void inventoryTick(
      ItemStack stack,
      Level level,
      net.minecraft.world.entity.Entity entity,
      int slot,
      boolean selected) {
    super.inventoryTick(stack, level, entity, slot, selected);
    if (!level.isClientSide
        && (vajra || iridium)
        && !ItemState.enabled(stack, "enchantment_initialized"))
      configureEnchantments(stack, level, ItemState.mode(stack, "enchantment", 2));
  }

  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    String[] modes =
        vajra ? new String[] {"normal", "vein", "extended"} : new String[] {"normal", "aoe"};
    String[] efficiencies = {"normal", "low_power", "fine"};
    tooltip.add(
        Component.translatable(
            "tooltip.ic2r_addons.mining",
            Component.translatable(
                "mode.ic2r_addons." + modes[ItemState.mode(stack, "mining", modes.length)])));
    tooltip.add(
        Component.translatable(
            "tooltip.ic2r_addons.efficiency",
            Component.translatable(
                "mode.ic2r_addons." + efficiencies[ItemState.mode(stack, "efficiency", 3)])));
    tooltip.add(Component.translatable("tooltip.ic2r_addons.mining_keys"));
    if (vajra || iridium)
      tooltip.add(Component.translatable("tooltip.ic2r_addons.enchantment_keys"));
  }
}
