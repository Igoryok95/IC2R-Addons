package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardEnergyArray;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardLiquidAdvanced;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardReader;
import com.igoryok95.ic2raddons.control.utils.FluidInfo;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class LargeReadingGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void energyArrayKeepsLargeReadingsAndPercentages(GameTestHelper h) {
    ItemStack card = new ItemStack(ModItems.card_energy_array.get());
    var reader = new ItemCardReader(card);
    reader.setInt("cardCount", 2);
    for (int i = 0; i < 2; i++) {
      BlockPos p = new BlockPos(i, 1, 1);
      h.setBlock(p, Ic2Blocks.MFSU);
      TileEntityElectricMFSU store = h.getBlockEntity(p);
      store.energy.setCapacity(4_000_000_000D);
      store.energy.addEnergy(3_000_000_000D);
      BlockPos world = h.absolutePos(p);
      reader.setInt("_" + i + "x", world.getX());
      reader.setInt("_" + i + "y", world.getY());
      reader.setInt("_" + i + "z", world.getZ());
    }
    var item = (ItemCardEnergyArray) card.getItem();
    CardState state = item.update(h.getLevel(), reader, 8, h.absolutePos(new BlockPos(1, 1, 1)));
    reader.setState(state);
    h.assertTrue(
        state == CardState.OK
            && reader.getDouble("_0energy") == 3_000_000_000D
            && reader.getDouble("_0maxCapacity") == 4_000_000_000D
            && reader.getDouble("energy") == 6_000_000_000D,
        "per-target and combined readings retain all EU");
    var rows = item.getStringData(h.getLevel(), 63, reader, true, false);
    h.assertTrue(rows.get(rows.size() - 1).textLeft.equals("75"), "combined percentage is 75");
    var bars = PortableProgressBars.forRows(card, reader);
    h.assertTrue(
        bars.size() == 3 && bars.values().stream().allMatch(v -> v == .75),
        "both target bars and summary use the full 64-bit readings");
    reader.setInt("_0energy", 500);
    reader.setInt("_0maxCapacity", 1000);
    h.assertTrue(
        PortableProgressBars.forRows(card, reader).containsValue(.5),
        "old saved integer card data remains readable");
    reader.setDouble("_0energy", (double) Integer.MIN_VALUE);
    h.assertTrue(
        PortableProgressBars.forRows(card, reader).size() == 2,
        "missing target status is excluded from totals and bars");
    h.succeed();
  }

  @GameTest(template = EMPTY)
  public static void advancedTankCardKeepsLongAmountsAndLegacyIntData(GameTestHelper h) {
    ItemStack card = new ItemStack(ModItems.card_liquid_advanced.get());
    var reader = new ItemCardReader(card);
    reader.setState(CardState.OK);
    reader.setInt("count", 1);
    new FluidInfo(Fluids.WATER, 3_000_000_000L, 4_000_000_000L).write(reader, 0);
    var item = (ItemCardLiquidAdvanced) card.getItem();
    var rows = item.getStringData(h.getLevel(), 31, reader, true, false);
    h.assertTrue(
        rows.get(1).textLeft.equals("3 000 000 000 mB")
            && rows.get(2).textLeft.equals("1 000 000 000 mB")
            && rows.get(4).textLeft.equals("75"),
        "amount, free space and percentage do not truncate");
    h.assertTrue(
        PortableProgressBars.forRows(card, reader).containsValue(.75),
        "large tank bar is 75 percent");
    reader.setInt("_0amount", 250);
    reader.setInt("_0capacity", 1000);
    h.assertTrue(
        PortableProgressBars.forRows(card, reader).containsValue(.25),
        "older tank cards remain readable");
    h.succeed();
  }
}
