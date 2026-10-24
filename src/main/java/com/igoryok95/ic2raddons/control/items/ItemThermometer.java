// SPDX-License-Identifier: GPL-3.0-only
package com.igoryok95.ic2raddons.control.items;

import com.igoryok95.ic2raddons.control.crossmod.CrossIC2R;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class ItemThermometer extends Item {
  public ItemThermometer(Properties properties) {
    super(properties.stacksTo(1));
  }

  protected boolean consume(ItemStack stack, net.minecraft.world.entity.player.Player player) {
    return true;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;
    var reactor = CrossIC2R.reactor(context.getLevel().getBlockEntity(context.getClickedPos()));
    if (reactor == null) return InteractionResult.PASS;
    if (!context.getLevel().isClientSide && consume(context.getItemInHand(), player))
      player.displayClientMessage(
          Component.translatable(
              "message.ic2r_addons.control.thermometer.heat",
              reactor.getHeat(),
              reactor.getMaxHeat()),
          false);
    return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
  }

  public static final class Digital extends ItemThermometer implements IElectricItem {
    public Digital(Properties properties) {
      super(properties);
    }

    @Override
    protected boolean consume(ItemStack stack, net.minecraft.world.entity.player.Player player) {
      if (ElectricItem.manager.use(stack, 50, player)) return true;
      player.displayClientMessage(
          Component.translatable("message.ic2r_addons.control.thermometer.no_power"), true);
      return false;
    }

    @Override
    public boolean canProvideEnergy(ItemStack stack) {
      return false;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
      return 12000;
    }

    @Override
    public int getTier(ItemStack stack) {
      return 1;
    }

    @Override
    public double getTransferLimit(ItemStack stack) {
      return 250;
    }
  }
}
