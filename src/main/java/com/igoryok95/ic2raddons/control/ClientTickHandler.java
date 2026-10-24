// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control;

import com.igoryok95.ic2raddons.control.network.ChannelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = EnergyControl.MODID, value = Dist.CLIENT)
public class ClientTickHandler {
  public static boolean altPressed;

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Pre event) {
    Minecraft mc = Minecraft.getInstance();
    boolean alt = Screen.hasAltDown();
    if (altPressed != alt) {
      altPressed = alt;
      if (mc.getConnection() != null) ChannelHandler.updateSeverKeys(alt);
    }
  }
}
