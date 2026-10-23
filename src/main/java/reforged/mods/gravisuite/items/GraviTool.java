package reforged.mods.gravisuite.items;

import ic2.core.IC2;
import ic2.core.item.tool.ItemToolWrenchElectric;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import reforged.mods.gravisuite.ItemState;

public final class GraviTool extends ItemToolWrenchElectric {
  public GraviTool() {
    super(new Properties().stacksTo(1));
    maxCharge = 100_000;
    transferLimit = 5_000;
    tier = 2;
  }

  public int mode(ItemStack stack) {
    return ItemState.mode(stack, "tool", 4);
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (IC2.keyboard.isModeSwitchKeyDown(player)) {
      if (!level.isClientSide) ItemState.cycle(player.getItemInHand(hand), "tool", 4);
      return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    return super.use(level, player, hand);
  }

  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    Player player = context.getPlayer();
    if (player != null && IC2.keyboard.isModeSwitchKeyDown(player)) {
      use(context.getLevel(), player, context.getHand());
      return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    return mode(stack) == 0 ? super.onItemUseFirst(stack, context) : InteractionResult.PASS;
  }

  public InteractionResult useOn(UseOnContext context) {
    return switch (mode(context.getItemInHand())) {
      case 1 -> Ic2Items.ELECTRIC_TREETAP.useOn(context);
      case 2 -> Ic2Items.ELECTRIC_HOE.useOn(context);
      case 3 -> {
        BlockState original = context.getLevel().getBlockState(context.getClickedPos());
        BlockState changed =
            original.hasProperty(BlockStateProperties.FACING)
                ? original.cycle(BlockStateProperties.FACING)
                : original.hasProperty(BlockStateProperties.AXIS)
                    ? original.cycle(BlockStateProperties.AXIS)
                    : original.rotate(Rotation.CLOCKWISE_90);
        if (changed == original
            || !ic2.api.item.ElectricItem.manager.canUse(context.getItemInHand(), 150))
          yield InteractionResult.PASS;
        if (!context.getLevel().isClientSide) {
          context.getLevel().setBlockAndUpdate(context.getClickedPos(), changed);
          ic2.api.item.ElectricItem.manager.use(context.getItemInHand(), 150, context.getPlayer());
        }
        yield InteractionResult.sidedSuccess(context.getLevel().isClientSide);
      }
      default -> super.useOn(context);
    };
  }

  public boolean beforeBlockBreak(
      Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
    return mode(player.getMainHandItem()) != 0
        || super.beforeBlockBreak(level, player, pos, state, blockEntity);
  }

  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(
        Component.translatable(
            "tooltip.solar_gravisuite.tool",
            Component.translatable(
                "mode.solar_gravisuite."
                    + new String[] {"wrench", "treetap", "hoe", "screwdriver"}[mode(stack)])));
    tooltip.add(Component.translatable("tooltip.solar_gravisuite.mode_key"));
  }
}
