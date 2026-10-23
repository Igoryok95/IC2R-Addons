package com.chocohead.advsolar.te;

import com.chocohead.advsolar.AdvSolarBlocks;
import com.chocohead.advsolar.gui.GuiDefs;
import com.chocohead.advsolar.recipe.MTRecipe;
import com.chocohead.advsolar.recipe.MolecularTransformerRecipes;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Molecular Transformer: an extreme-tier energy sink that converts one item into another by
 * pouring a recipe-defined total amount of EU into it. Rebuilt on {@link TileEntityElectricMachine}
 * (an IC2 energy-sink buffer) for 1.21.1, with synchronized datapack recipes.
 */
public class TileEntityMolecularTransformer extends TileEntityElectricMachine
    implements IHasGui, IGuiValueProvider {
  private static final int BUFFER = 100000;
  private static final int SINK_TIER = 14;

  public final InvSlot inputSlot =
      new InvSlot(this, "input", InvSlot.Access.IO, 1) {
        @Override
        public boolean accepts(ItemStack stack) {
          return MolecularTransformerRecipes.find(stack, level) != null;
        }
      };
  public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);

  @GuiSynced public ItemStack currentOutput = ItemStack.EMPTY;
  private ItemStack pendingInput = ItemStack.EMPTY;
  @GuiSynced public int totalEU;
  @GuiSynced public double energyUsed;
  @GuiSynced public double lastEnergyIn;

  public TileEntityMolecularTransformer(BlockPos pos, BlockState state) {
    super(AdvSolarBlocks.BE_MOLECULAR_TRANSFORMER.get(), pos, state, BUFFER, SINK_TIER);
    this.comparator.setUpdate(
        () -> this.totalEU == 0 ? 0 : (int) (this.energyUsed * 15.0D / this.totalEU));
  }

  @Override
  protected void loadAdditional(
      CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
    super.loadAdditional(nbt, registries);
    this.energyUsed = Math.max(0, nbt.getDouble("energyUsed"));
    if (!Double.isFinite(energyUsed)) energyUsed = 0;
    pendingInput = ItemStack.parseOptional(registries, nbt.getCompound("pendingInput"));
    this.totalEU = nbt.getInt("totalEU");
    this.currentOutput = ItemStack.parseOptional(registries, nbt.getCompound("output"));
  }

  @Override
  public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
    super.saveAdditional(nbt, registries);
    nbt.putDouble("energyUsed", this.energyUsed);
    if (!pendingInput.isEmpty()) nbt.put("pendingInput", pendingInput.save(registries));
    nbt.putInt("totalEU", this.totalEU);
    if (!this.currentOutput.isEmpty()) {
      nbt.put("output", this.currentOutput.save(registries));
    }
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    MTRecipe recipe = MolecularTransformerRecipes.find(inputSlot.get(0), level);
    lastEnergyIn = 0;
    if (recipe == null) {
      if (!currentOutput.isEmpty() || energyUsed != 0) {
        currentOutput = ItemStack.EMPTY;
        pendingInput = ItemStack.EMPTY;
        energyUsed = 0;
        totalEU = 0;
        setChanged();
      }
      shutdown(false);
      return;
    }
    ItemStack input = inputSlot.get(0);
    if (!ItemStack.isSameItemSameComponents(input, pendingInput)
        || totalEU != recipe.totalEU
        || !ItemStack.isSameItemSameComponents(currentOutput, recipe.output)
        || currentOutput.getCount() != recipe.output.getCount()) {
      pendingInput = input.copyWithCount(1);
      currentOutput = recipe.output.copy();
      totalEU = recipe.totalEU;
      energyUsed = 0;
      setChanged();
    }
    if (!outputSlot.canAdd(currentOutput)) {
      shutdown(false);
      return;
    }
    double used = Math.min(energy.getEnergy(), Math.max(0, totalEU - energyUsed));
    if (used > 0 && energy.useEnergy(used)) {
      energyUsed += used;
      lastEnergyIn = used;
      setChanged();
    }
    if (energyUsed >= totalEU) {
      ItemStack remaining = input.copy();
      remaining.shrink(1);
      inputSlot.put(0, remaining);
      outputSlot.add(currentOutput.copy());
      currentOutput = ItemStack.EMPTY;
      pendingInput = ItemStack.EMPTY;
      totalEU = 0;
      energyUsed = 0;
      setChanged();
    }
    if (used > 0) activate(false);
    else shutdown(false);
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
      return this.totalEU == 0 ? 0.0D : this.energyUsed / this.totalEU;
    }

    throw new IllegalArgumentException("Unknown GUI value: " + name);
  }

  public String getInput() {
    return displayName(this.inputSlot.get(0));
  }

  public String getOutput() {
    return displayName(this.currentOutput);
  }

  private String displayName(ItemStack stack) {
    if (stack.isEmpty()) return "";
    String name = stack.getHoverName().getString();
    return level != null && level.isClientSide
        ? net.minecraft.client.Minecraft.getInstance().font.plainSubstrByWidth(name, 98)
        : name;
  }

  public String getEnergyNeeded() {
    return this.totalEU == 0
        ? ""
        : String.format(
            "%,d %s", this.totalEU, Component.translatable("ic2.generic.text.EU").getString());
  }

  public String getEU() {
    return this.currentOutput.isEmpty()
        ? ""
        : String.format(
            "%,.0f %s",
            this.lastEnergyIn, Component.translatable("ic2.generic.text.EUt").getString());
  }

  public String getPercent() {
    return this.totalEU == 0
        ? ""
        : String.format("%,.0f%%", this.energyUsed * 100.0D / this.totalEU);
  }
}
