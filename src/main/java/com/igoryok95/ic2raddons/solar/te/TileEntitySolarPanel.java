package com.igoryok95.ic2raddons.solar.te;

import com.igoryok95.ic2raddons.solar.SolarPanelType;
import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base for the four Advanced Solar Panels. A solar panel is an IC2 energy source that fills an
 * internal buffer from sunlight: full {@code dayPower} EU/t in daylight, reduced {@code nightPower}
 * at night or in the rain, and emits at the configured tier. Rebuilt on top of IC2: Refactored's
 * {@link TileEntityBaseGenerator} for 1.21.1.
 */
public abstract class TileEntitySolarPanel extends TileEntityBaseGenerator
    implements IHasGui, IGuiValueProvider {
  public static final int STATE_NONE = 0;
  public static final int STATE_NIGHT = 1;
  public static final int STATE_DAY = 2;

  protected final int dayPower;
  protected final int nightPower;
  protected final int tier;
  protected final int maxStorage;
  @GuiSynced public int generationState = STATE_NONE;

  protected TileEntitySolarPanel(
      BlockEntityType<? extends TileEntitySolarPanel> type,
      BlockPos pos,
      BlockState state,
      SolarPanelType panelType) {
    this(
        type,
        pos,
        state,
        panelType.dayPower,
        panelType.nightPower,
        panelType.maxStorage,
        panelType.tier);
  }

  protected TileEntitySolarPanel(
      BlockEntityType<? extends TileEntitySolarPanel> type,
      BlockPos pos,
      BlockState state,
      int dayPower,
      int nightPower,
      int maxStorage,
      int tier) {
    super(type, pos, state, dayPower, tier, maxStorage);
    this.dayPower = dayPower;
    this.nightPower = nightPower;
    this.maxStorage = maxStorage;
    this.tier = tier;
  }

  @Override
  protected void loadAdditional(
      net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    // Retired panel IDs resolve to Quantum; their larger buffers must obey its capacity.
    double stored = energy.getEnergy();
    if (stored > maxStorage) energy.forceAddEnergy(maxStorage - stored);
  }

  @Override
  public boolean gainEnergy() {
    int power =
        com.igoryok95.ic2raddons.solar.item.ItemSolarHelmet.generation(
            level, worldPosition, dayPower, nightPower);
    generationState = power == 0 ? STATE_NONE : power == dayPower ? STATE_DAY : STATE_NIGHT;
    double accepted = energy.addEnergy(power);
    if (accepted > 0) setChanged();
    return accepted > 0;
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
    if ("progress".equals(name)) {
      double capacity = this.energy.getCapacity();
      return capacity > 0.0D ? this.energy.getEnergy() / capacity : 0.0D;
    }

    throw new IllegalArgumentException("Unknown GUI value: " + name);
  }

  @Override
  public boolean getGuiState(String name) {
    if ("sunlight".equals(name)) {
      return this.generationState == STATE_DAY;
    }
    if ("moonlight".equals(name)) {
      return this.generationState == STATE_NIGHT;
    }

    return super.getGuiState(name);
  }

  public int getStorage() {
    return (int) this.energy.getEnergy();
  }

  public int getMaxStorage() {
    return this.maxStorage;
  }

  public String getEnergyTooltip() {
    return Component.translatable(
            "screen.ic2r_addons.solar.energy", grouped(getStorage()), grouped(maxStorage))
        .getString();
  }

  public String getEnergyTierText() {
    return Component.translatable("screen.ic2r_addons.solar.tier", tier).getString();
  }

  private static String grouped(int value) {
    var symbols = java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT);
    symbols.setGroupingSeparator(' ');
    return new java.text.DecimalFormat("#,##0", symbols).format(value);
  }

  public String getMaxOutput() {
    return String.format(
        "%s %s %s",
        tr("ic2r_addons.gui.maxOutput"),
        grouped((int) ic2.api.energy.EnergyNet.instance.getPowerFromTier(this.tier)),
        tr("ic2.generic.text.EUt"));
  }

  public String getOutput() {
    int power =
        switch (this.generationState) {
          case STATE_DAY -> this.dayPower;
          case STATE_NIGHT -> this.nightPower;
          default -> 0;
        };
    return String.format(
        "%s %s %s", tr("ic2r_addons.gui.generating"), grouped(power), tr("ic2.generic.text.EUt"));
  }

  private static String tr(String key) {
    return Component.translatable(key).getString();
  }
}
