// SPDX-License-Identifier: GPL-3.0-only
// Energy Control integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.crossmod;

import com.igoryok95.ic2raddons.control.EnergyControl;
import com.igoryok95.ic2raddons.control.init.ModItems;
import com.igoryok95.ic2raddons.control.items.cards.ItemCardIC2;
import com.igoryok95.ic2raddons.control.items.kits.ItemKitIC2;
import com.igoryok95.ic2raddons.control.utils.DataHelper;
import ic2.api.energy.EnergyNet;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.tile.IEnergyStorage;
import ic2.core.block.comp.Energy;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorVessel;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.reactor.ItemReactorUranium;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Native EU monitoring for IC2R and all addon machines; never converts EU to FE. */
public final class CrossIC2R extends CrossModBase {
  public static IReactor reactor(BlockEntity tile) {
    if (tile instanceof IReactor reactor) return reactor;
    if (tile instanceof IReactorChamber chamber) return chamber.getReactorInstance();
    if (tile instanceof TileEntityReactorVessel vessel) return vessel.getReactorInstance();
    return null;
  }

  @Override
  public int getReactorHeat(Level level, BlockPos pos) {
    if (!level.hasChunkAt(pos)) return -1;
    var reactor = reactor(level.getBlockEntity(pos));
    if (reactor == null) {
      for (var side : Direction.values()) {
        var neighbor = pos.relative(side);
        if (level.hasChunkAt(neighbor)) reactor = reactor(level.getBlockEntity(neighbor));
        if (reactor != null) break;
      }
    }
    return reactor == null ? -1 : reactor.getHeat();
  }

  @Override
  public CompoundTag getEnergyData(BlockEntity tile) {
    if (tile == null || tile.getLevel() == null) return null;
    var data = new CompoundTag();
    if (tile instanceof Ic2TileEntity machine && machine.getComponent(Energy.class) != null) {
      var energy = machine.getComponent(Energy.class);
      data.putDouble(DataHelper.ENERGY, energy.getEnergy());
      data.putDouble(DataHelper.CAPACITY, energy.getCapacity());
      var stats =
          energy.getDelegate() == null
              ? null
              : EnergyNet.instance.getNodeStats(energy.getDelegate());
      if (stats != null) {
        data.putDouble(DataHelper.DIFF, stats.getEnergyIn() - stats.getEnergyOut());
        data.putDouble(DataHelper.OUTPUT, stats.getEnergyOut());
        data.putDouble("input", stats.getEnergyIn());
        data.putDouble("voltage", stats.getVoltage());
      }
    } else if (tile instanceof IEnergyStorage storage) {
      data.putDouble(DataHelper.ENERGY, storage.getStored());
      data.putDouble(DataHelper.CAPACITY, storage.getCapacity());
      data.putDouble(DataHelper.OUTPUT, storage.getOutputEnergyUnitsPerTick());
    } else return null;
    data.putString(DataHelper.EUTYPE, "EU");
    return data;
  }

  @Override
  public CompoundTag getCardData(BlockEntity tile) {
    if (tile
        instanceof com.igoryok95.ic2raddons.control.tileentities.TileEntityEnergyCounter counter) {
      var data = new CompoundTag();
      data.putDouble("total", counter.total());
      data.putDouble("average", counter.average());
      data.putDouble("current", counter.current());
      data.putDouble(DataHelper.OUTPUT, counter.current());
      return data;
    }
    var reactor = reactor(tile);
    if (reactor != null) {
      var data = new CompoundTag();
      data.putInt(DataHelper.HEAT, reactor.getHeat());
      data.putInt(DataHelper.MAXHEAT, reactor.getMaxHeat());
      data.putDouble(DataHelper.OUTPUT, reactor.getReactorEUEnergyOutput());
      data.putBoolean(DataHelper.ACTIVE, reactor.produceEnergy());
      int fuel = 0;
      int minimumRemaining = Integer.MAX_VALUE;
      for (int x = 0; x < 9; x++) {
        for (int y = 0; y < 6; y++) {
          var stack = reactor.getItemAt(x, y);
          if (stack != null && stack.getItem() instanceof ItemReactorUranium rod) {
            fuel += rod.numberOfCells;
            minimumRemaining = Math.min(minimumRemaining, rod.getMaxUse() - rod.getUse(stack));
          }
        }
      }
      data.putInt(DataHelper.FUEL, fuel);
      if (fuel > 0) data.putInt("timeLeft", Math.max(0, minimumRemaining));
      if (reactor instanceof TileEntityNuclearReactorElectric core && reactor.isFluidCooled()) {
        data.putDouble(DataHelper.OUTPUTHU, core.EmitHeat);
        data.putInt("coolant", core.getinputtank().getFluidAmount());
        data.putInt("hotCoolant", core.getoutputtank().getFluidAmount());
      }
      return data;
    }
    var data = getEnergyData(tile);
    if (data != null && tile instanceof Ic2TileEntity machine)
      data.putBoolean(DataHelper.ACTIVE, machine.getActive());
    return data;
  }

  @Override
  public boolean isElectricItem(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() instanceof IElectricItem;
  }

  @Override
  public double dischargeItem(ItemStack stack, int needed, int tier) {
    return isElectricItem(stack)
        ? ElectricItem.manager.discharge(stack, needed, tier, true, false, false)
        : 0;
  }

  @Override
  public void registerItems(RegisterEvent.RegisterHelper<Item> event) {
    ModItems.kit_ic2 = new ItemKitIC2(new Item.Properties());
    ModItems.card_ic2 = new ItemCardIC2(new Item.Properties());
    event.register(
        ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "kit_ic2"), ModItems.kit_ic2);
    event.register(
        ResourceLocation.fromNamespaceAndPath(EnergyControl.MODID, "card_ic2"), ModItems.card_ic2);
  }

  @Override
  public void addKitsToCreativeTab(CreativeModeTab.Output output) {
    output.accept(ModItems.kit_ic2);
  }

  @Override
  public void addCardsToCreativeTab(CreativeModeTab.Output output) {
    output.accept(ModItems.card_ic2);
  }
}
