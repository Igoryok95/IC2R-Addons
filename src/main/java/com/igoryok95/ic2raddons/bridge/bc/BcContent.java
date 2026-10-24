package com.igoryok95.ic2raddons.bridge.bc;

import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.Util;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded only after the BuildCraft dependency guard. */
public final class BcContent {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, IC2RAddons.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, IC2RAddons.MODID);
  private static final DeferredRegister<BlockEntityType<?>> TILES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IC2RAddons.MODID);
  private static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IC2RAddons.MODID);
  public static final Supplier<Block> ENGINE =
      BLOCKS.register(
          "electric_engine",
          () ->
              Ic2TileEntityBlock.create(
                  BlockBehaviour.Properties.of()
                      .strength(3, 15)
                      .sound(SoundType.METAL)
                      .noOcclusion(),
                  ElectricEngine.class,
                  true,
                  Ic2TileEntityBlock.DefaultDrop.Self,
                  Util.allFacings,
                  true));
  public static final Supplier<Item> ENGINE_ITEM =
      ITEMS.register(
          "electric_engine",
          () ->
              new BlockItem(ENGINE.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(
                    ItemStack stack,
                    Item.TooltipContext context,
                    java.util.List<Component> tip,
                    TooltipFlag flag) {
                  super.appendHoverText(stack, context, tip, flag);
                  tip.add(Component.translatable("tooltip.ic2r_addons.engine_conversion"));
                  tip.add(
                      Component.translatable(
                          "tooltip.ic2r_addons.engine_voltage",
                          (int)
                              ic2.api.energy.EnergyNet.instance.getPowerFromTier(
                                  com.igoryok95.ic2raddons.bridge.BridgeConfig.INPUT_TIER.get())));
                  tip.add(Component.translatable("tooltip.ic2r_addons.engine_sides"));
                }
              });
  public static final Supplier<BlockEntityType<ElectricEngine>> ENGINE_TILE =
      TILES.register(
          "electric_engine",
          () -> BlockEntityType.Builder.of(ElectricEngine::new, ENGINE.get()).build(null));

  static {
    TABS.register(
        "buildcraft",
        () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ic2r_addons.buildcraft"))
                .icon(() -> new ItemStack(ENGINE_ITEM.get()))
                .displayItems((p, o) -> o.accept(ENGINE_ITEM.get()))
                .build());
  }

  public static void register(IEventBus bus) {
    bus.addListener(
        (net.neoforged.neoforge.event.RegisterGameTestsEvent event) ->
            event.register(BcGameTests.class));
    BLOCKS.register(bus);
    ITEMS.register(bus);
    TILES.register(bus);
    TABS.register(bus);
  }

  private BcContent() {}
}
