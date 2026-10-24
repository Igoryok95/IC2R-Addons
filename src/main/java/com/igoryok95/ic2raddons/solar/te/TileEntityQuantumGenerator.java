package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Quantum Generator: a high-tier energy source that produces a fixed, large amount of EU for
 * free, gated by a redstone signal (signal off = generating). Rebuilt on {@link
 * TileEntityBaseGenerator}.
 *
 * <p>Note: the classic mod let the player tune production and tier through GUI buttons; this port
 * runs at the classic default output (512 EU/t, tier 3). See PORTING.md.
 */
public class TileEntityQuantumGenerator extends TileEntityBaseGenerator
    implements IHasGui, IGuiValueProvider {
  public static final int DEFAULT_PRODUCTION = 512;
  public static final int DEFAULT_TIER = 3;
  private static final int STORAGE = 100000;

  @GuiSynced public int production = DEFAULT_PRODUCTION;
  @GuiSynced public int tier = DEFAULT_TIER;
  protected final Redstone redstone;

  public TileEntityQuantumGenerator(BlockPos pos, BlockState state) {
    super(
        AdvSolarBlocks.BE_QUANTUM_GENERATOR.get(),
        pos,
        state,
        DEFAULT_PRODUCTION,
        DEFAULT_TIER,
        STORAGE);
    this.redstone = this.addComponent(new Redstone(this));
  }

  @Override
  public boolean gainEnergy() {
    if (this.redstone.hasRedstoneInput()) {
      return false;
    }

    if (this.energy.getFreeEnergy() > 0.0D) {
      this.energy.addEnergy(this.production);
      this.setChanged();
      return true;
    }

    return false;
  }

  @Override
  public boolean gainFuel() {
    return false;
  }

  @Override
  public boolean needsFuel() {
    return false;
  }

  @Override
  protected boolean delayActiveUpdate() {
    return true;
  }

  @Override
  public ContainerBase<?> createServerScreenHandler(int syncId, Player player) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE,
        syncId,
        player.getInventory(),
        (TileEntityInventory) this,
        GuiDefs.parse(this));
  }

  @Override
  public ContainerBase<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE,
        syncId,
        inventory,
        (TileEntityInventory) this,
        GuiDefs.parse(this));
  }

  @Override
  public double getGuiValue(String name) {
    throw new IllegalArgumentException("Unknown GUI value: " + name);
  }

  public String getTier() {
    return this.tier > 5
        ? Component.translatable("ic2r_addons.gui.max").getString()
        : Integer.toString(this.tier);
  }
}
