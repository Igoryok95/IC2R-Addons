package com.igoryok95.ic2raddons.equipment.gametest;

import com.igoryok95.ic2raddons.equipment.GraviItems;
import com.igoryok95.ic2raddons.equipment.items.GraviArmor;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import ic2.api.item.ElectricItem;
import ic2.core.gametest.ElectricArmorGameTests;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class ArmorProtectionGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final EquipmentSlot[] SLOTS = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };

  @GameTest(template = EMPTY)
  public static void upgradedAndMixedSuitsKeepAllArmorPoints(GameTestHelper helper) {
    Item[][] sets = {
      {
        AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
        GraviItems.ADVANCED_NANO.get(),
        GraviItems.ADVANCED_NANO_LEGGINGS.get(),
        GraviItems.ADVANCED_NANO_BOOTS.get()
      },
      {
        AdvSolarBlocks.HYBRID_SOLAR_HELMET.get(),
        GraviItems.GRAVI_CHESTPLATE.get(),
        GraviItems.ADVANCED_QUANTUM_LEGGINGS.get(),
        GraviItems.ADVANCED_QUANTUM_BOOTS.get()
      },
      {
        AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
        GraviItems.GRAVI_CHESTPLATE.get(),
        Ic2Items.QUANTUM_LEGGINGS,
        Ic2Items.QUANTUM_BOOTS
      },
      {
        AdvSolarBlocks.QUANTUM_SOLAR_HELMET.get(),
        GraviItems.ADVANCED_NANO.get(),
        GraviItems.ADVANCED_QUANTUM_LEGGINGS.get(),
        GraviItems.ADVANCED_NANO_BOOTS.get()
      }
    };
    for (Item[] set : sets) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      int[] expected = {3, 11, 17, 20};
      for (int i = 0; i < SLOTS.length; i++) {
        player.setItemSlot(SLOTS[i], ElectricItemManager.getCharged(set[i], 100_000));
        ElectricArmorGameTests.refreshEquipment(player);
        armor(helper, player, expected[i], "adding " + SLOTS[i]);
      }
      for (EquipmentSlot slot : SLOTS) {
        ElectricItem.manager.discharge(
            player.getItemBySlot(slot), 8, Integer.MAX_VALUE, true, false, false);
        ElectricArmorGameTests.refreshEquipment(player);
        armor(helper, player, 20, "energy update in " + slot);
      }
      var attacker = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
      float before = player.getHealth();
      helper.assertTrue(
          player.hurt(helper.getLevel().damageSources().mobAttack(attacker), 10),
          "real attack must reach health and EU absorption");
      helper.assertTrue(
          before - player.getHealth() > 0 && before - player.getHealth() < 1,
          "full upgraded protection must reduce real damage, not merely fill the HUD");
      ElectricArmorGameTests.refreshEquipment(player);
      armor(helper, player, 20, "after damage");
      player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      ElectricArmorGameTests.refreshEquipment(player);
      armor(helper, player, 9, "only leggings and boots remain");
      ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
      ElectricItem.manager.discharge(
          boots, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, false);
      ElectricArmorGameTests.refreshEquipment(player);
      armor(helper, player, 6, "only boots discharged");
      ElectricItem.manager.charge(boots, 100_000, Integer.MAX_VALUE, true, false);
      ElectricArmorGameTests.refreshEquipment(player);
      armor(helper, player, 9, "boots recharged without reequip");
    }
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void protectiveChestsRequireChargeButBackpacksStayUnarmored(GameTestHelper helper) {
    for (GraviArmor.Kind kind : GraviArmor.Kind.values()) {
      GraviArmor item =
          switch (kind) {
            case NANO -> GraviItems.ADVANCED_NANO.get();
            case GRAVI -> GraviItems.GRAVI_CHESTPLATE.get();
            case JETPACK -> GraviItems.ADVANCED_JETPACK.get();
            case ADVANCED_LAPPACK -> GraviItems.ADVANCED_LAPPACK.get();
            case ULTIMATE_LAPPACK -> GraviItems.ULTIMATE_LAPPACK.get();
          };
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ItemStack stack = ElectricItemManager.getCharged(item, 100_000);
      player.setItemSlot(EquipmentSlot.CHEST, stack);
      ElectricArmorGameTests.refreshEquipment(player);
      armor(
          helper,
          player,
          kind == GraviArmor.Kind.NANO || kind == GraviArmor.Kind.GRAVI ? 8 : 0,
          "charged " + kind);
      ElectricItem.manager.discharge(
          stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, false);
      ElectricArmorGameTests.refreshEquipment(player);
      armor(helper, player, 0, "empty " + kind);
      helper.assertTrue(
          ((ItemArmorElectric) item).getMaxCharge(stack) == kind.capacity,
          "capacity must remain unchanged");
    }
    helper.succeed();
  }

  private static void armor(GameTestHelper helper, Player player, int expected, String context) {
    helper.assertTrue(
        player.getArmorValue() == expected,
        context + ": expected " + expected + ", got " + player.getArmorValue());
  }
}
