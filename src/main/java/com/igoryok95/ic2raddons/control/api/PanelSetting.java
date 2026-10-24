// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.api;

import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;

/**
 * Object of PanelSetting class defines one checkbox in the card's settings.
 *
 * @author Shedar
 * @see IItemCard#getSettingsList(ItemStack)
 */
public class PanelSetting {
  /** Name of the option */
  public String title;

  /** Bit number in display settings. Should be in the range 0-31. */
  public int displayBit;

  /**
   * @param title Translation key of the name of the option
   * @param displayBit Bit number in display settings. Should be in the range 0-31.
   */
  public PanelSetting(String title, int displayBit) {
    this.title = Language.getInstance().getOrDefault(title);
    this.displayBit = displayBit;
  }
}
