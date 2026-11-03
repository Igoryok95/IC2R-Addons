package com.igoryok95.ic2raddons.solar.client;

import com.igoryok95.ic2raddons.solar.AdvSolarBlocks;
import com.igoryok95.ic2raddons.solar.te.TileEntityMolecularTransformer;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.LoggerFactory;

/** One combined real-client pass for ASP GUI frames, live Jade tiers and original item sprites. */
@EventBusSubscriber(modid = "ic2r_addons", value = Dist.CLIENT)
public final class SolarPresentationClientSmoke {
  private static final BlockPos ORIGIN = new BlockPos(200, 128, 0);
  private static int ticks,
      index = Integer.getInteger("ic2r_addons.solarPresentationStart", 0),
      locale;
  private static CompletableFuture<Void> pending;
  private static List<Block> blocks;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
    if (!Boolean.getBoolean("ic2r_addons.solarPresentationSmoke")) return;
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    if (mc.player == null
        || mc.level == null
        || server == null
        || mc.getOverlay() != null
        || pending != null && !pending.isDone()) return;
    if (pending != null) pending.join();
    mc.options.pauseOnLostFocus = false;
    mc.options.hideGui = false;
    var id = mc.player.getUUID();
    if (blocks == null) {
      blocks =
          List.of(
              AdvSolarBlocks.ADVANCED_SOLAR_PANEL.get(),
              AdvSolarBlocks.HYBRID_SOLAR_PANEL.get(),
              AdvSolarBlocks.ULTIMATE_SOLAR_PANEL.get(),
              AdvSolarBlocks.QUANTUM_SOLAR_PANEL.get(),
              AdvSolarBlocks.MOLECULAR_TRANSFORMER.get(),
              AdvSolarBlocks.QUANTUM_GENERATOR.get());
      mc.options.guiScale().set(2);
      mc.resizeDisplay();
      pending =
          CompletableFuture.runAsync(
              () -> {
                var player = server.getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                player.setGameMode(GameType.CREATIVE);
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.getInventory().clearContent();
                level.setDayTime(6000);
                for (var pos : BlockPos.betweenClosed(197, 127, -4, 220, 132, 3))
                  level.setBlockAndUpdate(
                      pos,
                      pos.getY() == 127
                          ? Blocks.STONE.defaultBlockState()
                          : Blocks.AIR.defaultBlockState());
                for (int i = 0; i < blocks.size(); i++) {
                  var pos = ORIGIN.east(i * 3);
                  level.setBlockAndUpdate(
                      pos,
                      blocks
                          .get(i)
                          .defaultBlockState()
                          .setValue(Ic2TileEntityBlock.horizontalFacingProperty, Direction.NORTH));
                }
                var transformer =
                    (TileEntityMolecularTransformer) level.getBlockEntity(ORIGIN.east(12));
                transformer.inputSlot.put(new ItemStack(Items.GLOWSTONE_DUST, 2));
                transformer.getComponent(Energy.class).addEnergy(10000);
              },
              server);
      if (Boolean.getBoolean("ic2r_addons.solarPresentationEnglishOnly")) locale = 1;
      language(mc, locale == 1 ? "en_us" : "ru_ru");
      return;
    }
    int phase = ++ticks;
    if (index < blocks.size()) {
      var pos = ORIGIN.east(index * 3);
      if (phase == 5)
        pending =
            CompletableFuture.runAsync(
                () -> {
                  var player = server.getPlayerList().getPlayer(id);
                  aim(player, pos);
                  ((IHasGui) player.serverLevel().getBlockEntity(pos))
                      .openManagedBe(player, InteractionHand.MAIN_HAND);
                },
                server);
      if (phase == 12) {
        require(
            mc.screen instanceof SolarMachineScreen,
            "ASP screen kept the gray default background: " + index);
        var screen = (SolarMachineScreen) mc.screen;
        require(
            screen.getMenu() == mc.player.containerMenu,
            "replacement screen lost the actual player container: " + index);
        verifyItemTooltip(mc);
        if (index == 0 || index == 4)
          capture(mc, "solar-frame-" + index + "-" + mc.options.languageCode);
      }
      if (phase == 15) mc.player.closeContainer();
      if (phase >= 16) {
        double x = pos.getX() + .5, y = pos.getY() - .3, z = pos.getZ() - 3;
        var delta = Vec3.atCenterOf(pos).subtract(new Vec3(x, y + mc.player.getEyeHeight(), z));
        float pitch =
            (float)
                -Math.toDegrees(
                    Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
        mc.player.setDeltaMovement(Vec3.ZERO);
        mc.player.moveTo(x, y, z, 0, pitch);
      }
      if (phase == 35) {
        if (index < 5) {
          require(
              mc.hitResult instanceof BlockHitResult hit && hit.getBlockPos().equals(pos),
              "Jade camera missed the target");
          var center = Class.forName("snownee.jade.impl.ObjectDataCenter");
          var data =
              ((CompoundTag) center.getMethod("getServerData").invoke(null))
                  .getCompound("ic2r_addons_electrical");
          int[] tiers = {1, 2, 3, 5, 5};
          require(
              data.contains("tier") && data.getInt("tier") == tiers[index],
              "wrong/missing real Jade energy tier: " + data);
          require(
              data.getDouble(index == 4 ? "inputPacket" : "packet")
                  == ic2.api.energy.EnergyNet.instance.getPowerFromTier(tiers[index]),
              "wrong Jade packet limit");
          if (index == 4)
            require(
                data.contains("progress") && data.contains("result"),
                "existing recipe telemetry was lost");
          if (index == 0 || index == 3 || index == 4)
            capture(mc, "solar-jade-" + index + "-" + mc.options.languageCode);
        }
        index++;
        ticks = 0;
      }
      return;
    }
    if (locale == 0) {
      locale = 1;
      index = ticks = 0;
      language(mc, "en_us");
      return;
    }
    if (phase == 5) mc.setScreen(new MaterialsScreen());
    if (phase == 10) capture(mc, "solar-original-materials-16px");
    if (phase == 15) {
      LoggerFactory.getLogger("IC2R-Solar-Presentation")
          .info(
              "IC2R_SOLAR_PRESENTATION_PASS: six original GUI frames and five real Jade tiers in RU/EN; native transformer tooltip; seven original 16px sprites");
      mc.stop();
    }
  }

  private static void verifyItemTooltip(Minecraft mc) {
    var item = AdvSolarBlocks.ITEM_MOLECULAR_TRANSFORMER.get();
    for (var flag : List.of(TooltipFlag.NORMAL, TooltipFlag.ADVANCED)) {
      var lines = new ArrayList<Component>();
      item.appendHoverText(new ItemStack(item), Item.TooltipContext.of(mc.level), lines, flag);
      require(
          lines.stream()
              .anyMatch(
                  line ->
                      line.getContents() instanceof TranslatableContents t
                          && t.getKey().equals("ic2.item.tooltip.power_tier")
                          && t.getArgs()[0].equals(5)),
          "tooltip lacks native tier five");
      require(
          lines.stream().anyMatch(line -> line.getString().contains("8 192")),
          "tooltip lacks localized packet limit");
      for (var line : lines)
        require(
            line.getStyle().getColor() != null
                && line.getStyle().getColor().getValue() == ChatFormatting.GRAY.getColor(),
            "machine tooltip is not native gray");
    }
  }

  private static void aim(net.minecraft.server.level.ServerPlayer player, BlockPos target) {
    double x = target.getX() + .5, y = target.getY() - .3, z = target.getZ() - 3;
    var delta = Vec3.atCenterOf(target).subtract(new Vec3(x, y + player.getEyeHeight(), z));
    float pitch =
        (float)
            -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
    player.connection.teleport(x, y, z, 0, pitch);
    player.setDeltaMovement(Vec3.ZERO);
  }

  private static void language(Minecraft mc, String code) {
    mc.options.languageCode = code;
    mc.getLanguageManager().setSelected(code);
    var previous = pending;
    pending =
        previous == null
            ? mc.reloadResourcePacks()
            : previous.thenCompose(
                ignored -> mc.submit(mc::reloadResourcePacks).thenCompose(value -> value));
  }

  private static void capture(Minecraft mc, String name) {
    Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), ignored -> {});
  }

  private static void require(boolean valid, String message) {
    if (!valid) throw new IllegalStateException(message);
  }

  private static final class MaterialsScreen extends Screen {
    private final List<ItemStack> stacks = new ArrayList<>();

    private MaterialsScreen() {
      super(Component.empty());
      stacks.add(new ItemStack(Ic2Items.TIN_INGOT));
      stacks.add(new ItemStack(Ic2Items.URANIUM_INGOT));
      for (var name :
          List.of(
              "iridium_ingot",
              "uranium_ingot",
              "sunnarium",
              "sunnarium_part",
              "sunnarium_alloy",
              "enriched_sunnarium",
              "enriched_sunnarium_alloy")) {
        var key = ResourceLocation.fromNamespaceAndPath("ic2r_addons", name);
        if (BuiltInRegistries.ITEM.containsKey(key))
          stacks.add(new ItemStack(BuiltInRegistries.ITEM.get(key)));
      }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(0, 0, width, height, 0xFF20252A);
      for (int i = 0; i < stacks.size(); i++) {
        var stack = stacks.get(i);
        var sprite =
            minecraft
                .getItemRenderer()
                .getModel(stack, minecraft.level, minecraft.player, 0)
                .getParticleIcon()
                .contents();
        require(
            sprite.width() == 16 && sprite.height() == 16,
            "material is not a native 16px sprite: " + stack);
        int x = 30 + i % 3 * 200, y = 30 + i / 3 * 125;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(3, 3, 1);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
        graphics.drawString(font, stack.getHoverName(), x, y + 58, 0xFFFFFFFF, false);
      }
    }
  }

  private SolarPresentationClientSmoke() {}
}
