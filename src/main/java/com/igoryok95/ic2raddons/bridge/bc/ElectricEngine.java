/* Thermal and piston behavior adapted from ForestryMC TileEngine/TileEngineElectric.
 * Copyright (c) 2011-2014 SirSengir and Forestry contributors. LGPL-3.0-or-later.
 * See LICENSES/ForestryCE-LGPL-3.0.txt and NOTICE.md. */
package com.igoryok95.ic2raddons.bridge.bc;

import buildcraft.api.v2.*;
import buildcraft.api.v2.energy.*;
import com.igoryok95.ic2raddons.bridge.*;
import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import ic2.api.upgrade.*;
import ic2.core.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.*;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.*;
import ic2.core.network.*;
import ic2.core.ref.Ic2ScreenHandlers;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * EU conversion and MJ delivery are separate: stored MJ, original thermal stages and complete
 * strokes.
 */
public final class ElectricEngine extends TileEntityInventory
    implements MjPortProvider, EngineTelemetry, IHasGui, IGuiValueProvider, IUpgradableBlock {
  public static final int MAX_HEAT = 3000;
  public static final long MAX_MJ_BUFFER = 10_000_000_000L;
  public final Energy energy;
  public final InvSlotDischarge dischargeSlot;
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
  @GuiSynced public int heat;
  @GuiSynced public boolean forceCooldown;
  @GuiSynced public float pistonPhase;
  @GuiSynced public boolean pistonMoving;
  @GuiSynced private long mjBuffer;
  @GuiSynced private String status = "no_energy";
  @GuiSynced private double guiEuPerTick, guiMjPerTick;
  @GuiSynced private double euDemand, mjGeneration;
  @GuiSynced private int voltage;
  private float previousPhase;
  private long budgetTick = Long.MIN_VALUE, transferred;
  private static final long MAX_TRANSFER = 8_000_000L;

  private final MjPort output =
      new MjPort() {
        public MjTransferResult insert(MjAmount amount, OperationMode mode) {
          return MjTransferResult.none(amount);
        }

        public MjTransferResult extract(MjAmount amount, OperationMode mode) {
          if (level == null
              || level.isClientSide
              || !BridgeConfig.ENABLED.get()
              || (!powered() && !pistonMoving)) return MjTransferResult.none(amount);
          resetBudget();
          long n =
              Math.min(
                  amount.microMj(), Math.min(mjBuffer, Math.max(0, deliveryLimit() - transferred)));
          if (mode == OperationMode.EXECUTE && n > 0) {
            mjBuffer -= n;
            transferred += n;
            guiMjPerTick = transferred / 1_000_000.0;
            setChanged();
          }
          return MjTransferResult.of(amount, MjAmount.ofMicro(n));
        }

        public MjAmount stored() {
          return MjAmount.ofMicro(mjBuffer);
        }

        public MjAmount capacity() {
          return MjAmount.ofMicro(MAX_MJ_BUFFER);
        }

        public boolean canInsert() {
          return false;
        }
      };

  public ElectricEngine(BlockPos pos, BlockState state) {
    super(BcContent.ENGINE_TILE.get(), pos, state);
    dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, BridgeConfig.INPUT_TIER.get());
    energy =
        addComponent(
            Energy.asBasicSink(this, BridgeConfig.CAPACITY.get(), BridgeConfig.INPUT_TIER.get())
                .addManagedSlot(dischargeSlot));
    updateInputSides();
    updateUpgrades();
  }

  private void updateInputSides() {
    energy.setDirections(EnumSet.complementOf(EnumSet.of(getFacing())), Set.of());
  }

  @Override
  protected void setFacing(net.minecraft.world.level.Level world, Direction facing) {
    Direction previous = getFacing();
    super.setFacing(world, facing);
    if (energy != null) updateInputSides();
    world.invalidateCapabilities(worldPosition);
    world.neighborChanged(worldPosition.relative(previous), getBlockType(), worldPosition);
    world.neighborChanged(worldPosition.relative(facing), getBlockType(), worldPosition);
    setChanged();
  }

  @Override
  public void onPlaced(
      net.minecraft.world.item.ItemStack stack,
      net.minecraft.world.entity.LivingEntity placer,
      Direction facing) {
    super.onPlaced(stack, placer, facing);
    alignToReceiver();
  }

  private void alignToReceiver() {
    if (level == null || level.isClientSide) return;
    var candidates = new LinkedHashSet<Direction>();
    candidates.add(getFacing());
    candidates.addAll(List.of(Direction.values()));
    for (var side : candidates) {
      var target = worldPosition.relative(side);
      if (!level.hasChunkAt(target)) continue;
      var port =
          BuildCraftApi.service(BuildCraftServices.ENERGY).port(level, target, side.getOpposite());
      if (port.isPresent() && port.get().canInsert()) {
        if (side != getFacing()) setFacing(level, side);
        return;
      }
    }
  }

  /** BCCE 8.0.25 uses these platform capabilities to build pipe topology and pull MJ. */
  public buildcraft.lib.internal.mj.IMjPassiveProvider legacyOutput(Direction side) {
    if (side == null || side != getFacing()) return null;
    return new buildcraft.lib.internal.mj.IMjPassiveProvider() {
      public boolean canConnect(buildcraft.lib.internal.mj.IMjConnector other) {
        return side == getFacing();
      }

      public long extractPower(long minimum, long maximum, boolean doExtract) {
        if (side != getFacing() || maximum <= 0 || minimum > maximum) return 0;
        var amount = MjAmount.ofMicro(maximum);
        long available = output.extract(amount, OperationMode.SIMULATE).transferred().microMj();
        if (available < Math.max(0, minimum)) return 0;
        return doExtract
            ? output.extract(amount, OperationMode.EXECUTE).transferred().microMj()
            : available;
      }
    };
  }

  private boolean powered() {
    return BridgeConfig.ENABLED.get()
        && (!BridgeConfig.REQUIRE_REDSTONE.get() || level.hasNeighborSignal(worldPosition));
  }

  private long deliveryLimit() {
    return Math.min(MAX_TRANSFER, (long) Math.floor(mjGeneration * 1_000_000));
  }

  private void resetBudget() {
    if (budgetTick != level.getGameTime()) {
      budgetTick = level.getGameTime();
      transferred = 0;
    }
  }

  private void updateUpgrades() {
    upgradeSlot.onChanged();
    double speed =
        (double) upgradeSlot.getOperationsPerTick(1000)
            * 1000
            / upgradeSlot.getOperationLength(1000);
    double rawDemand = upgradeSlot.getEnergyDemand(BridgeConfig.EU_PER_TICK.get());
    double baseMj = BridgeConfig.EU_PER_TICK.get() * BridgeConfig.MJ_PER_EU.get();
    mjGeneration = Math.min(8, baseMj * speed);
    euDemand = Math.max(1, rawDemand);
    int tier = Math.min(5, upgradeSlot.getTier(BridgeConfig.INPUT_TIER.get()));
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    voltage = (int) ic2.api.energy.EnergyNet.instance.getPowerFromTier(tier);
    // Removing an upgrade must not destroy EU that is already stored.
    energy.setCapacity(
        Math.max(
            energy.getEnergy(),
            upgradeSlot.getEnergyStorage(
                BridgeConfig.CAPACITY.get(), 1000, BridgeConfig.EU_PER_TICK.get())));
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    updateUpgrades();
    alignToReceiver();
    resetBudget();
    int oldStage = getHeatStage();
    boolean oldMoving = pistonMoving;
    if (heat >= MAX_HEAT) forceCooldown = true;
    else if (forceCooldown && heat == 0) forceCooldown = false;
    boolean fueled = !forceCooldown && energy.canUseEnergy(euDemand);
    boolean enabled = powered();
    int loss = (!fueled || !enabled ? 1 : 0) + (oldStage == 2 || oldStage == 4 ? 1 : 0);
    heat = Math.max(0, heat - loss);
    if (fueled && enabled)
      heat = Math.min(MAX_HEAT, heat + 1 + (mjBuffer > MAX_MJ_BUFFER / 2 ? 1 : 0));
    guiEuPerTick = guiMjPerTick = 0;
    long generated = (long) Math.floor(mjGeneration * 1_000_000);
    if (fueled && enabled && generated <= MAX_MJ_BUFFER - mjBuffer && energy.useEnergy(euDemand)) {
      mjBuffer += generated;
      guiEuPerTick = euDemand;

    } else if (forceCooldown) {
      mjBuffer =
          Math.max(0, mjBuffer - 2_000_000); // Forestry drains 20 RF/t during forced cooling.
    }
    boolean consumer = false;
    if (BridgeConfig.ENABLED.get() && (enabled || pistonMoving) && mjBuffer > 0) {
      var target = worldPosition.relative(getFacing());
      if (level.hasChunkAt(target)) {
        var port =
            BuildCraftApi.service(BuildCraftServices.ENERGY)
                .port(level, target, getFacing().getOpposite());
        consumer = port.isPresent() && port.get().canInsert();
        if (consumer) {
          var offered =
              MjAmount.ofMicro(Math.min(mjBuffer, Math.max(0, deliveryLimit() - transferred)));
          var accepted = port.get().insert(offered, OperationMode.EXECUTE).transferred().microMj();
          accepted = Math.clamp(accepted, 0, offered.microMj());
          mjBuffer -= accepted;
          transferred += accepted;
        }
      }
    }
    guiMjPerTick = transferred / 1_000_000.0;
    boolean work = enabled && !forceCooldown && transferred > 0;
    advancePiston(work);
    setActive(work);
    status =
        !BridgeConfig.ENABLED.get()
            ? "disabled"
            : forceCooldown
                ? "cooldown"
                : !enabled
                    ? "redstone"
                    : transferred > 0
                        ? "running"
                        : !consumer
                            ? "no_consumer"
                            : !fueled && mjBuffer == 0 ? "no_energy" : "output_full";
    if (!fueled && mjBuffer == 0 && enabled && !forceCooldown) status = "no_energy";
    if (oldStage != getHeatStage() || oldMoving != pistonMoving || level.getGameTime() % 20 == 0) {
      for (var field : List.of("heat", "pistonMoving", "pistonPhase"))
        IC2.network.get(true).updateTileEntityField(this, field);
    }
    setChanged();
  }

  private void advancePiston(boolean work) {
    previousPhase = pistonPhase;
    if (pistonMoving) {
      pistonPhase += getPistonSpeed();
      if (pistonPhase >= 1) {
        pistonPhase = 0;
        pistonMoving = false;
      }
    } else if (work) {
      pistonMoving = true;
      pistonPhase = 0;
    }
  }

  @Override
  protected void updateEntityClient() {
    advancePiston(getActive());
  }

  public int getHeatStage() {
    double ratio = (double) heat / MAX_HEAT;
    return ratio < .2 ? 0 : ratio < .45 ? 1 : ratio < .65 ? 2 : ratio < .85 ? 3 : 4;
  }

  private float getPistonSpeed() {
    return heat >= MAX_HEAT ? .08f : .03f + .01f * getHeatStage();
  }

  public float getPistonStep(float partialTick) {
    if (!pistonMoving) return 0;
    float from = previousPhase > pistonPhase ? pistonPhase : previousPhase;
    float phase = net.minecraft.util.Mth.lerp(partialTick, from, pistonPhase);
    return Math.min(phase, 1 - phase) * 12;
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    heat = Math.clamp(tag.getInt("EngineHeat"), 0, MAX_HEAT);
    forceCooldown = tag.getBoolean("ForceCooldown");
    mjBuffer = Math.clamp(tag.getLong("mjBuffer"), 0, MAX_MJ_BUFFER);
    pistonPhase = tag.getFloat("EngineProgress");
    if (!Float.isFinite(pistonPhase) || pistonPhase < 0 || pistonPhase >= 1) pistonPhase = 0;
    previousPhase = pistonPhase;
    pistonMoving = tag.getBoolean("pistonMoving");
    updateUpgrades();
  }

  @Override
  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("EngineHeat", heat);
    tag.putBoolean("ForceCooldown", forceCooldown);
    tag.putLong("mjBuffer", mjBuffer);
    tag.putFloat("EngineProgress", pistonPhase);
    tag.putBoolean("pistonMoving", pistonMoving);
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.addAll(List.of("heat", "pistonMoving", "pistonPhase"));
    return fields;
  }

  @Override
  public Optional<MjPort> mjPort(Direction side) {
    return side == getFacing() ? Optional.of(output) : Optional.empty();
  }

  @Override
  public Optional<MjPortDescriptor> mjPortDescriptor(Direction side) {
    return side == getFacing()
        ? Optional.of(
            new MjPortDescriptor(
                ResourceLocation.parse("buildcraft:mj"),
                Set.of(
                    MjPortRole.CONNECTOR,
                    MjPortRole.PROVIDER,
                    MjPortRole.PASSIVE_PROVIDER,
                    MjPortRole.READABLE),
                MjAmount.ZERO,
                MjAmount.ofMicro(deliveryLimit())))
        : Optional.empty();
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.Transformer);
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public ContainerBase<?> createServerScreenHandler(int id, Player player) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE, id, player.getInventory(), this, GuiDefs.parse(this));
  }

  @Override
  public ContainerBase<?> createClientScreenHandler(
      int id, Inventory inventory, GrowingBuffer data) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE, id, inventory, this, GuiDefs.parse(this));
  }

  @Override
  public double getGuiValue(String name) {
    if (name.equals("charge")) return energy.getFillRatio();
    throw new IllegalArgumentException("Unknown engine gauge: " + name);
  }

  @Override
  public boolean getGuiState(String name) {
    if (name.startsWith("chargeColor"))
      return Integer.parseInt(name.substring(11)) == Math.min(4, (int) (energy.getFillRatio() * 5));
    return super.getGuiState(name);
  }

  private static String number(double value) {
    return String.format(Locale.ROOT, "%.2f", value);
  }

  public String getChargeText() {
    return Component.translatable(
            "screen.ic2r_addons.engine.charge",
            String.format(Locale.ROOT, "%,.0f", energy.getEnergy()).replace(',', ' '),
            String.format(Locale.ROOT, "%,.0f", energy.getCapacity()).replace(',', ' '))
        .getString();
  }

  public String getMjText() {
    return Component.translatable("screen.ic2r_addons.engine.mj", number(mjBuffer / 1_000_000.0))
        .getString();
  }

  public String getHeatText() {
    return Component.translatable(
            "screen.ic2r_addons.engine.heat",
            number(20 + heat / 10.0),
            number(heat * 100.0 / MAX_HEAT))
        .getString();
  }

  public String getInputText() {
    return Component.translatable("screen.ic2r_addons.engine.input", number(guiEuPerTick))
        .getString();
  }

  public String getLimitText() {
    return Component.translatable("screen.ic2r_addons.engine.limit", number(mjGeneration))
        .getString();
  }

  public String getOutputText() {
    return Component.translatable("screen.ic2r_addons.engine.output", number(guiMjPerTick))
        .getString();
  }

  public String getVoltageText() {
    return Component.translatable("tooltip.ic2r_addons.engine_voltage", voltage).getString();
  }

  public String getStatusText() {
    return Component.translatable("status.ic2r_addons.engine." + status).getString();
  }

  @Override
  public CompoundTag telemetry() {
    var n = new CompoundTag();
    n.putDouble("eu", energy.getEnergy());
    n.putDouble("capacity", energy.getCapacity());
    n.putDouble("euPerTick", guiEuPerTick);
    n.putDouble("mjPerTick", guiMjPerTick);
    n.putInt("voltage", voltage);
    n.putDouble("temperature", 20 + heat / 10.0);
    n.putDouble("heatPercent", heat * 100.0 / MAX_HEAT);
    n.putLong("mjBuffer", mjBuffer);
    n.putLong("mjCapacity", MAX_MJ_BUFFER);
    n.putInt("heatStage", getHeatStage());
    n.putBoolean("redstone", level != null && level.hasNeighborSignal(worldPosition));
    n.putString("status", status);
    return n;
  }
}
