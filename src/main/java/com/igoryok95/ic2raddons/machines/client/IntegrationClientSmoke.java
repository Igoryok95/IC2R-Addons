package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.IC2RAddons;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.GuiElement;
import ic2.core.gui.RecipeButton;
import ic2.core.ref.Ic2Blocks;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Single opt-in pass over the real JEI menus, transfer handlers, GUI text and Jade overlays. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class IntegrationClientSmoke {
  public static void runtime(Object value) {
    if (Boolean.getBoolean("ic2r_addons.integrationSmoke")) SmokeRun.runtime(value);
  }

  @SubscribeEvent
  public static void onTick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.integrationSmoke")
        || !net.neoforged.fml.ModList.get().isLoaded("jei")) return;
    SmokeRun.tick(event);
  }

  private static final class SmokeRun {
    private record Case(Block block, String category) {}

    private static final BlockPos POS = new BlockPos(0, 128, 48);
    private static IJeiRuntime runtime;
    private static List<Case> cases;
    private static int ticks = Integer.getInteger("ic2r_addons.integrationStart", 0) * 65;
    private static int totalTicks;
    private static IRecipeLayoutDrawable<?> layout;
    private static IRecipeTransferHandler handler;
    private static int recipesChecked;

    public static void runtime(Object value) {
      runtime = (IJeiRuntime) value;
    }

    @SuppressWarnings({"rawtypes", "unchecked", "removal"})
    public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
      if (!Boolean.getBoolean("ic2r_addons.integrationSmoke")) return;
      var mc = Minecraft.getInstance();
      if (totalTicks > 0 && mc.getSingleplayerServer() == null) {
        mc.stop();
        return;
      }
      if (mc.player == null
          || mc.level == null
          || mc.getSingleplayerServer() == null
          || runtime == null) return;
      mc.options.pauseOnLostFocus = false;
      if (++totalTicks > 7200) throw new IllegalStateException("Integration smoke timed out");
      if (cases == null) cases = cases();
      int index = ticks / 65;
      int phase = ticks % 65;
      ticks++;
      if (index >= Integer.getInteger("ic2r_addons.integrationEnd", Integer.MAX_VALUE)) {
        IC2RAddons.LOGGER.info(
            "INTEGRATION_SMOKE_RANGE_PASS: end={} recipe_checks={}", index, recipesChecked);
        mc.stop();
        return;
      }
      if (index >= cases.size()) {
        jade(mc, index - cases.size(), phase);
        return;
      }
      var entry = cases.get(index);
      if (phase == 0) setup(mc, entry);
      if (phase == 8) {
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              var tile = player.serverLevel().getBlockEntity(POS);
              if (tile instanceof IHasGui gui) gui.openManagedBe(player, InteractionHand.MAIN_HAND);
              else
                player.openMenu(
                    (net.minecraft.world.MenuProvider) tile, buf -> buf.writeBlockPos(POS));
            });
      }
      if (phase == 15) {
        if (!(mc.screen
            instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)) {
          ticks--;
          return;
        }
        RecipeType type =
            runtime
                .getRecipeManager()
                .getRecipeType(ResourceLocation.parse(entry.category))
                .orElseThrow();
        var category = runtime.getRecipeManager().getRecipeCategory(type);
        handler =
            (IRecipeTransferHandler)
                runtime
                    .getRecipeTransferManager()
                    .getRecipeTransferHandler(mc.player.containerMenu, category)
                    .orElse(null);
        if (handler == null)
          throw new IllegalStateException("No transfer handler: " + entry.category);
        var recipes = runtime.getRecipeManager().createRecipeLookup(type).get().toList();
        if (recipes.isEmpty()) throw new IllegalStateException("Empty category: " + entry.category);
        var focus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        layout = null;
        for (Object recipe : recipes) {
          IRecipeLayoutDrawable candidate =
              (IRecipeLayoutDrawable)
                  runtime
                      .getRecipeManager()
                      .createRecipeLayoutDrawable(category, recipe, focus)
                      .orElseThrow();
          var error =
              handler.transferRecipe(
                  mc.player.containerMenu,
                  recipe,
                  candidate.getRecipeSlotsView(),
                  mc.player,
                  false,
                  false);
          if (error != null && error.getType().name().equals("INTERNAL"))
            throw new IllegalStateException("Internal JEI transfer error: " + entry.category);
          if (layout == null) layout = candidate;
          recipesChecked++;
        }
        var ingredients = new ArrayList<ItemStack>();
        for (var view : layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT))
          view.getItemStacks()
              .findFirst()
              .ifPresent(stack -> ingredients.add(stack.copyWithCount(stack.getMaxStackSize())));
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              for (int i = 0; i < ingredients.size(); i++)
                player.getInventory().setItem(9 + i, ingredients.get(i));
              player.containerMenu.broadcastChanges();
            });
        if (mc.font.width("100.00%") > 43)
          throw new IllegalStateException("Warmup text exceeds its column");
      }
      if (phase == 25) {
        var error =
            handler.transferRecipe(
                mc.player.containerMenu,
                layout.getRecipe(),
                layout.getRecipeSlotsView(),
                mc.player,
                false,
                true);
        if (error != null)
          throw new IllegalStateException(
              "Positive JEI transfer failed: " + entry.category + " " + error.getType());
      }
      if (phase == 35) {
        var inputs =
            mc.player.containerMenu.slots.stream()
                .filter(
                    slot ->
                        (mc.player.containerMenu
                                    instanceof
                                    com.igoryok95.ic2raddons.control.containers
                                        .ContainerKitAssembler
                                && slot.index >= 1
                                && slot.index <= 3)
                            || slot instanceof ic2.core.slot.SlotInvSlot inventory
                                && (inventory.invSlot.name.equals("input")
                                    || inventory.invSlot.name.equals("inputA")
                                    || inventory.invSlot.name.equals("inputB")))
                .toList();
        if (inputs.stream().noneMatch(slot -> slot.hasItem()))
          throw new IllegalStateException("Transfer did not reach the machine: " + entry.category);
        if (entry.block == AdvMachinesBlocks.COMPACTING_RECYCLER.get()
            && inputs.getFirst().getItem().getCount() < 9)
          throw new IllegalStateException("Recycler transfer must supply all nine scrap items");
        Screenshot.grab(
            mc.gameDirectory,
            "integration-gui-" + index + ".png",
            mc.getMainRenderTarget(),
            text -> {});
        if (!(mc.screen instanceof Ic2Gui<?> gui)) {
          runtime.getRecipesGui().showTypes(List.of(layout.getRecipeCategory().getRecipeType()));
          return;
        }
        var elementsField = Ic2Gui.class.getDeclaredField("elements");
        elementsField.setAccessible(true);
        var elements = (List<GuiElement<?>>) elementsField.get(gui);
        var button =
            elements.stream().filter(element -> element instanceof RecipeButton).findFirst();
        if (button.isPresent()) {
          var x = GuiElement.class.getDeclaredField("x");
          var y = GuiElement.class.getDeclaredField("y");
          x.setAccessible(true);
          y.setAccessible(true);
          gui.mouseClicked(
              gui.getX() + x.getInt(button.get()) + 1, gui.getY() + y.getInt(button.get()) + 1, 0);
        } else
          runtime.getRecipesGui().showTypes(List.of(layout.getRecipeCategory().getRecipeType()));
      }
      if (phase == 55) {
        Screenshot.grab(
            mc.gameDirectory,
            "integration-jei-" + index + ".png",
            mc.getMainRenderTarget(),
            text -> {});
        IC2RAddons.LOGGER.info("INTEGRATION_SMOKE_MENU_PASS: {} {}", entry.block, entry.category);
      }
    }

    private static void setup(Minecraft mc, Case entry) {
      mc.setScreen(null);
      var server = mc.getSingleplayerServer();
      var id = mc.player.getUUID();
      server.execute(
          () -> {
            var player = server.getPlayerList().getPlayer(id);
            player.closeContainer();
            player.setGameMode(GameType.CREATIVE);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().clearContent();
            var level = player.serverLevel();
            level.setBlockAndUpdate(POS.below(), Blocks.BEDROCK.defaultBlockState());
            level.removeBlock(POS, false);
            level.setBlockAndUpdate(POS, entry.block.defaultBlockState());
            var tile = level.getBlockEntity(POS);
            if (tile instanceof TileEntityMetalFormer metal) {
              metal.setMode(
                  entry.category.endsWith("rolling")
                      ? 1
                      : entry.category.endsWith("cutting") ? 2 : 0);
            }
            player.teleportTo(.5, 128, 45.5);
          });
    }

    private static List<Case> cases() {
      var list = new ArrayList<Case>();
      list.add(new Case(Ic2Blocks.MACERATOR, "ic2:macerator"));
      list.add(new Case(Ic2Blocks.COMPRESSOR, "ic2:compressor"));
      list.add(new Case(Ic2Blocks.EXTRACTOR, "ic2:extractor"));
      list.add(new Case(Ic2Blocks.CENTRIFUGE, "ic2:centrifuge"));
      list.add(new Case(Ic2Blocks.BLOCK_CUTTER, "ic2:block_cutter"));
      list.add(new Case(Ic2Blocks.BLAST_FURNACE, "ic2:blast_furnace"));
      list.add(new Case(Ic2Blocks.ORE_WASHING_PLANT, "ic2:ore_washer"));
      list.add(new Case(Ic2Blocks.SOLID_CANNER, "ic2:canner_bottle"));
      list.add(new Case(Ic2Blocks.CANNER, "ic2:canner_bottle"));
      for (String mode : List.of("extruding", "rolling", "cutting"))
        list.add(new Case(Ic2Blocks.METAL_FORMER, "ic2:metal_former_" + mode));
      for (var block :
          List.of(Ic2Blocks.IRON_FURNACE, Ic2Blocks.ELECTRIC_FURNACE, Ic2Blocks.INDUCTION_FURNACE))
        list.add(new Case(block, "minecraft:smelting"));
      list.add(new Case(AdvMachinesBlocks.ROTARY_MACERATOR.get(), "ic2:macerator"));
      list.add(new Case(AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(), "ic2:compressor"));
      list.add(new Case(AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(), "ic2:extractor"));
      list.add(
          new Case(AdvMachinesBlocks.COMPACTING_RECYCLER.get(), "ic2r_addons:compacting_recycler"));
      list.add(new Case(AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(), "ic2:metal_former_extruding"));
      list.add(new Case(AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(), "ic2:metal_former_rolling"));
      list.add(new Case(AdvMachinesBlocks.WATER_JET_CUTTER.get(), "ic2:metal_former_cutting"));
      list.add(new Case(AdvMachinesBlocks.THERMAL_WASHER.get(), "ic2:ore_washer"));
      list.add(
          new Case(
              AdvSolarBlocks.MOLECULAR_TRANSFORMER.get(), "ic2r_addons:molecular_transformer"));
      list.add(
          new Case(
              com.igoryok95.ic2raddons.control.init.ModItems.kit_assembler.get(),
              "ic2r_addons:kit_assembler"));
      return List.copyOf(list);
    }

    private static void jade(Minecraft mc, int index, int phase) {
      var blocks =
          List.of(
              Ic2Blocks.BATBOX,
              Ic2Blocks.MFSU,
              Ic2Blocks.INDUCTION_FURNACE,
              AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get(),
              AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get(),
              AdvMachinesBlocks.ROTARY_MACERATOR.get(),
              AdvSolarBlocks.MOLECULAR_TRANSFORMER.get());
      if (index >= blocks.size()) {
        IC2RAddons.LOGGER.info(
            "INTEGRATION_SMOKE_PASS: menus={} recipe_checks={} jade_views={}",
            cases.size(),
            recipesChecked,
            blocks.size());
        mc.stop();
        return;
      }
      if (phase == 0) {
        setup(mc, new Case(blocks.get(index), ""));
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              var tile = (Ic2TileEntity) player.serverLevel().getBlockEntity(POS);
              var energy = tile.getComponent(Energy.class);
              if (energy != null) energy.addEnergy(energy.getCapacity() / 2);
              if (tile instanceof ic2.core.block.machine.tileentity.TileEntityInduction induction) {
                induction.heat = 10000;
                induction.setManualWarmup(true);
              }
              player.serverLevel().setDayTime(6000);
            });
      }
      if (phase == 20) mc.player.closeContainer();
      mc.player.setYRot(0);
      mc.player.setXRot(20);
      if (phase == 50)
        Screenshot.grab(
            mc.gameDirectory,
            "integration-jade-" + index + ".png",
            mc.getMainRenderTarget(),
            text -> {});
    }
  }
}
