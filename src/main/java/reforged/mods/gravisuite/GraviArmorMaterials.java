package reforged.mods.gravisuite;

import ic2.core.ref.Ic2ArmorMaterials;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

/** Retains IC2R protection properties while selecting the upgraded armor's own artwork. */
public final class GraviArmorMaterials {
  public static final Holder<ArmorMaterial> NANO =
      upgraded(Ic2ArmorMaterials.NANO_SUIT.holder(), "advanced_nano");
  public static final Holder<ArmorMaterial> QUANTUM =
      upgraded(Ic2ArmorMaterials.QUANTUM_SUIT.holder(), "advanced_quantum");

  private static Holder<ArmorMaterial> upgraded(Holder<ArmorMaterial> original, String texture) {
    ArmorMaterial base = original.value();
    return Holder.direct(
        new ArmorMaterial(
            base.defense(),
            base.enchantmentValue(),
            base.equipSound(),
            base.repairIngredient(),
            List.of(new ArmorMaterial.Layer(GraviSuite.id(texture))),
            base.toughness(),
            base.knockbackResistance()));
  }

  private GraviArmorMaterials() {}
}
