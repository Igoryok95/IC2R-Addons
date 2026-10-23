package reforged.mods.gravisuite;

import ic2.api.item.ElectricItem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import reforged.mods.gravisuite.items.MiningTool;

public final class Mining {
  private static final Set<UUID> WORKING = new HashSet<>();
  private static final TagKey<net.minecraft.world.level.block.Block> ORES =
      TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));

  private Mining() {}

  public static boolean isWorking(Player player) {
    return WORKING.contains(player.getUUID());
  }

  public static List<BlockPos> plane(BlockPos center, Direction face) {
    List<BlockPos> result = new ArrayList<>();
    for (int a = -1; a <= 1; a++)
      for (int b = -1; b <= 1; b++) {
        if (a == 0 && b == 0) continue;
        result.add(
            switch (face.getAxis()) {
              case X -> center.offset(0, a, b);
              case Y -> center.offset(a, 0, b);
              case Z -> center.offset(a, b, 0);
            });
      }
    return result;
  }

  public static void extraBlocks(
      ServerPlayer player, ItemStack tool, BlockPos center, MiningTool item, int mode) {
    var level = player.serverLevel();
    BlockState original = level.getBlockState(center);
    List<BlockPos> targets;
    if (item.vajra) {
      if (mode == 1 && !original.is(ORES)) return;
      targets = connected(player, center, false, GraviConfig.VEIN_LIMIT.get());
    } else {
      var hit =
          level.clip(
              new ClipContext(
                  player.getEyePosition(),
                  player
                      .getEyePosition()
                      .add(player.getLookAngle().scale(player.blockInteractionRange())),
                  ClipContext.Block.OUTLINE,
                  ClipContext.Fluid.NONE,
                  player));
      targets = plane(center, hit.getDirection());
    }
    breakTargets(player, tool, center, targets, item.cost(tool), state -> item.effective(state));
  }

  public static List<BlockPos> connected(
      ServerPlayer player, BlockPos center, boolean tree, int limit) {
    var level = player.serverLevel();
    BlockState origin = level.getBlockState(center);
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    Set<BlockPos> visited = new HashSet<>();
    List<BlockPos> result = new ArrayList<>();
    queue.add(center);
    visited.add(center);
    while (!queue.isEmpty() && result.size() < limit) {
      BlockPos current = queue.remove();
      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (Math.abs(next.getX() - center.getX()) > 16
            || Math.abs(next.getZ() - center.getZ()) > 16
            || Math.abs(next.getY() - center.getY()) > 32
            || !level.hasChunkAt(next)
            || !visited.add(next)) continue;
        BlockState state = level.getBlockState(next);
        if (tree ? !state.is(BlockTags.LOGS) : !state.is(origin.getBlock())) continue;
        result.add(next);
        queue.add(next);
        if (result.size() >= limit) break;
      }
    }
    return result;
  }

  public static void breakTargets(
      ServerPlayer player,
      ItemStack tool,
      BlockPos center,
      List<BlockPos> targets,
      int cost,
      java.util.function.Predicate<BlockState> effective) {
    if (!WORKING.add(player.getUUID())) return;
    try {
      float reference =
          player.level().getBlockState(center).getDestroySpeed(player.level(), center);
      for (BlockPos pos : targets) {
        if (player.getMainHandItem() != tool || !player.level().hasChunkAt(pos)) break;
        BlockState state = player.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (state.isAir()
            || state.hasBlockEntity()
            || hardness < 0
            || hardness > Math.max(reference, 0.1F) * 8
            || !effective.test(state)
            || !player.level().getWorldBorder().isWithinBounds(pos)) continue;
        if (!player.isCreative() && !ElectricItem.manager.canUse(tool, cost * 2.0)) break;
        player.gameMode.destroyBlock(pos);
      }
    } finally {
      WORKING.remove(player.getUUID());
    }
  }

  public static InteractionResult placeTorch(UseOnContext context) {
    Player player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (stack.is(Items.TORCH)) {
        BlockHitResult hit =
            new BlockHitResult(
                context.getClickLocation(),
                context.getClickedFace(),
                context.getClickedPos(),
                context.isInside());
        return ((BlockItem) Items.TORCH)
            .place(
                new BlockPlaceContext(context.getLevel(), player, context.getHand(), stack, hit));
      }
    }
    return InteractionResult.PASS;
  }
}
