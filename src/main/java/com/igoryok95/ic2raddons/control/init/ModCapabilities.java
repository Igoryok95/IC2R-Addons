// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.init;

import com.igoryok95.ic2raddons.control.lib.tileentities.TileEntityInventory;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

  public static void register(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.info_panel.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.info_panel_advanced.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.holo_panel.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.range_trigger.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.thermal_monitor.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.remote_thermo.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModTileEntityTypes.kit_assembler.get(),
        TileEntityInventory::getItemHandler);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        ModTileEntityTypes.kit_assembler.get(),
        (be, side) -> be.getEnergyHandler(side));
  }
}
