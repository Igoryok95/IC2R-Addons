package com.igoryok95.ic2raddons.equipment.items;

import com.igoryok95.ic2raddons.equipment.GraviConfig;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.Mining;
import com.igoryok95.ic2raddons.equipment.ToolMessages;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.tool.ItemElectricToolChainsaw;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedChainsaw extends ItemElectricToolChainsaw {
  public AdvancedChainsaw() {
    super(new Properties().stacksTo(1));
    maxCharge = 45_000;
    transferLimit = 500;
    tier = 2;
  }

  public float getDestroySpeed(ItemStack stack, BlockState state) {
    return super.getDestroySpeed(stack, state)
        * (state.is(BlockTags.MINEABLE_WITH_AXE) && ElectricItem.manager.canUse(stack, 100)
            ? 2
            : 1);
  }

  @Override
  public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
    return player.isCreative() || ElectricItem.manager.canUse(player.getMainHandItem(), 100);
  }

  @Override
  public boolean mineBlock(
      ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity user) {
    if (!level.isClientSide
        && user instanceof ServerPlayer server
        && ItemState.enabled(stack, "tree")
        && !server.isShiftKeyDown()
        && !Mining.isWorking(server)
        && state.is(BlockTags.LOGS))
      Mining.breakTargets(
          server,
          stack,
          pos,
          Mining.connected(server, pos, true, GraviConfig.TREE_LIMIT.get()),
          100,
          candidate -> candidate.is(BlockTags.LOGS));
    return super.mineBlock(stack, level, state, pos, user);
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (player.isShiftKeyDown() && IC2.keyboard.isModeSwitchKeyDown(player)) {
      if (!level.isClientSide)
        ToolMessages.toggle(
            player,
            "tooltip.ic2r_addons.tree",
            ItemState.toggle(player.getItemInHand(hand), "tree"));
      return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    return super.use(level, player, hand);
  }
}
