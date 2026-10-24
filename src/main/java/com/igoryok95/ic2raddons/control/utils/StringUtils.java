// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import net.minecraft.world.item.ItemStack;

public class StringUtils {
  private static DecimalFormat formatter;

  private static DecimalFormat getFormatter() {
    if (formatter == null) {
      DecimalFormat lFormatter = new DecimalFormat("#,###.###");
      DecimalFormatSymbols smb = new DecimalFormatSymbols();
      smb.setGroupingSeparator(' ');
      lFormatter.setDecimalFormatSymbols(smb);
      formatter = lFormatter;
    }
    return formatter;
  }

  public static String getFormatted(String resourceName, String value, boolean showLabels) {
    if (showLabels) return Translator.get(resourceName, value);
    return value;
  }

  public static String getFormatted(String resourceName, double value, boolean showLabels) {
    return getFormatted(resourceName, getFormatter().format(value), showLabels);
  }

  public static String getFormattedKey(String resourceName, Object... arguments) {
    return Translator.get(resourceName, arguments);
  }

  public static String getItemName(ItemStack stack) {
    return stack.getHoverName().getString();
  }
}
