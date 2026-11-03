package com.igoryok95.ic2raddons.solar.client;

import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import com.igoryok95.ic2raddons.solar.te.TileEntityQuantumGenerator;
import com.igoryok95.ic2raddons.solar.te.TileEntitySolarPanel;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** ASP artwork already contains complete GUI frames, including transparent outer corners. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class SolarMachineScreen extends DynamicGui<TileEntityInventory> {
  private SolarMachineScreen(
      DynamicContainer<TileEntityInventory> menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @Override
  protected void drawBackgroundAndTitle(
      GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    // XML draws the original background and localized title; no default gray frame underneath.
  }

  @SubscribeEvent
  @SuppressWarnings("unchecked")
  public static void open(ScreenEvent.Opening event) {
    if (event.getNewScreen() instanceof DynamicGui<?> screen
        && !(screen instanceof SolarMachineScreen)
        && (screen.getMenu().base instanceof TileEntityMolecularTransformer
            || screen.getMenu().base instanceof TileEntitySolarPanel
            || screen.getMenu().base instanceof TileEntityQuantumGenerator)) {
      event.setNewScreen(
          new SolarMachineScreen(
              (DynamicContainer<TileEntityInventory>) screen.getMenu(),
              Minecraft.getInstance().player.getInventory(),
              screen.getTitle()));
    }
  }
}
