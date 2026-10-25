package com.igoryok95.ic2raddons.bridge.bc;

import buildcraft.api.v2.*;
import buildcraft.api.v2.block.*;
import buildcraft.api.v2.tool.WrenchAdapter;
import com.igoryok95.ic2raddons.bridge.BridgeConfig;
import com.igoryok95.ic2raddons.equipment.items.GraviTool;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.api.item.ElectricItem;
import ic2.api.tile.IWrenchAble;
import ic2.core.item.tool.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class WrenchBridge {
  public static boolean icWrench(ItemStack s) {
    if (s.getItem() instanceof GraviTool tool && tool.mode(s) != 0) return false;
    return s.getItem() instanceof ItemToolWrench || s.getItem() instanceof ItemToolWrenchElectric;
  }

  public static boolean bcWrench(ItemStack s) {
    return BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals("buildcraftcore:wrench");
  }

  private static boolean bcBlock(net.minecraft.world.level.block.state.BlockState s) {
    return BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace().startsWith("buildcraft");
  }

  public static void register() {
    NeoForge.EVENT_BUS.addListener(
        net.neoforged.bus.api.EventPriority.LOWEST, WrenchBridge::rightClick);
    NeoForge.EVENT_BUS.addListener(WrenchBridge::breakSpeed);
    NeoForge.EVENT_BUS.addListener(WrenchBridge::harvest);
    NeoForge.EVENT_BUS.addListener(
        (ItemTooltipEvent e) -> {
          if (BridgeConfig.ENABLED.get()
              && (icWrench(e.getItemStack()) || bcWrench(e.getItemStack())))
            e.getToolTip()
                .add(
                    net.minecraft.network.chat.Component.translatable(
                        "tooltip.ic2r_addons.wrench_bridge"));
        });
    NeoForge.EVENT_BUS.addListener(
        net.neoforged.bus.api.EventPriority.LOWEST, WrenchBridge::breakBlock);
  }

  public static void registerAdapter() {
    var r = BuildCraftApi.registry(BuildCraftRegistries.WRENCH_ADAPTERS);
    r.register(
        IC2RAddons.id("ic2_wrenches"),
        new WrenchAdapter() {
          public boolean recognizes(ItemStack s) {
            return BridgeConfig.ENABLED.get()
                && icWrench(s)
                && (!(s.getItem() instanceof ItemToolWrenchElectric)
                    || ElectricItem.manager.canUse(s, 100));
          }

          public void onUsed(buildcraft.api.v2.tool.WrenchUseContext context) {
            if (context.stack().getItem() instanceof ItemToolWrenchElectric)
              ElectricItem.manager.use(context.stack(), 100, context.player());
          }
        });
  }

  public static void rightClick(PlayerInteractEvent.RightClickBlock e) {
    if (!BridgeConfig.ENABLED.get() || e.isCanceled()) return;
    var player = e.getEntity();
    var level = e.getLevel();
    var state = level.getBlockState(e.getPos());
    boolean icToBc = icWrench(e.getItemStack()) && bcBlock(state);
    boolean bcToIc = bcWrench(e.getItemStack()) && state.getBlock() instanceof IWrenchAble;
    if (!icToBc && !bcToIc) return;
    if (icToBc && ic2.core.IC2.keyboard.isModeSwitchKeyDown(player)) return;
    if (!player.mayBuild() || !level.mayInteract(player, e.getPos())) return;
    if (icToBc
        && e.getItemStack().getItem() instanceof ItemToolWrenchElectric
        && !ElectricItem.manager.canUse(e.getItemStack(), 100)) {
      e.setCanceled(true);
      e.setCancellationResult(InteractionResult.FAIL);
      return;
    }
    if (level.isClientSide) {
      e.setCanceled(true);
      e.setCancellationResult(InteractionResult.SUCCESS);
      return;
    }
    InteractionResult result;
    if (bcToIc)
      result =
          ItemToolWrench.trySetFacingFromHit(
              new UseOnContext(player, e.getHand(), e.getHitVec()), player);
    else {
      // Pipes handle wrench clicks on the block, before the wrench item's rotation fallback.
      // Let BC invoke its adapter (and charge an electric wrench) exactly once for this path.
      var blockResult =
          state.useItemOn(e.getItemStack(), level, player, e.getHand(), e.getHitVec());
      if (blockResult.consumesAction()
          || blockResult == net.minecraft.world.ItemInteractionResult.FAIL) {
        e.setCanceled(true);
        e.setCancellationResult(blockResult.result());
        return;
      }
      result =
          BuiltInRegistries.ITEM
              .get(net.minecraft.resources.ResourceLocation.parse("buildcraftcore:wrench"))
              .useOn(new UseOnContext(player, e.getHand(), e.getHitVec()));
      if (result == InteractionResult.SUCCESS
          && e.getItemStack().getItem() instanceof ItemToolWrenchElectric)
        ElectricItem.manager.use(e.getItemStack(), 100, player);
    }
    if (result != InteractionResult.PASS) {
      e.setCanceled(true);
      e.setCancellationResult(result);
    }
  }

  public static void breakSpeed(PlayerEvent.BreakSpeed e) {
    if (!BridgeConfig.ENABLED.get()) return;
    var s = e.getEntity().getMainHandItem();
    if (bcWrench(s) && e.getState().getBlock() instanceof IWrenchAble
        || icWrench(s) && bcBlock(e.getState())) e.setNewSpeed(6);
  }

  public static void harvest(PlayerEvent.HarvestCheck e) {
    if (!BridgeConfig.ENABLED.get()) return;
    var s = e.getEntity().getMainHandItem();
    if (bcWrench(s) && e.getTargetBlock().getBlock() instanceof IWrenchAble
        || icWrench(s) && bcBlock(e.getTargetBlock())) e.setCanHarvest(true);
  }

  public static void breakBlock(BlockEvent.BreakEvent e) {
    if (e.isCanceled() || !BridgeConfig.ENABLED.get() || e.getPlayer().isCreative()) return;
    var player = e.getPlayer();
    var tool = player.getMainHandItem();
    if (bcWrench(tool) && e.getState().getBlock() instanceof IWrenchAble) {
      ItemToolWrench.tryRemoveWithWrench(player.level(), player, e.getPos(), e.getState());
      e.setCanceled(true);
    } else if (icWrench(tool)
        && bcBlock(e.getState())
        && tool.getItem() instanceof ItemToolWrenchElectric) {
      if (!ElectricItem.manager.canUse(tool, 100)) e.setCanceled(true);
      else ElectricItem.manager.use(tool, 100, player);
    }
  }

  private WrenchBridge() {}
}
