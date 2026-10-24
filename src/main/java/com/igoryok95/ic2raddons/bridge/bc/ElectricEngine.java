package com.igoryok95.ic2raddons.bridge.bc;

import buildcraft.api.v2.*;
import buildcraft.api.v2.energy.*;
import com.igoryok95.ic2raddons.bridge.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 energy sink and directional BC MJ provider. Only committed MJ transfers consume EU. */
public final class ElectricEngine extends Ic2TileEntity implements MjPortProvider, EngineTelemetry {
  public final Energy energy;
  private long budgetTick = Long.MIN_VALUE, transferred;
  private String status = "no_energy";
  private final MjPort output =
      new MjPort() {
        public MjTransferResult insert(MjAmount amount, OperationMode mode) {
          return MjTransferResult.none(amount);
        }

        public MjTransferResult extract(MjAmount amount, OperationMode mode) {
          if (level == null || level.isClientSide || !enabled())
            return MjTransferResult.none(amount);
          resetBudget();
          long n = Math.min(amount.microMj(), available());
          if (mode == OperationMode.EXECUTE && n > 0) commit(n);
          return MjTransferResult.of(amount, MjAmount.ofMicro(n));
        }

        public MjAmount stored() {
          return MjAmount.ofMicro(toMicro(energy.getEnergy()));
        }

        public MjAmount capacity() {
          return MjAmount.ofMicro(toMicro(energy.getCapacity()));
        }

        public boolean canInsert() {
          return false;
        }
      };

  public ElectricEngine(BlockPos pos, BlockState state) {
    super(BcContent.ENGINE_TILE.get(), pos, state);
    energy =
        addComponent(
            Energy.asBasicSink(this, BridgeConfig.CAPACITY.get(), BridgeConfig.INPUT_TIER.get()));
    updateInputSides();
  }

  private void updateInputSides() {
    energy.setDirections(EnumSet.complementOf(EnumSet.of(getFacing())), Set.of());
  }

  @Override
  protected void setFacing(net.minecraft.world.level.Level world, Direction facing) {
    super.setFacing(world, facing);
    if (energy != null) updateInputSides();
  }

  private long toMicro(double eu) {
    return (long) Math.floor(Math.max(0, eu) * BridgeConfig.MJ_PER_EU.get() * 1000000.0);
  }

  private boolean enabled() {
    return BridgeConfig.ENABLED.get()
        && (!BridgeConfig.REQUIRE_REDSTONE.get() || level.hasNeighborSignal(worldPosition));
  }

  private void resetBudget() {
    if (budgetTick != level.getGameTime()) {
      budgetTick = level.getGameTime();
      transferred = 0;
    }
  }

  private long available() {
    return Math.min(
        toMicro(energy.getEnergy()),
        Math.max(0, toMicro(BridgeConfig.EU_PER_TICK.get()) - transferred));
  }

  private void commit(long micro) {
    double cost = micro / (BridgeConfig.MJ_PER_EU.get() * 1000000.0);
    energy.useEnergy(cost);
    transferred += micro;
    setChanged();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    resetBudget();
    status =
        !BridgeConfig.ENABLED.get()
            ? "disabled"
            : !enabled() ? "redstone" : available() == 0 ? "no_energy" : "no_consumer";
    if (enabled() && available() > 0) {
      BlockPos target = worldPosition.relative(getFacing());
      if (level.hasChunkAt(target)) {
        var port =
            BuildCraftApi.service(BuildCraftServices.ENERGY)
                .port(level, target, getFacing().getOpposite());
        if (port.isPresent() && port.get().canInsert()) {
          var offered = MjAmount.ofMicro(available());
          var accepted = port.get().insert(offered, OperationMode.EXECUTE).transferred().microMj();
          if (accepted > 0) commit(accepted);
          status = accepted > 0 ? "running" : "output_full";
        }
      }
    }
    if (transferred > 0) status = "running";
    setActive(transferred > 0);
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
                MjAmount.ofMicro(toMicro(BridgeConfig.EU_PER_TICK.get()))))
        : Optional.empty();
  }

  @Override
  public CompoundTag telemetry() {
    var n = new CompoundTag();
    n.putDouble("eu", energy.getEnergy());
    n.putDouble("capacity", energy.getCapacity());
    n.putDouble("euPerTick", transferred / (BridgeConfig.MJ_PER_EU.get() * 1000000.0));
    n.putDouble("mjPerTick", transferred / 1000000.0);
    n.putInt(
        "voltage",
        (int) ic2.api.energy.EnergyNet.instance.getPowerFromTier(BridgeConfig.INPUT_TIER.get()));
    n.putString("status", status);
    return n;
  }
}
