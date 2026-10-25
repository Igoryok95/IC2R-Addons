package com.igoryok95.ic2raddons.machines.client;

import com.igoryok95.ic2raddons.machines.AdvMachinesBlocks;
import com.igoryok95.ic2raddons.machines.te.TileEntityHeatingMachine;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.IWarmupControl;
import ic2.core.block.machine.tileentity.TileEntityBetterItemBuffer;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in, isolated pass over the changed screens, using actual C2S button clicks. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class WarmupClientSmoke {
  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.warmupSmoke")) return;
    if (!net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy")) return;
    SmokeRun.tick(event);
  }

  private static final class SmokeRun {
    private static volatile List<BlockPos> positions;
    private static int ticks;
    private static java.util.concurrent.CompletableFuture<Void> englishReload;
    private static int englishTicks;

    public static void tick(ClientTickEvent.Post event) {
      if (!Boolean.getBoolean("ic2r_addons.warmupSmoke")) return;
      var mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
      mc.options.pauseOnLostFocus = false;
      var server = mc.getSingleplayerServer();
      var id = mc.player.getUUID();
      if (ticks++ == 0)
        server.execute(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              player.setGameMode(GameType.CREATIVE);
              player.getAbilities().flying = true;
              player.onUpdateAbilities();
              for (int x = -3; x < 34; x++)
                for (int z = 10; z < 19; z++)
                  player
                      .serverLevel()
                      .setBlockAndUpdate(
                          new BlockPos(x, 127, z),
                          net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState());
              var blocks =
                  new ArrayList<Block>(
                      List.of(
                          Ic2Blocks.INDUCTION_FURNACE,
                          Ic2Blocks.CENTRIFUGE,
                          Ic2Blocks.BLAST_FURNACE));
              for (var entry : AdvMachinesBlocks.BLOCKS.getEntries()) blocks.add(entry.get());
              blocks.add(Ic2Blocks.ITEM_BUFFER_2);
              blocks.add(com.igoryok95.ic2raddons.bridge.bc.BcContent.ENGINE.get());
              var placed = new ArrayList<BlockPos>();
              for (int i = 0; i < blocks.size(); i++) {
                var pos = new BlockPos(i * 2, 128, 16);
                var level = player.serverLevel();
                level.removeBlock(pos, false);
                level.setBlockAndUpdate(pos, blocks.get(i).defaultBlockState());
                var tile = (Ic2TileEntity) level.getBlockEntity(pos);
                var energy = tile.getComponent(Energy.class);
                if (energy != null) energy.addEnergy(energy.getCapacity() * .5);
                if (tile instanceof IWarmupControl control) control.setManualWarmup(false);
                if (tile instanceof TileEntityHeatingMachine machine)
                  com.igoryok95.ic2raddons.machines.te.MachineClientFixtures.prepare(machine);
                if (tile instanceof TileEntityBetterItemBuffer buffer) {
                  for (int slot = 0; slot < 9; slot++)
                    buffer.bufferSlot.put(slot, new ItemStack(Ic2Items.IRON_DUST, slot + 1));
                  buffer.upgradeSlot.put(0, new ItemStack(Ic2Items.PULLING_UPGRADE));
                  buffer.upgradeSlot.put(3, new ItemStack(Ic2Items.EJECTOR_UPGRADE));
                }
                if (tile instanceof com.igoryok95.ic2raddons.bridge.bc.ElectricEngine engine) {
                  level.setBlockAndUpdate(
                      pos.below(),
                      net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState());
                  var pipePos = pos.east();
                  level.setBlockAndUpdate(
                      pipePos.below(),
                      net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                  player.setItemInHand(
                      InteractionHand.MAIN_HAND,
                      new ItemStack(buildcraft.transport.BCTransportItems.PIPE_POWER_WOOD.get()));
                  player
                      .getMainHandItem()
                      .useOn(
                          new net.minecraft.world.item.context.UseOnContext(
                              player,
                              InteractionHand.MAIN_HAND,
                              new net.minecraft.world.phys.BlockHitResult(
                                  net.minecraft.world.phys.Vec3.atCenterOf(pipePos.below())
                                      .add(0, .5, 0),
                                  net.minecraft.core.Direction.UP,
                                  pipePos.below(),
                                  false)));
                  var quarry =
                      net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                          net.minecraft.resources.ResourceLocation.parse(
                              "buildcraftbuilders:quarry"));
                  level.setBlockAndUpdate(pos.east(2), quarry.defaultBlockState());
                  ((buildcraft.builders.tile.TileQuarry) level.getBlockEntity(pos.east(2)))
                      .onPlacedBy(player, new ItemStack(quarry));
                }
                placed.add(pos);
              }
              positions = List.copyOf(placed);
            });
      if (positions == null || ticks < 80) return;
      if (ticks == 80)
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
          if (entry.getKey().location().getNamespace().equals("ic2r_addons")
              && !net.minecraft.client.resources.language.I18n.exists(
                  entry.getValue().getDescriptionId()))
            throw new IllegalStateException("Missing live item name: " + entry.getKey());
        }
      int step = ticks - 80;
      int start = Integer.getInteger("ic2r_addons.warmupStart", 0);
      int index = start + step / 120;
      int phase = step % 120;
      if (index >= positions.size()) {
        int view =
            step
                - (positions.size() - start) * 120
                + (Boolean.getBoolean("ic2r_addons.warmupEnglishOnly") ? 840 : 0);
        if (view >= 840) {
          if (englishReload == null) {
            mc.options.languageCode = "en_us";
            mc.getLanguageManager().setSelected("en_us");
            englishReload = mc.reloadResourcePacks();
            return;
          }
          if (!englishReload.isDone() || mc.getOverlay() != null) return;
          int frame = englishTicks++;
          if (frame == 0) mc.setScreen(new PresentationGallery());
          if (frame == 60)
            Screenshot.grab(
                mc.gameDirectory,
                "item-presentation-en_us.png",
                mc.getMainRenderTarget(),
                text -> {});
          if (frame == 100)
            server.execute(
                () -> {
                  var player = server.getPlayerList().getPlayer(id);
                  player.closeContainer();
                  player.getInventory().clearContent();
                  var engine =
                      (com.igoryok95.ic2raddons.bridge.bc.ElectricEngine)
                          player.serverLevel().getBlockEntity(positions.get(12));
                  var saved = engine.saveWithFullMetadata(player.registryAccess());
                  saved.putLong("mjBuffer", 1_250_000_000L);
                  saved.putInt("EngineHeat", 900);
                  engine.loadWithComponents(saved, player.registryAccess());
                  engine.energy.addEnergy(10000);
                  mc.execute(
                      () -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
                  ((IHasGui) engine).openManagedBe(player, InteractionHand.MAIN_HAND);
                });
          if (frame == 145) {
            if (!(mc.screen instanceof Ic2Gui<?>))
              throw new IllegalStateException("Missing English engine GUI");
            Screenshot.grab(
                mc.gameDirectory, "engine-gui-en_us.png", mc.getMainRenderTarget(), text -> {});
          }
          if (frame == 170) mc.player.closeContainer();
          if (frame == 230)
            Screenshot.grab(
                mc.gameDirectory, "engine-jade-en_us.png", mc.getMainRenderTarget(), text -> {});
          if (frame >= 260) {
            System.out.println(
                "WARMUP_CLIENT_PASS: changed menus, synchronized toggles, engine/Vajra hand views, all item names, RU/EN gallery and engine GUI/Jade");
            mc.stop();
          }
          return;
        }
        if (view >= 720) {
          if (view == 720) mc.setScreen(new PresentationGallery());
          if (view == 790)
            Screenshot.grab(
                mc.gameDirectory,
                "item-presentation-ru_ru.png",
                mc.getMainRenderTarget(),
                text -> {});
          return;
        }
        if (view % 120 == 0) {
          int shot = view / 120;
          mc.player.closeContainer();
          mc.options.setCameraType(
              shot % 2 == 0
                  ? net.minecraft.client.CameraType.FIRST_PERSON
                  : net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
          server.execute(
              () -> {
                var player = server.getPlayerList().getPlayer(id);
                var enginePos = positions.get(12);
                player.connection.teleport(
                    enginePos.getX() + .5, enginePos.getY(), enginePos.getZ() - 2.5, 0, 25);
                var item =
                    shot < 2
                        ? com.igoryok95.ic2raddons.bridge.bc.BcContent.ENGINE_ITEM.get()
                        : shot < 4
                            ? buildcraft.energy.BCEnergyBlocks.ENGINE_FE_ITEM.get()
                            : com.igoryok95.ic2raddons.equipment.GraviItems.VAJRA.get();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
                player.inventoryMenu.broadcastChanges();
              });
        }
        if (view % 120 == 70)
          Screenshot.grab(
              mc.gameDirectory,
              "engine-hand-" + (view / 120) + ".png",
              mc.getMainRenderTarget(),
              text -> {});
        return;
      }
      if (phase == 0)
        server.execute(
            () -> {
              var player = server.getPlayerList().getPlayer(id);
              player.closeContainer();
              var pos = positions.get(index);
              player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 12);
              var machine = player.serverLevel().getBlockEntity(pos);
              if (machine instanceof com.igoryok95.ic2raddons.bridge.bc.ElectricEngine engine) {
                engine.energy.addEnergy(10000);
                engine.heat = 900;
              }
              ((IHasGui) machine).openManagedBe(player, InteractionHand.MAIN_HAND);
            });
      if (phase == 25 || phase == 65 || phase == 105) {
        if (mc.screen != null && mc.screen.getTitle().getString().startsWith("container."))
          throw new IllegalStateException("Untranslated menu title " + mc.screen.getTitle());
        if (!(mc.screen instanceof Ic2Gui<?> gui))
          throw new IllegalStateException("Missing warmup menu " + index);
        for (var slot : gui.getMenu().slots)
          if (slot.x < 0
              || slot.y < 0
              || slot.x + 16 > gui.getXSize()
              || slot.y + 16 > gui.getYSize())
            throw new IllegalStateException("Slot outside warmup menu " + index);
        if (index < 11) {
          boolean expected = phase == 65;
          if (((IWarmupControl) gui.getMenu().base).isManualWarmup() != expected)
            throw new IllegalStateException(
                "Client button state not synchronized " + index + ":" + phase);
          server.execute(
              () -> {
                var tile =
                    (IWarmupControl)
                        server
                            .getPlayerList()
                            .getPlayer(id)
                            .serverLevel()
                            .getBlockEntity(positions.get(index));
                if (tile.isManualWarmup() != expected)
                  throw new IllegalStateException("Server button state differs " + index);
              });
        }
        if (phase != 105)
          Screenshot.grab(
              mc.gameDirectory,
              "warmup-" + index + "-" + (phase == 65 ? "on" : "off") + ".png",
              mc.getMainRenderTarget(),
              text -> System.out.println(text.getString()));
      }
      if (index < 11 && (phase == 35 || phase == 85)) {
        var gui = (Ic2Gui<?>) mc.screen;
        gui.mouseClicked(gui.getX() + 162, gui.getY() + 14, 0);
      }
    }

    private static final class PresentationGallery extends net.minecraft.client.gui.screens.Screen {
      private final String[] names = {
        "advanced_diamond_drill",
        "advanced_iridium_drill",
        "advanced_chainsaw",
        "gravitool",
        "vajra",
        "advanced_solar_panel",
        "hybrid_solar_panel",
        "ultimate_solar_panel",
        "quantum_solar_panel",
        "advanced_solar_helmet",
        "hybrid_solar_helmet",
        "ultimate_solar_helmet",
        "quantum_solar_helmet",
        "kit_ic2",
        "card_ic2",
        "rotary_macerator",
        "singularity_compressor",
        "centrifuge_extractor",
        "compacting_recycler",
        "liquescent_extruder",
        "impellerized_roller",
        "water_jet_cutter",
        "thermal_washer",
        "advanced_jetpack",
        "advanced_lappack",
        "ultimate_lappack",
        "advanced_nano",
        "advanced_quant",
        "advanced_nano_leggings",
        "advanced_nano_boots",
        "advanced_quantum_leggings",
        "advanced_quantum_boots"
      };

      PresentationGallery() {
        super(
            net.minecraft.network.chat.Component.translatable("itemGroup.ic2r_addons.technology"));
      }

      public boolean isPauseScreen() {
        return false;
      }

      public void render(
          net.minecraft.client.gui.GuiGraphics graphics,
          int mouseX,
          int mouseY,
          float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF20242B);
        for (int i = 0; i < names.length; i++) {
          int rows = (names.length + 1) / 2;
          int x = 8 + (i / rows) * (width / 2),
              y = 10 + (i % rows) * Math.min(23, (height - 20) / rows);
          var item =
              net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                  com.igoryok95.ic2raddons.solar.IC2RAddons.id(names[i]));
          var stack = new ItemStack(item);
          graphics.renderItem(stack, x, y);
          graphics.pose().pushPose();
          graphics.pose().translate(x + 22, y + 5, 0);
          float scale =
              Math.min(1, (width / 2f - 35) / Math.max(1, font.width(stack.getHoverName())));
          graphics.pose().scale(scale, scale, 1);
          graphics.drawString(
              font,
              stack.getHoverName().copy().withStyle(stack.getRarity().getStyleModifier()),
              0,
              0,
              0xFFFFFFFF);
          graphics.pose().popPose();
        }
      }
    }
  }
}
