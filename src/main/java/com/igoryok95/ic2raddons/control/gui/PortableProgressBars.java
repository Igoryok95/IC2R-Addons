// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.gui;

import com.igoryok95.ic2raddons.control.api.CardState;
import com.igoryok95.ic2raddons.control.api.ICardReader;
import com.igoryok95.ic2raddons.control.items.cards.*;
import com.igoryok95.ic2raddons.control.utils.DataHelper;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only decoration for the portable panel's all-fields row layout. No packet, item component,
 * card setting, or sensor value is written here.
 */
final class PortableProgressBars {

  static Map<Integer, Double> forRows(ItemStack stack, ICardReader reader) {
    Map<Integer, Double> bars = new HashMap<>();
    if (reader.getState() != CardState.OK) return bars;
    int row = reader.getTitleList().size();
    if (stack.getItem() instanceof ItemCardEnergy) {
      put(
          bars,
          row + 3,
          reader.getDouble(DataHelper.ENERGY),
          reader.getDouble(DataHelper.CAPACITY));
    } else if (stack.getItem() instanceof ItemCardLiquid) {
      put(bars, row + 4, reader.getLong("amount"), reader.getLong("capacity"));
    } else if (stack.getItem() instanceof ItemCardEnergyArray
        || stack.getItem() instanceof ItemCardLiquidArray) {
      boolean energy = stack.getItem() instanceof ItemCardEnergyArray;
      double total = 0;
      double capacity = 0;
      int rowsPerTarget = energy ? 4 : 5;
      for (int i = 0; i < reader.getCardCount(); i++) {
        double amount =
            energy ? reader.getDouble("_" + i + "energy") : reader.getLong("_" + i + "amount");
        if (amount == Integer.MIN_VALUE || amount == Integer.MIN_VALUE + 1) {
          row++; // Out-of-range/no-target row.
          continue;
        }
        double size =
            energy
                ? reader.getDouble("_" + i + "maxCapacity")
                : reader.getLong("_" + i + "capacity");
        put(bars, row + rowsPerTarget - 1, amount, size);
        row += rowsPerTarget;
        total += amount;
        capacity += size;
      }
      put(bars, row + 3, total, capacity); // Four-row summary.
    } else if (stack.getItem() instanceof ItemCardLiquidAdvanced) {
      for (int i = 0; i < reader.getInt("count"); i++) {
        if (!reader.hasField("_" + i + "capacity")) continue;
        put(
            bars,
            row + 4,
            reader.getLong("_" + i + "amount"),
            reader.getLong("_" + i + "capacity"));
        row += 5;
      }
    }
    return bars;
  }

  private static void put(Map<Integer, Double> bars, int row, double amount, double capacity) {
    // An unknown/zero capacity has no meaningful graphical fraction. Keep original text.
    if (capacity > 0 && Double.isFinite(capacity) && Double.isFinite(amount))
      bars.put(row, Math.max(0, Math.min(1, amount / capacity)));
  }
}
