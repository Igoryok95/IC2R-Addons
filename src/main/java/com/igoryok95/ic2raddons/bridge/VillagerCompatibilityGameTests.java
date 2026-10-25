package com.igoryok95.ic2raddons.bridge;

import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.equipment.GraviItems;
import ic2.api.item.IElectricItem;
import ic2.core.experimental.RestoredVillagers;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class VillagerCompatibilityGameTests {
  @GameTest(template = "gametest/empty3x3x3")
  public static void professionsOfferAddonGoodsWithoutChangingLappackBalance(GameTestHelper h) {
    var villager = EntityType.VILLAGER.create(h.getLevel());
    var expected =
        java.util.Map.of(
            "electric", Set.<Item>of(ModItems.basic_circuit.get(), ModItems.advanced_circuit.get()),
            "nuclear", Set.<Item>of(ModItems.thermometer.get(), ModItems.portable_panel.get()),
            "greg",
                Set.<Item>of(GraviItems.ADVANCED_DRILL.get(), GraviItems.ADVANCED_LAPPACK.get()));
    for (var entry : expected.entrySet()) {
      var found = new java.util.HashSet<Item>();
      var pool = VillagerTrades.TRADES.get(RestoredVillagers.ALL.get(entry.getKey()).get());
      for (var offers : pool.values())
        for (var listing : offers) {
          var offer = listing.getOffer(villager, h.getLevel().random);
          if (offer != null) found.add(offer.getResult().getItem());
        }
      h.assertTrue(found.containsAll(entry.getValue()), "addon trade extension: " + entry.getKey());
    }
    var advanced = new ItemStack(GraviItems.ADVANCED_LAPPACK.get());
    var ultimate = new ItemStack(GraviItems.ULTIMATE_LAPPACK.get());
    var advancedItem = (IElectricItem) advanced.getItem();
    var ultimateItem = (IElectricItem) ultimate.getItem();
    h.assertTrue(
        advancedItem.getMaxCharge(advanced) == 40_000_000
            && ultimateItem.getMaxCharge(ultimate) == 100_000_000
            && ultimateItem.getTransferLimit(ultimate) == 50_000
            && ultimateItem.getTier(ultimate) == 4,
        "released lappack capacities and charging tier");
    h.succeed();
  }

  private VillagerCompatibilityGameTests() {}
}
