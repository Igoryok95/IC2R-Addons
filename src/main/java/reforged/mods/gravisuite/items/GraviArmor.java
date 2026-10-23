package reforged.mods.gravisuite.items;

import ic2.api.item.ElectricItem;
import ic2.api.item.IHazmatLike;
import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.jetpack.IBoostingJetpack;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import reforged.mods.gravisuite.GraviSuite;
import reforged.mods.gravisuite.ItemState;

public class GraviArmor extends ItemArmorElectric implements IBoostingJetpack, IHazmatLike {
    public enum Kind {
        ADVANCED_LAPPACK("advanced_lappack", 40_000_000, 12_000, 4, 0),
        ULTIMATE_LAPPACK("ultimate_lappack", 100_000_000, 50_000, 4, 0),
        JETPACK("advanced_jetpack", 4_000_000, 5_000, 3, 0),
        NANO("advanced_nano", 8_000_000, 5_000, 3, 0.9),
        GRAVI("advanced_quant", 100_000_000, 50_000, 4, 1.2);
        public final String texture;
        public final int capacity, transfer, tier;
        public final double absorption;
        Kind(String texture, int capacity, int transfer, int tier, double absorption) {
            this.texture = texture; this.capacity = capacity; this.transfer = transfer; this.tier = tier; this.absorption = absorption;
        }
    }
    public final Kind kind;
    public GraviArmor(Kind kind) {
        super(material(kind), EquipmentSlot.CHEST, new Properties().stacksTo(1).rarity(Rarity.EPIC), kind.capacity, kind.transfer, kind.tier);
        this.kind = kind;
    }
    private static Holder<ArmorMaterial> material(Kind kind) {
        return Holder.direct(new ArmorMaterial(Map.of(ArmorItem.Type.CHESTPLATE, 0), 0, SoundEvents.ARMOR_EQUIP_IRON,
                Ingredient::of, List.of(new ArmorMaterial.Layer(GraviSuite.id(kind.texture))), 2, 0));
    }
    public boolean canProvideEnergy(ItemStack stack) { return true; }
    public int getEnergyPerDamage() { return kind == Kind.GRAVI ? 20_000 : kind == Kind.NANO ? 5_000 : 0; }
    public double getDamageAbsorptionRatio() { return kind.absorption; }
    public boolean addsProtection(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        return kind == Kind.GRAVI && ElectricItem.manager.getCharge(stack) > 0;
    }
    public boolean isJetpackActive(ItemStack stack) {
        return (kind == Kind.JETPACK || kind == Kind.NANO) && ItemState.enabled(stack, "engine");
    }
    public void drainEnergy(ItemStack stack, int amount) { ElectricItem.manager.discharge(stack, amount + 6, Integer.MAX_VALUE, true, false, false); }
    public float getPower(ItemStack stack) { return 1.0F; }
    public float getDropPercentage(ItemStack stack) { return 0.05F; }
    public double getChargeLevel(ItemStack stack) { return ElectricItem.manager.getCharge(stack) / getMaxCharge(stack); }
    public float getHoverMultiplier(ItemStack stack, boolean upwards) { return upwards ? 0.25F : 0.15F; }
    public float getWorldHeightDivisor(ItemStack stack) { return 1.0F; }
    public float getBaseThrust(ItemStack stack, boolean hovering) { return hovering ? 0.8F : 0.3F; }
    public float getBoostThrust(Player player, ItemStack stack, boolean hovering) {
        return IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.canUse(stack, 20) ? 0.12F : 0;
    }
    public boolean useBoostPower(ItemStack stack, float amount) { return ElectricItem.manager.use(stack, 20, null); }
    public float getHoverBoost(Player player, ItemStack stack, boolean upwards) { return IC2.keyboard.isBoostKeyDown(player) ? 2 : 1; }
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (kind == Kind.GRAVI || kind == Kind.JETPACK || kind == Kind.NANO) {
            tooltip.add(Component.translatable("tooltip.gravisuite.engine", Component.translatable(ItemState.enabled(stack, "engine") ? "gravisuite.on" : "gravisuite.off")));
            tooltip.add(Component.translatable("tooltip.gravisuite.engine_key"));
        }
    }
}
