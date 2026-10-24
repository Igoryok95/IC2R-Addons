package com.igoryok95.ic2raddons.solar.gametest;

import com.igoryok95.ic2raddons.solar.*;
import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = IC2RAddons.MODID, value = Dist.CLIENT)
public final class ClientSmoke {
  private static int ticks;
  private static int startup;
  private static volatile BlockPos machine;
  private static volatile boolean ready;
  private static int guiFrames;
  private static boolean guiCaptured;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("ic2r_addons.clientSmoke")) return;
    Minecraft mc = Minecraft.getInstance();
    mc.options.pauseOnLostFocus = false;
    if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
      if (mc.screen != null && startup++ == 120) {
        IC2RAddons.LOGGER.info(
            "IC2R_ADDONS_CLIENT_STARTUP_SCREEN {}", mc.screen.getClass().getName());
        screenshot(mc, "solar-smoke-startup.png");
      }
      return;
    }
    if (ticks++ == 0) {
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                var player = server.getPlayerList().getPlayer(id);
                if (player == null) return;
                var level = player.serverLevel();
                level.setDayTime(6000);
                level.setWeatherParameters(10000, 0, false, false);
                var base = player.blockPosition();
                for (int x = -6; x <= 6; x++)
                  for (int z = -3; z <= 10; z++) {
                    level.setBlockAndUpdate(
                        base.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y <= 5; y++) {
                      BlockPos cell = base.offset(x, y, z);
                      if (level.getBlockEntity(cell)
                          instanceof net.minecraft.world.Container inventory)
                        inventory.clearContent();
                      level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                    }
                  }
                level
                    .getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(
                            base.getX() - 7,
                            base.getY() - 2,
                            base.getZ() - 4,
                            base.getX() + 8,
                            base.getY() + 7,
                            base.getZ() + 12))
                    .forEach(net.minecraft.world.entity.Entity::discard);
                level
                    .getEntitiesOfClass(
                        net.minecraft.world.entity.decoration.ArmorStand.class,
                        new net.minecraft.world.phys.AABB(
                            net.minecraft.world.phys.Vec3.atLowerCornerOf(base.offset(-6, -2, -3)),
                            net.minecraft.world.phys.Vec3.atLowerCornerOf(base.offset(7, 7, 11))))
                    .forEach(net.minecraft.world.entity.Entity::discard);
                var equipment =
                    new Item[][] {
                      {
                        AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
                            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO.get(),
                        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_LEGGINGS.get(),
                            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_BOOTS.get()
                      },
                      {
                        AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
                            com.igoryok95.ic2raddons.equipment.GraviItems.GRAVI_CHESTPLATE.get(),
                        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_LEGGINGS
                                .get(),
                            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_BOOTS
                                .get()
                      }
                    };
                var slots =
                    new net.minecraft.world.entity.EquipmentSlot[] {
                      net.minecraft.world.entity.EquipmentSlot.HEAD,
                          net.minecraft.world.entity.EquipmentSlot.CHEST,
                      net.minecraft.world.entity.EquipmentSlot.LEGS,
                          net.minecraft.world.entity.EquipmentSlot.FEET
                    };
                for (int i = 0; i < equipment.length; i++) {
                  var stand =
                      new net.minecraft.world.entity.decoration.ArmorStand(
                          level,
                          base.getX() + (i == 0 ? -1.5 : 2.5),
                          base.getY(),
                          base.getZ() + 3.5);
                  stand.setNoGravity(true);
                  stand.setYRot(180);
                  for (int j = 0; j < slots.length; j++)
                    stand.setItemSlot(
                        slots[j],
                        ic2.core.item.ElectricItemManager.getCharged(equipment[i][j], 1_234_567));
                  level.addFreshEntity(stand);
                }
                machine = base.offset(0, 0, 4);
                level.setBlockAndUpdate(
                    machine, AdvSolarBlocks.MOLECULAR_TRANSFORMER.get().defaultBlockState());
                if (level.getBlockEntity(machine) instanceof TileEntityMolecularTransformer te) {
                  te.inputSlot.put(new ItemStack(Items.WITHER_SKELETON_SKULL));
                  te.getComponent(Energy.class).addEnergy(100000);
                }
                level.setBlockAndUpdate(
                    base.offset(-1, 0, 4),
                    AdvSolarBlocks.PHOTONIC_SOLAR_PANEL.get().defaultBlockState());
                level.setBlockAndUpdate(
                    base.offset(2, 0, 4),
                    AdvSolarBlocks.HYBRID_SOLAR_PANEL.get().defaultBlockState());
                player.teleportTo(base.getX() + 0.5, base.getY() + 0.2, base.getZ() + 0.5);
                player.setYRot(0);
                player.setXRot(10);
                player.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new ItemStack(AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get()));
                ready = true;
              });
    }
    if (!ready) return;
    if (ticks > 148 && ticks < 200 && mc.screen instanceof ic2.core.gui.dynamic.DynamicGui) {
      if (++guiFrames == 3) {
        screenshot(mc, "solar-smoke-transformer-gui.png");
        guiCaptured = true;
      }
    }
    if (ticks == 100) mc.setScreen(new ItemsScreen());
    if (ticks == 125) screenshot(mc, "solar-smoke-items.png");
    if (ticks == 150) {
      mc.setScreen(null);
      var id = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                if (player != null
                    && player.serverLevel().getBlockEntity(machine) instanceof IHasGui gui) {
                  boolean opened =
                      gui.openManagedBe(player, net.minecraft.world.InteractionHand.MAIN_HAND);
                  IC2RAddons.LOGGER.info(
                      "IC2R_ADDONS_CLIENT_OPEN_MENU opened={} valid={} menu={} pos={} machine={}",
                      opened,
                      gui.stillValid(player),
                      player.containerMenu.getClass().getName(),
                      player.position(),
                      machine);
                } else
                  IC2RAddons.LOGGER.error(
                      "IC2R_ADDONS_CLIENT_OPEN_MENU missing player or machine: {}", machine);
              });
    }
    if (ticks == 180) {
      IC2RAddons.LOGGER.info(
          "IC2R_ADDONS_CLIENT_SMOKE_SCREEN {}",
          mc.screen == null ? "null" : mc.screen.getClass().getName());
      if (!guiCaptured)
        IC2RAddons.LOGGER.warn(
            "IC2R_ADDONS_CLIENT_GUI_NOT_CAPTURED menu={}",
            mc.player.containerMenu.getClass().getName());
    }
    if (ticks >= 200 && ticks < 240 && mc.screen != null) mc.setScreen(null);
    if (ticks == 230) screenshot(mc, "solar-smoke-world.png");
    if (ticks == 260) com.igoryok95.ic2raddons.solar.compat.JeiSearchSmoke.show("@ic2r");
    if (ticks == 280) screenshot(mc, "solar-smoke-jei-solar.png");
    if (ticks == 290) com.igoryok95.ic2raddons.solar.compat.JeiSearchSmoke.show("@addons");
    if (ticks == 310) screenshot(mc, "solar-smoke-jei-gravi.png");
    boolean bc = net.neoforged.fml.ModList.get().isLoaded("buildcraftenergy");
    if (bc) com.igoryok95.ic2raddons.bridge.client.EngineClientSmoke.tick(mc, ticks, machine);
    if (ticks == (bc ? 440 : 330)) {
      IC2RAddons.LOGGER.info("IC2R_ADDONS_CLIENT_SMOKE_COMPLETE");
      mc.stop();
    }
  }

  private static void screenshot(Minecraft mc, String name) {
    Screenshot.grab(
        mc.gameDirectory,
        name,
        mc.getMainRenderTarget(),
        message -> IC2RAddons.LOGGER.info("IC2R_ADDONS_CLIENT_SMOKE_IMAGE {}", name));
  }

  private static final class ItemsScreen extends Screen {
    private ItemsScreen() {
      super(Component.translatable("itemGroup.ic2r_addons.technology"));
    }

    public boolean isPauseScreen() {
      return false;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
      g.fill(0, 0, width, height, 0xff262b35);
      g.drawCenteredString(font, title, width / 2, 8, 0xffffffff);
      Item[] equipment = {
        AdvSolarBlocks.ADVANCED_SOLAR_HELMET.get(),
            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_LEGGINGS.get(),
            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_NANO_BOOTS.get(),
        AdvSolarBlocks.HYBRID_SOLAR_HELMET.get(), AdvSolarBlocks.ULTIMATE_SOLAR_HELMET.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.GRAVI_CHESTPLATE.get(),
            com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_LEGGINGS.get(),
        com.igoryok95.ic2raddons.equipment.GraviItems.ADVANCED_QUANTUM_BOOTS.get()
      };
      int i = 0;
      for (Item item : equipment) {
        var stack = new ItemStack(item);
        int cell = width / 3;
        int x = (i % 3) * cell, y = 32 + (i / 3) * 58;
        g.renderItem(stack, x + cell / 2 - 8, y);
        g.drawCenteredString(
            font,
            font.plainSubstrByWidth(stack.getHoverName().getString(), cell - 4),
            x + cell / 2,
            y + 20,
            0xffeeeeee);
        double charge = ((ic2.api.item.IElectricItem) item).getMaxCharge(stack);
        g.drawCenteredString(
            font,
            Component.translatable(
                "tooltip.ic2r_addons.capacity", ic2.core.util.Util.toSiString(charge, 2)),
            x + cell / 2,
            y + 32,
            0xff4edffd);
        i++;
      }
    }
  }
}
