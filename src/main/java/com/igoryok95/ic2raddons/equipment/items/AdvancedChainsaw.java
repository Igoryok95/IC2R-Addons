package com.igoryok95.ic2raddons.equipment.items;

import com.igoryok95.ic2raddons.equipment.GraviConfig;
import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.Mining;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.tool.ItemElectricToolChainsaw;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

  public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
    if (!player.isCreative() && !ElectricItem.manager.canUse(stack, 100)) return true;
    if (player instanceof ServerPlayer server
        && ItemState.enabled(stack, "tree")
        && !player.isShiftKeyDown()
        && !Mining.isWorking(player)
        && player.level().getBlockState(pos).is(BlockTags.LOGS))
      Mining.breakTargets(
          server,
          stack,
          pos,
          Mining.connected(server, pos, true, GraviConfig.TREE_LIMIT.get()),
          100,
          state -> state.is(BlockTags.LOGS));
    return false;
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (player.isShiftKeyDown() && IC2.keyboard.isModeSwitchKeyDown(player)) {
      if (!level.isClientSide)
        player.displayClientMessage(
            Component.translatable(
                "tooltip.ic2r_addons.tree",
                Component.translatable(
                    ItemState.toggle(player.getItemInHand(hand), "tree")
                        ? "ic2r_addons.on"
                        : "ic2r_addons.off")),
            true);
      return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    return super.use(level, player, hand);
  }
}
