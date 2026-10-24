package com.igoryok95.ic2raddons.equipment;

import com.igoryok95.ic2raddons.equipment.items.AdvancedNanoArmor;
import com.igoryok95.ic2raddons.equipment.items.AdvancedQuantumArmor;
import com.igoryok95.ic2raddons.equipment.items.GraviArmor;
import com.igoryok95.ic2raddons.equipment.items.UtilityItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.IC2;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class GraviEvents {
  private static final net.minecraft.resources.ResourceLocation FLIGHT =
      GraviSuite.id("gravi_flight");
  private static final net.minecraft.resources.ResourceLocation SPRINT =
      GraviSuite.id("nano_sprint");

  private GraviEvents() {}

  public static void tick(PlayerTickEvent.Post event) {
    Player player = event.getEntity();
    if (player.level().isClientSide) return;
    ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
    boolean sprint =
        legs.getItem() instanceof AdvancedNanoArmor
            && player.isSprinting()
            && !player.getAbilities().flying
            && ElectricItem.manager.use(legs, 8, null);
    var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
    if (movement != null) {
      if (sprint && !movement.hasModifier(SPRINT))
        movement.addTransientModifier(
            new AttributeModifier(SPRINT, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      if (!sprint && movement.hasModifier(SPRINT)) movement.removeModifier(SPRINT);
    }
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    boolean flight =
        chest.getItem() instanceof GraviArmor armor
            && armor.kind == GraviArmor.Kind.GRAVI
            && ItemState.enabled(chest, "engine")
            && ElectricItem.manager.canUse(chest, 278);
    var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
    if (attribute != null) {
      if (flight && !attribute.hasModifier(FLIGHT))
        attribute.addTransientModifier(
            new AttributeModifier(FLIGHT, 1, AttributeModifier.Operation.ADD_VALUE));
      if (!flight && attribute.hasModifier(FLIGHT)) attribute.removeModifier(FLIGHT);
    }
    if (flight) {
      int cost = player.getAbilities().flying || !player.onGround() ? 278 : 1;
      if (IC2.keyboard.isBoostKeyDown(player) && player.getAbilities().flying) {
        int boostedCost = cost * 3;
        if (ElectricItem.manager.canUse(chest, boostedCost)) {
          cost = boostedCost;
          player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().scale(0.12)));
        }
      }
      ElectricItem.manager.use(chest, cost, null);
      if (player.getAbilities().flying) player.resetFallDistance();
    }
    if (player.tickCount % 5 == 0) {
      for (int i = 0; i < 9; i++) {
        ItemStack item = player.getInventory().getItem(i);
        if (item.getItem() instanceof UtilityItem utility) utility.nearby(item, player);
      }
    }
    if (chest.getItem() instanceof GraviArmor armor) {
      if (armor.kind == GraviArmor.Kind.GRAVI
          && player.isOnFire()
          && ElectricItem.manager.use(chest, 1000, null)) player.clearFire();
      for (ItemStack held : List.of(player.getMainHandItem(), player.getOffhandItem())) {
        if (held.getItem() instanceof IElectricItem && held != chest) {
          double available =
              ElectricItem.manager.discharge(
                  chest, armor.getTransferLimit(chest), Integer.MAX_VALUE, false, true, true);
          double accepted =
              ElectricItem.manager.charge(held, available, armor.getTier(chest), false, false);
          if (accepted > 0)
            ElectricItem.manager.discharge(chest, accepted, Integer.MAX_VALUE, true, true, false);
        }
      }
    }
  }

  public static void fall(LivingFallEvent event) {
    if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    boolean absorbed =
        boots.getItem() instanceof AdvancedNanoArmor nano
            ? nano.absorbFall(boots, event.getDistance())
            : boots.getItem() instanceof AdvancedQuantumArmor quantum
                && quantum.absorbFall(boots, event.getDistance());
    if (absorbed) {
      event.setDamageMultiplier(0);
      return;
    }
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (chest.getItem() instanceof GraviArmor armor
        && armor.kind == GraviArmor.Kind.GRAVI
        && ElectricItem.manager.use(
            chest, Math.max(0, Math.ceil(event.getDistance() - 3)) * 900, null))
      event.setDamageMultiplier(0);
  }
}
