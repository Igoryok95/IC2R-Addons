package reforged.mods.gravisuite;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.IC2;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import reforged.mods.gravisuite.items.GraviArmor;
import reforged.mods.gravisuite.items.UtilityItem;

public final class GraviEvents {
    private static final net.minecraft.resources.ResourceLocation FLIGHT = GraviSuite.id("gravi_flight");
    private GraviEvents() {}
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean flight = chest.getItem() instanceof GraviArmor armor && armor.kind == GraviArmor.Kind.GRAVI
                && ItemState.enabled(chest, "engine") && ElectricItem.manager.canUse(chest, 278);
        var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute != null) {
            if (flight && !attribute.hasModifier(FLIGHT)) attribute.addTransientModifier(new AttributeModifier(FLIGHT, 1, AttributeModifier.Operation.ADD_VALUE));
            if (!flight && attribute.hasModifier(FLIGHT)) attribute.removeModifier(FLIGHT);
        }
        if (flight) {
            int cost = player.getAbilities().flying || !player.onGround() ? 278 : 1;
            if (IC2.keyboard.isBoostKeyDown(player) && player.getAbilities().flying) {
                cost *= 3;
                if (ElectricItem.manager.canUse(chest, cost)) player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().scale(0.12)));
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
            if (armor.kind == GraviArmor.Kind.GRAVI && player.isOnFire() && ElectricItem.manager.use(chest, 1000, null)) player.clearFire();
            for (ItemStack held : List.of(player.getMainHandItem(), player.getOffhandItem())) {
                if (held.getItem() instanceof IElectricItem && held != chest) {
                    double available = ElectricItem.manager.discharge(chest, armor.getTransferLimit(chest), Integer.MAX_VALUE, false, true, true);
                    double accepted = ElectricItem.manager.charge(held, available, armor.getTier(chest), false, false);
                    if (accepted > 0) ElectricItem.manager.discharge(chest, accepted, Integer.MAX_VALUE, true, true, false);
                }
            }
        }
    }
    public static void fall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GraviArmor armor && armor.kind == GraviArmor.Kind.GRAVI
                && ElectricItem.manager.use(chest, Math.max(0, Math.ceil(event.getDistance() - 3)) * 900, null)) event.setDamageMultiplier(0);
    }
}
