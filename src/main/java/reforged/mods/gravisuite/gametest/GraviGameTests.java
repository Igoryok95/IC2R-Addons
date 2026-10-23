package reforged.mods.gravisuite.gametest;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import reforged.mods.gravisuite.*;
import reforged.mods.gravisuite.items.Relocator;
import reforged.mods.gravisuite.network.GraviNetwork;

@GameTestHolder("gravisuite")
@PrefixGameTestTemplate(false)
public final class GraviGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void allEquipmentUsesIc2Energy(GameTestHelper helper) {
    for (var entry : GraviItems.ITEMS.getEntries()) {
      Item item = entry.get();
      if (!(item instanceof IElectricItem electric)) continue;
      ItemStack stack = new ItemStack(item);
      double charged =
          ElectricItem.manager.charge(
              stack, Double.POSITIVE_INFINITY, electric.getTier(stack), true, false);
      helper.assertTrue(
          charged == electric.getMaxCharge(stack), "full capacity for " + entry.getId());
      double simulated =
          ElectricItem.manager.discharge(stack, 100, Integer.MAX_VALUE, true, false, true);
      helper.assertTrue(
          simulated == 100 && ElectricItem.manager.getCharge(stack) == charged,
          "simulation must not drain " + entry.getId());
      helper.assertTrue(
          ElectricItem.manager.charge(
                  new ItemStack(item), 100, electric.getTier(stack) - 1, true, false)
              == 0,
          "lower voltage must not charge " + entry.getId());
    }
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void lappacksImproveIc2rCapacity(GameTestHelper helper) {
    ItemStack base = new ItemStack(Ic2Items.LAPPACK),
        advanced = new ItemStack(GraviItems.ADVANCED_LAPPACK.get()),
        ultimate = new ItemStack(GraviItems.ULTIMATE_LAPPACK.get());
    double original = ((IElectricItem) base.getItem()).getMaxCharge(base);
    helper.assertTrue(
        ((IElectricItem) advanced.getItem()).getMaxCharge(advanced) == original * 2,
        "advanced pack should double IC2R storage");
    helper.assertTrue(
        ((IElectricItem) ultimate.getItem()).getMaxCharge(ultimate) == original * 5,
        "ultimate pack should provide five times IC2R storage");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void mfsuChargesAddonArmor(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MFSU);
    TileEntityElectricBlock storage = helper.getBlockEntity(new BlockPos(1, 1, 1));
    ItemStack pack = new ItemStack(GraviItems.ULTIMATE_LAPPACK.get());
    helper.assertTrue(storage.chargeSlot.accepts(pack), "MFSU must accept the ultimate pack");
    storage.chargeSlot.put(pack);
    storage.energy.addEnergy(100_000);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                ElectricItem.manager.getCharge(pack) > 0,
                "MFSU must actually charge the addon pack"));
  }

  @GameTest(template = EMPTY)
  public static void stateChangesPreserveCharge(GameTestHelper helper) {
    ItemStack stack = charged(GraviItems.VAJRA.get(), 5000);
    ItemState.cycle(stack, "mining", 3);
    ItemState.toggle(stack, "enabled");
    helper.assertTrue(
        ElectricItem.manager.getCharge(stack) == 5000,
        "mode state must preserve IC2's charge data");
    helper.assertTrue(ItemState.mode(stack, "mining", 3) == 1, "mode must persist");
    ItemStack copy = stack.copy();
    ItemState.cycle(copy, "mining", 3);
    helper.assertTrue(
        ItemState.mode(stack, "mining", 3) == 1, "copy updates must not mutate the original");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void upgradeRecipePreservesInputCharge(GameTestHelper helper) {
    var holder =
        helper.getLevel().getRecipeManager().byKey(GraviSuite.id("advanced_lappack")).orElseThrow();
    CraftingRecipe recipe = (CraftingRecipe) holder.value();
    ItemStack original = charged(Ic2Items.LAPPACK, 5000),
        crystal = charged(Ic2Items.LAPOTRON_CRYSTAL, 2000);
    CraftingInput input =
        CraftingInput.of(
            1, 3, List.of(original, new ItemStack(Ic2Items.ADVANCED_CIRCUIT), crystal));
    helper.assertTrue(recipe.matches(input, helper.getLevel()), "upgrade ingredients must match");
    ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
    helper.assertTrue(result.is(GraviItems.ADVANCED_LAPPACK.get()), "upgrade result");
    helper.assertTrue(
        ElectricItem.manager.getCharge(result) == 7000, "upgrade must carry ingredient charge");
    helper.assertTrue(
        ElectricItem.manager.getCharge(original) == 5000,
        "recipe preview must not drain ingredients");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void everyItemHasRecipeAndPreservesIc2Progression(GameTestHelper helper) {
    for (var entry : GraviItems.ITEMS.getEntries())
      helper.assertTrue(
          helper.getLevel().getRecipeManager().byKey(entry.getId()).isPresent(),
          "missing recipe for " + entry.getId());
    ItemStack upgraded = new ItemStack(GraviItems.ADVANCED_IRIDIUM_DRILL.get()),
        original = new ItemStack(Ic2Items.IRIDIUM_DRILL);
    helper.assertTrue(
        ((IElectricItem) upgraded.getItem()).getMaxCharge(upgraded)
            > ((IElectricItem) original.getItem()).getMaxCharge(original),
        "advanced iridium drill must improve capacity");
    helper.assertTrue(
        ((IElectricItem) upgraded.getItem()).getTransferLimit(upgraded)
            > ((IElectricItem) original.getItem()).getTransferLimit(original),
        "advanced iridium drill must improve charging");
    helper.assertTrue(
        helper
            .getLevel()
            .getRecipeManager()
            .byKey(ResourceLocation.parse("ic2:shaped/quantum_chestplate"))
            .isPresent(),
        "IC2R quantum progression must remain available");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void flightRequiresChargeAndCleansUpOnRemoval(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack armor = charged(GraviItems.GRAVI_CHESTPLATE.get(), 5000);
    ItemState.toggle(armor, "engine");
    player.setItemSlot(EquipmentSlot.CHEST, armor);
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
    helper.assertTrue(
        attribute.hasModifier(GraviSuite.id("gravi_flight")),
        "charged engine must grant creative flight");
    player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        !attribute.hasModifier(GraviSuite.id("gravi_flight")),
        "removing armor must remove its flight modifier");
    player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(GraviItems.GRAVI_CHESTPLATE.get()));
    ItemState.toggle(player.getItemBySlot(EquipmentSlot.CHEST), "engine");
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        !attribute.hasModifier(GraviSuite.id("gravi_flight")), "empty armor must not grant flight");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void enginePacketOnlyChangesEquippedAddonArmor(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack unrelated = new ItemStack(Ic2Items.QUANTUM_CHESTPLATE);
    player.setItemSlot(EquipmentSlot.CHEST, unrelated);
    GraviNetwork.apply(player, new GraviNetwork.Action(0, 0, ""));
    helper.assertTrue(
        !ItemState.enabled(unrelated, "engine"), "packet must not modify unrelated armor");
    ItemStack addon = new ItemStack(GraviItems.ADVANCED_JETPACK.get());
    player.setItemSlot(EquipmentSlot.CHEST, addon);
    GraviNetwork.apply(player, new GraviNetwork.Action(0, 0, ""));
    helper.assertTrue(
        ItemState.enabled(addon, "engine"), "packet should toggle the equipped addon engine");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void armorPowersIc2Tools(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack pack = charged(GraviItems.ADVANCED_LAPPACK.get(), 10_000),
        drill = new ItemStack(Ic2Items.DIAMOND_DRILL);
    player.setItemSlot(EquipmentSlot.CHEST, pack);
    player.setItemSlot(EquipmentSlot.MAINHAND, drill);
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        ElectricItem.manager.getCharge(drill) > 0, "addon pack must power an IC2R tool");
    helper.assertTrue(
        ElectricItem.manager.getCharge(pack) + ElectricItem.manager.getCharge(drill) == 10_000,
        "transfer must conserve EU");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void aoeCostsEnergyPerBlockAndAvoidsContainers(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = charged(GraviItems.ADVANCED_DRILL.get(), 5000);
    player.setItemSlot(EquipmentSlot.MAINHAND, tool);
    BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
    player.serverLevel().setBlockAndUpdate(center, Blocks.STONE.defaultBlockState());
    List<BlockPos> targets = Mining.plane(center, Direction.UP);
    for (BlockPos pos : targets)
      player.serverLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
    player.serverLevel().setBlockAndUpdate(targets.get(0), Blocks.CHEST.defaultBlockState());
    Mining.breakTargets(player, tool, center, targets, 160, state -> true);
    helper.assertTrue(
        player.level().getBlockState(targets.get(0)).is(Blocks.CHEST),
        "area mining must not destroy containers");
    helper.assertTrue(
        player.level().getBlockState(center).is(Blocks.STONE),
        "helper must not mine the original block twice");
    helper.assertTrue(
        ElectricItem.manager.getCharge(tool) == 5000 - 7 * 160,
        "seven extra blocks must cost exactly seven operations");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void emptyDrillCannotMineArea(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = new ItemStack(GraviItems.ADVANCED_DRILL.get());
    player.setItemSlot(EquipmentSlot.MAINHAND, tool);
    BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
    player.serverLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
    helper.assertTrue(
        GraviItems.ADVANCED_DRILL.get().onBlockStartBreak(tool, pos, player),
        "unpowered tool must cancel mining");
    helper.assertTrue(
        player.level().getBlockState(pos).is(Blocks.STONE), "unpowered tool must preserve blocks");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void voiderOnlyRemovesFilteredItems(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = charged(GraviItems.VOIDER.get(), 100);
    player.getInventory().setItem(0, tool);
    player.getInventory().setItem(1, new ItemStack(Items.COBBLESTONE, 30));
    player.getInventory().setItem(2, new ItemStack(Items.DIAMOND, 3));
    ItemState.update(tool, tag -> tag.putString("filter", "minecraft:cobblestone"));
    GraviItems.VOIDER.get().purge(tool, player);
    helper.assertValueEqual(
        player.getInventory().countItem(Items.COBBLESTONE),
        10,
        "100 EU should remove twenty matching items");
    helper.assertValueEqual(
        player.getInventory().countItem(Items.DIAMOND), 3, "unmatched items must remain");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void magnetRespectsReservedDrops(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = charged(GraviItems.MAGNET.get(), 100);
    ItemState.toggle(tool, "enabled");
    var entity =
        new ItemEntity(
            player.level(),
            player.getX() + 2,
            player.getY(),
            player.getZ(),
            new ItemStack(Items.DIAMOND));
    entity.setNoPickUpDelay();
    entity.setTarget(java.util.UUID.randomUUID());
    player.level().addFreshEntity(entity);
    GraviItems.MAGNET.get().nearby(tool, player);
    helper.assertTrue(
        entity.getDeltaMovement().lengthSqr() < 0.1 && ElectricItem.manager.getCharge(tool) == 100,
        "reserved drops must not be attracted");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void relocatorPointsAreBoundedAndKeepCharge(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = charged(GraviItems.RELOCATOR.get(), 1234);
    for (int i = 0; i < 10; i++)
      helper.assertTrue(Relocator.addPoint(tool, player, "Test " + i), "point should be accepted");
    helper.assertTrue(
        !Relocator.addPoint(tool, player, "Extra"), "eleventh point must be rejected");
    Relocator.removePoint(tool, -1);
    Relocator.removePoint(tool, 999);
    helper.assertValueEqual(
        Relocator.points(tool).size(), 10, "invalid indices must not modify destinations");
    helper.assertTrue(
        ElectricItem.manager.getCharge(tool) == 1234, "point editing must preserve charge");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void personalTeleportChargesOnlyOnSuccess(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack tool = charged(GraviItems.RELOCATOR.get(), 2_000_000);
    var target = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5));
    player.teleportTo(target.x, target.y, target.z);
    Relocator.addPoint(tool, player, "Home");
    player.teleportTo(target.x + 1, target.y, target.z);
    helper.assertTrue(
        Relocator.personalTeleport(tool, player), "safe destination should be reachable");
    helper.assertTrue(
        player.position().distanceToSqr(target) < 0.01,
        "player should reach the saved destination");
    helper.assertTrue(
        ElectricItem.manager.getCharge(tool) == 1_000_000, "same-dimension teleport cost");
    helper
        .getLevel()
        .setBlockAndUpdate(BlockPos.containing(target), Blocks.STONE.defaultBlockState());
    player.teleportTo(target.x + 1, target.y, target.z);
    helper.assertTrue(
        !Relocator.personalTeleport(tool, player), "obstructed destination must fail");
    helper.assertTrue(
        ElectricItem.manager.getCharge(tool) == 1_000_000, "failed teleport must not consume EU");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void portalExpiresAndPersistsDestination(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    var target =
        new Relocator.Destination(
            helper.getLevel().dimension(),
            helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5)),
            0,
            0,
            "Home");
    BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
    helper.assertTrue(
        PortalContent.createPortal(helper.getLevel(), pos, target, player),
        "portal should be placed in empty space");
    var portal = (PortalContent.PortalBlockEntity) helper.getLevel().getBlockEntity(pos);
    helper.assertTrue(portal.target.name().equals("Home"), "portal should retain its destination");
    portal.expires = helper.getLevel().getGameTime();
    ((PortalContent.PortalBlockEntity) helper.getLevel().getBlockEntity(pos.above())).expires =
        portal.expires;
    helper.runAfterDelay(
        3,
        () -> {
          helper.assertTrue(
              helper.getLevel().isEmptyBlock(pos) && helper.getLevel().isEmptyBlock(pos.above()),
              "temporary portal must expire");
          helper.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void advancedDrillsImproveIc2rMining(GameTestHelper helper) {
    var stone = Blocks.STONE.defaultBlockState();
    ItemStack baseDiamond = charged(Ic2Items.DIAMOND_DRILL, Double.POSITIVE_INFINITY);
    ItemStack advancedDiamond = charged(GraviItems.ADVANCED_DRILL.get(), Double.POSITIVE_INFINITY);
    ItemStack baseIridium = charged(Ic2Items.IRIDIUM_DRILL, Double.POSITIVE_INFINITY);
    ItemStack advancedIridium =
        charged(GraviItems.ADVANCED_IRIDIUM_DRILL.get(), Double.POSITIVE_INFINITY);
    helper.assertTrue(
        advancedDiamond.getDestroySpeed(stone) > baseDiamond.getDestroySpeed(stone),
        "diamond upgrade must mine faster");
    helper.assertTrue(
        advancedIridium.getDestroySpeed(stone) > baseIridium.getDestroySpeed(stone),
        "iridium upgrade must mine faster");
    ItemState.cycle(advancedIridium, "efficiency", 3);
    ItemState.cycle(advancedIridium, "efficiency", 3);
    helper.assertTrue(
        advancedIridium.getDestroySpeed(stone) >= baseIridium.getDestroySpeed(stone),
        "fine iridium mode must retain base speed while saving energy");
    ItemStack vajra = charged(GraviItems.VAJRA.get(), Double.POSITIVE_INFINITY);
    helper.assertTrue(
        vajra.getDestroySpeed(stone) > baseIridium.getDestroySpeed(stone),
        "vajra must exceed the base iridium drill speed");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void advancedIridiumInitializesFortune(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack stack = charged(GraviItems.ADVANCED_IRIDIUM_DRILL.get(), 1500);
    player.setItemSlot(EquipmentSlot.MAINHAND, stack);
    GraviItems.ADVANCED_IRIDIUM_DRILL
        .get()
        .inventoryTick(stack, helper.getLevel(), player, 0, true);
    var enchantments =
        helper
            .getLevel()
            .registryAccess()
            .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
    helper.assertValueEqual(
        net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
            enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),
            stack),
        3,
        "advanced iridium should start with Fortune III");
    helper.assertTrue(
        ElectricItem.manager.getCharge(stack) == 1500,
        "enchantment initialization must preserve charge");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void insufficientBoostEnergyStillDrainsFlightPower(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack chest = charged(GraviItems.GRAVI_CHESTPLATE.get(), 700);
    ItemState.toggle(chest, "engine");
    player.setItemSlot(EquipmentSlot.CHEST, chest);
    player.getAbilities().flying = true;
    ((ic2.core.util.Keyboard) ic2.core.IC2.keyboard)
        .processKeyUpdate(
            player,
            ic2.core.util.Keyboard.Key.toInt(
                java.util.EnumSet.of(ic2.core.util.Keyboard.Key.boost)));
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        ElectricItem.manager.getCharge(chest) < 700,
        "insufficient boost charge must fall back to paid normal flight");
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        !player
            .getAttribute(NeoForgeMod.CREATIVE_FLIGHT)
            .hasModifier(GraviSuite.id("gravi_flight")),
        "flight permission must expire when charge runs out");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void portalRespectsCancelledPlacement(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
    var destination =
        new Relocator.Destination(helper.getLevel().dimension(), player.position(), 0, 0, "Home");
    java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent>
        cancel =
            event -> {
              if (event.getEntity() == player) event.setCanceled(true);
            };
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cancel);
    try {
      helper.assertTrue(
          !PortalContent.createPortal(helper.getLevel(), pos, destination, player),
          "cancelled placement must fail");
      helper.assertTrue(
          helper.getLevel().isEmptyBlock(pos) && helper.getLevel().isEmptyBlock(pos.above()),
          "cancelled portal placement must restore both blocks");
    } finally {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);
    }
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void translocatorProjectileMovesTarget(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(2, 1, 1));
    pig.setNoAi(true);
    var destination =
        new Relocator.Destination(
            helper.getLevel().dimension(),
            helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 2.5)),
            0,
            0,
            "Target");
    var ball =
        new PortalContent.RelocatorBall(PortalContent.RELOCATOR_BALL.get(), helper.getLevel());
    ball.configure(player, destination, false);
    var start = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1.5, 1.5));
    ball.setPos(start);
    ball.setDeltaMovement(1, 0, 0);
    helper.getLevel().addFreshEntity(ball);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                pig.position().distanceToSqr(destination.position()) < 0.5,
                "translocator orb must teleport its hit target"));
  }

  @GameTest(template = EMPTY)
  public static void armorUpgradesRetainIc2TypesAndImproveStorage(GameTestHelper helper) {
    Item[][] pairs = {
      {Ic2Items.NANO_LEGGINGS, GraviItems.ADVANCED_NANO_LEGGINGS.get()},
      {Ic2Items.NANO_BOOTS, GraviItems.ADVANCED_NANO_BOOTS.get()},
      {Ic2Items.QUANTUM_LEGGINGS, GraviItems.ADVANCED_QUANTUM_LEGGINGS.get()},
      {Ic2Items.QUANTUM_BOOTS, GraviItems.ADVANCED_QUANTUM_BOOTS.get()}
    };
    for (Item[] pair : pairs) {
      ItemStack base = new ItemStack(pair[0]), upgrade = new ItemStack(pair[1]);
      IElectricItem original = (IElectricItem) pair[0], advanced = (IElectricItem) pair[1];
      helper.assertTrue(
          pair[0].getClass().isInstance(pair[1]), "upgrade must inherit IC2R abilities");
      helper.assertTrue(
          advanced.getMaxCharge(upgrade) == original.getMaxCharge(base) * 4,
          "four times the storage");
      helper.assertTrue(
          advanced.getTransferLimit(upgrade) > original.getTransferLimit(base), "faster charging");
      helper.assertTrue(
          advanced.getTier(upgrade) == original.getTier(base),
          "voltage tier must remain compatible");
    }
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void advancedBootsAbsorbFallsWithExactEnergy(GameTestHelper helper) {
    ItemStack nano = charged(GraviItems.ADVANCED_NANO_BOOTS.get(), 100_000);
    var nanoItem = GraviItems.ADVANCED_NANO_BOOTS.get();
    helper.assertTrue(
        !((ic2.core.item.armor.ItemArmorNanoSuit) Ic2Items.NANO_BOOTS)
            .absorbFall(charged(Ic2Items.NANO_BOOTS, 100_000), 20),
        "base nano rejects heavy falls");
    helper.assertTrue(nanoItem.absorbFall(nano, 20), "upgraded nano must absorb a heavy fall");
    helper.assertTrue(ElectricItem.manager.getCharge(nano) == 15_000, "nano fall costs 85,000 EU");
    helper.assertTrue(
        !nanoItem.absorbFall(nano, 20), "insufficient energy must not grant free fall protection");
    ServerPlayer player = player(helper);
    ItemStack quantum = charged(GraviItems.ADVANCED_QUANTUM_BOOTS.get(), 200_000);
    player.setItemSlot(EquipmentSlot.FEET, quantum);
    var event = new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player, 20, 1);
    GraviEvents.fall(event);
    helper.assertTrue(
        event.getDamageMultiplier() == 0, "addon boots must integrate with fall events");
    helper.assertTrue(
        ElectricItem.manager.getCharge(quantum) == 100_000, "quantum fall costs half base EU");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nanoSprintConsumesEnergyAndCleansUp(GameTestHelper helper) {
    ServerPlayer player = player(helper);
    ItemStack legs = charged(GraviItems.ADVANCED_NANO_LEGGINGS.get(), 16);
    player.setItemSlot(EquipmentSlot.LEGS, legs);
    player.setSprinting(true);
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    var attribute =
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
    helper.assertTrue(
        attribute.hasModifier(GraviSuite.id("nano_sprint")), "charged nano leggings boost sprint");
    helper.assertTrue(ElectricItem.manager.getCharge(legs) == 8, "sprint must cost 8 EU/t");
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        !attribute.hasModifier(GraviSuite.id("nano_sprint")), "depleted legs cannot grant speed");
    player.setItemSlot(EquipmentSlot.LEGS, charged(GraviItems.ADVANCED_NANO_LEGGINGS.get(), 16));
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
    GraviEvents.tick(new PlayerTickEvent.Post(player));
    helper.assertTrue(
        !attribute.hasModifier(GraviSuite.id("nano_sprint")), "removing legs clears own modifier");
    helper.succeed();
  }

  private static ItemStack charged(Item item, double amount) {
    return ElectricItemManager.getCharged(item, amount);
  }

  private static ServerPlayer player(GameTestHelper helper) {
    var cookie =
        net.minecraft.server.network.CommonListenerCookie.createInitial(
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "gravisuite-test"),
            false);
    ServerPlayer player =
        new ServerPlayer(
            helper.getLevel().getServer(),
            helper.getLevel(),
            cookie.gameProfile(),
            cookie.clientInformation());
    var connection =
        new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
    new io.netty.channel.embedded.EmbeddedChannel(connection);
    helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
    player.setGameMode(GameType.SURVIVAL);
    var pos = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5));
    player.teleportTo(pos.x, pos.y, pos.z);
    return player;
  }
}
