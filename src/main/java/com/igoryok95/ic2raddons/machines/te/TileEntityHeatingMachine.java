package com.igoryok95.ic2raddons.machines.te;

import com.igoryok95.ic2raddons.solar.gui.GuiDefs;
import ic2.api.recipe.*;
import ic2.api.upgrade.*;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.*;
import ic2.core.block.machine.IWarmupControl;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Advanced Machines heat/spin progression adapted from immibis / Chocohead / craftorio. */
public abstract class TileEntityHeatingMachine extends TileEntityElectricMachine
    implements IHasGui, IGuiValueProvider, IUpgradableBlock, IWarmupControl {
  public static final int MAX_HEAT = 10000;
  public static final int DEFAULT_ACTIVE_EU = 16;
  public final InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack> inputSlot;
  public final InvSlotOutput outputSlot;
  public final InvSlotUpgrade upgradeSlot;
  protected final Redstone redstone;
  protected final int idleEU;
  protected final int activeEU;
  protected final int maxProgress;
  @GuiSynced private boolean manualWarmup;

  @Override
  public boolean isManualWarmup() {
    return manualWarmup;
  }

  @Override
  public void setManualWarmup(boolean enabled) {
    manualWarmup = enabled;
    setChanged();
  }

  @GuiSynced protected short heat;
  @GuiSynced public int progress;
  @GuiSynced public double lastEuPerTick;
  private ItemStack pendingInput = ItemStack.EMPTY;

  public TileEntityHeatingMachine(
      BlockEntityType<? extends TileEntityHeatingMachine> type,
      BlockPos pos,
      BlockState state,
      int outputs,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    this(type, pos, state, outputs, manager, 1, DEFAULT_ACTIVE_EU);
  }

  public TileEntityHeatingMachine(
      BlockEntityType<? extends TileEntityHeatingMachine> type,
      BlockPos pos,
      BlockState state,
      int outputs,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager,
      int idleEU,
      int activeEU) {
    super(type, pos, state, 10000, 2);
    this.idleEU = idleEU;
    this.activeEU = activeEU;
    maxProgress = 120000 / getSpeedFactor();
    inputSlot = new InvSlotProcessableGeneric(this, "input", 1, manager);
    outputSlot = new InvSlotOutput(this, "output", outputs);
    upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
    redstone = addComponent(new Redstone(this));
    comparator.setUpdate(() -> heat * 15 / MAX_HEAT);
  }

  @Override
  protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
    super.loadAdditional(nbt, registries);
    manualWarmup = nbt.getBoolean("manualWarmup");
    heat = (short) Math.clamp(nbt.getShort("heat"), 0, MAX_HEAT);
    progress = Math.clamp(nbt.getInt("progress"), 0, maxProgress - 1);
    pendingInput = ItemStack.parseOptional(registries, nbt.getCompound("pendingInput"));
  }

  @Override
  public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
    super.saveAdditional(nbt, registries);
    nbt.putShort("heat", heat);
    nbt.putBoolean("manualWarmup", manualWarmup);
    nbt.putInt("progress", progress);
    if (!pendingInput.isEmpty()) nbt.put("pendingInput", pendingInput.save(registries));
  }

  protected int getSpeedFactor() {
    return 1;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    lastEuPerTick = 0;
    boolean hadProgress = progress > 0;
    ItemStack input = inputSlot.get();
    if (!ItemStack.isSameItemSameComponents(input, pendingInput)) {
      progress = 0;
      pendingInput = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
    }
    boolean spinning = false;
    boolean processing = false;
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe = recipeResult();
    boolean canProcess = recipe != null;
    int maintenanceWater = getIdleWaterUse();
    if (hasWater(maintenanceWater + (canProcess ? getOperationWaterUse(recipe) : 0))) {
      if (canProcess && energy.useEnergy(activeEU)) {
        lastEuPerTick = activeEU;
        consumeWater(maintenanceWater);
        spinning = true;
        processing = true;
        heat = (short) Math.min(MAX_HEAT, heat + 1);
        progress += heat;
        while (progress >= maxProgress && recipe != null) {
          operate(recipe);
          progress -= maxProgress;
          recipe = recipeResult();
        }
        // A completion can leave a fractional tick of work; do not transfer it to a new item.
        if (inputSlot.isEmpty()) {
          progress = 0;
          pendingInput = ItemStack.EMPTY;
        }
      } else if ((manualWarmup || redstone.hasRedstoneInput()) && energy.useEnergy(idleEU)) {
        lastEuPerTick = idleEU;
        consumeWater(maintenanceWater);
        spinning = true;
        heat = (short) Math.min(MAX_HEAT, heat + 1);
      }
    }
    if (!spinning) heat = (short) Math.max(0, heat - 2);
    // Active drives IC2R's client sound lifecycle; stored heat alone is silent standby.
    if (processing) {
      if (!getActive()) activate(false);
    } else if (getActive()) shutdown(hadProgress || canProcess);
    // IC2R also treats the model's ACTIVE property as a reason to play the loop.
    if (getBlockState().getValue(Ic2TileEntityBlock.ACTIVE) != processing)
      teBlock.setActive(level, worldPosition, getBlockState(), processing);
    upgradeSlot.tickNoMark();
    // Heat, partial work and EU must survive a normal save even when no recipe finishes.
    setChanged();
  }

  protected MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipeResult() {
    if (inputSlot.isEmpty()) return null;
    var result = inputSlot.process();
    return result != null
            && outputSlot.canAdd(result.getOutput())
            && hasWater(getOperationWaterUse(result))
        ? result
        : null;
  }

  public boolean canOperate() {
    return recipeResult() != null;
  }

  protected void operate(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    Collection<ItemStack> output = new ArrayList<>(StackUtil.copy(recipe.getOutput()));
    output = adjustOutput(recipe, output);
    for (int i = 0; i < upgradeSlot.size(); i++) {
      ItemStack stack = upgradeSlot.get(i);
      if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgrade)
        output = upgrade.onProcessEnd(stack, this, output);
    }
    inputSlot.consume(recipe);
    outputSlot.add(output);
    consumeWater(getOperationWaterUse(recipe));
  }

  protected Collection<ItemStack> adjustOutput(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe,
      Collection<ItemStack> output) {
    return output;
  }

  protected int getOperationWaterUse(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    return 0;
  }

  protected int getIdleWaterUse() {
    return 0;
  }

  protected boolean hasWater(int amount) {
    return amount == 0;
  }

  protected void consumeWater(int amount) {}

  public int getHeat() {
    return heat;
  }

  public int getRawHeat() {
    return heat;
  }

  public double getWarmupPercent() {
    return Math.clamp(heat * 100D / MAX_HEAT, 0, 100);
  }

  public String getWarmupText() {
    String value = java.text.NumberFormat.getNumberInstance(Locale.ROOT).format(getWarmupPercent());
    return net.minecraft.network.chat.Component.translatable(
            "ic2r_addons.machines.gui.warmup_percent", value)
        .getString();
  }

  public int getMaxProgress() {
    return maxProgress;
  }

  public int getActiveEU() {
    return activeEU;
  }

  @Override
  public double getGuiValue(String name) {
    if (name.equals("progress")) return Math.min(1, (double) progress / maxProgress);
    throw new IllegalArgumentException("Unknown machine GUI value: " + name);
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
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.RedstoneSensitive,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return Ic2SoundEvents.MACHINE_INTERRUPT1;
  }

  @Override
  public ContainerBase<?> createServerScreenHandler(int syncId, Player player) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE, syncId, player.getInventory(), this, GuiDefs.parse(this));
  }

  @Override
  public ContainerBase<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DynamicContainer.create(
        Ic2ScreenHandlers.DYNAMIC_BE, syncId, inventory, this, GuiDefs.parse(this));
  }
}
