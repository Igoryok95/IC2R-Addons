package com.igoryok95.ic2raddons.equipment.items;

import com.igoryok95.ic2raddons.equipment.ItemState;
import com.igoryok95.ic2raddons.equipment.PortalContent;
import com.igoryok95.ic2raddons.equipment.network.RelocatorMenu;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Relocator extends ElectricUtility {
  public static final int MAX_POINTS = 10;

  public Relocator() {
    super(10_000_000, 20_000, 4);
  }

  public record Destination(
      ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch, String name) {
    public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("dimension", dimension.location().toString());
      tag.putDouble("x", position.x);
      tag.putDouble("y", position.y);
      tag.putDouble("z", position.z);
      tag.putFloat("yaw", yaw);
      tag.putFloat("pitch", pitch);
      tag.putString("name", name);
      return tag;
    }

    public static Destination load(CompoundTag tag) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString("dimension"));
      Vec3 pos = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
      if (id == null
          || !Double.isFinite(pos.x)
          || !Double.isFinite(pos.y)
          || !Double.isFinite(pos.z)
          || Math.abs(pos.x) > 29_999_984
          || Math.abs(pos.z) > 29_999_984) return null;
      float yaw = tag.getFloat("yaw"), pitch = tag.getFloat("pitch");
      if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) return null;
      return new Destination(
          ResourceKey.create(Registries.DIMENSION, id), pos, yaw, pitch, tag.getString("name"));
    }

    public Component title(int index) {
      return name.isBlank()
          ? Component.translatable("screen.ic2r_addons.point", index + 1)
          : Component.literal(name);
    }
  }

  public static List<Destination> points(ItemStack stack) {
    List<Destination> points = new ArrayList<>();
    ListTag tags = ItemState.read(stack).getList("points", 10);
    for (int i = 0; i < Math.min(MAX_POINTS, tags.size()); i++) {
      Destination point = Destination.load(tags.getCompound(i));
      if (point != null) points.add(point);
    }
    return points;
  }

  public static Destination selected(ItemStack stack) {
    List<Destination> list = points(stack);
    return list.isEmpty() ? null : list.get(ItemState.mode(stack, "selected", list.size()));
  }

  public static boolean addPoint(ItemStack stack, ServerPlayer player, String name) {
    List<Destination> list = points(stack);
    if (list.size() >= MAX_POINTS) return false;
    String clean =
        name.codePoints()
            .filter(c -> !Character.isISOControl(c) && c != '§')
            .limit(48)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    list.add(
        new Destination(
            player.level().dimension(),
            player.position(),
            player.getYRot(),
            player.getXRot(),
            clean));
    savePoints(stack, list);
    ItemState.update(stack, tag -> tag.putInt("selected", list.size() - 1));
    return true;
  }

  public static void removePoint(ItemStack stack, int index) {
    List<Destination> list = points(stack);
    if (index < 0 || index >= list.size()) return;
    list.remove(index);
    savePoints(stack, list);
    ItemState.update(
        stack, tag -> tag.putInt("selected", Math.max(0, Math.min(index, list.size() - 1))));
  }

  private static void savePoints(ItemStack stack, List<Destination> list) {
    ListTag tags = new ListTag();
    list.forEach(point -> tags.add(point.save()));
    ItemState.update(stack, tag -> tag.put("points", tags));
  }

  public static boolean teleport(Entity entity, Destination destination) {
    if (destination == null
        || entity.getServer() == null
        || entity.isPassenger()
        || entity.isVehicle()) return false;
    var target = entity.getServer().getLevel(destination.dimension);
    BlockPos block = BlockPos.containing(destination.position);
    if (target == null
        || target.isOutsideBuildHeight(block)
        || !target.getWorldBorder().isWithinBounds(block)) return false;
    target.getChunkAt(block);
    var bounds = entity.getBoundingBox().move(destination.position.subtract(entity.position()));
    if (!target.noCollision(entity, bounds)) return false;
    long previousCooldown = entity.getPersistentData().getLong("gravisuite_portal_cooldown");
    float previousFallDistance = entity.fallDistance;
    entity
        .getPersistentData()
        .putLong("gravisuite_portal_cooldown", System.currentTimeMillis() + 2500);
    entity.resetFallDistance();
    boolean success =
        entity.teleportTo(
            target,
            destination.position.x,
            destination.position.y,
            destination.position.z,
            Set.of(),
            destination.yaw,
            destination.pitch);
    if (!success) {
      entity.fallDistance = previousFallDistance;
      entity.getPersistentData().putLong("gravisuite_portal_cooldown", previousCooldown);
    }
    return success;
  }

  public static boolean personalTeleport(ItemStack stack, ServerPlayer player) {
    Destination point = selected(stack);
    if (point == null) return false;
    int cost = point.dimension.equals(player.level().dimension()) ? 1_000_000 : 1_500_000;
    if (!ElectricItem.manager.canUse(stack, cost) || !teleport(player, point)) return false;
    ElectricItem.manager.use(stack, cost, null);
    return true;
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide && player instanceof ServerPlayer server) {
      if (IC2.keyboard.isModeSwitchKeyDown(player)) ItemState.cycle(stack, "relocator", 3);
      else if (player.isShiftKeyDown() || ItemState.mode(stack, "relocator", 3) == 0) {
        server.openMenu(
            new SimpleMenuProvider(
                (id, inventory, ignored) -> new RelocatorMenu(id, inventory, hand),
                Component.translatable("screen.ic2r_addons.relocator")),
            buffer -> buffer.writeEnum(hand));
      } else {
        Destination destination = selected(stack);
        int mode = ItemState.mode(stack, "relocator", 3);
        int cost = mode == 1 ? 2_000_000 : 2_500_000;
        if (destination != null && ElectricItem.manager.canUse(stack, cost)) {
          PortalContent.RelocatorBall ball =
              new PortalContent.RelocatorBall(PortalContent.RELOCATOR_BALL.get(), level);
          ball.configure(server, destination, mode == 2);
          if (level.addFreshEntity(ball)) ElectricItem.manager.use(stack, cost, null);
        } else
          player.displayClientMessage(
              Component.translatable("message.ic2r_addons.destination_or_energy"), true);
      }
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
  }

  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    if (context.getPlayer() == null) return InteractionResult.PASS;
    return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
  }

  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    String[] modes = {"personal", "translocator", "portal"};
    tooltip.add(
        Component.translatable(
            "tooltip.ic2r_addons.tool",
            Component.translatable(
                "mode.ic2r_addons." + modes[ItemState.mode(stack, "relocator", 3)])));
    Destination point = selected(stack);
    tooltip.add(
        Component.translatable(
            "tooltip.ic2r_addons.destination",
            point == null
                ? Component.translatable("ic2r_addons.none")
                : point.title(
                    ItemState.mode(stack, "selected", Math.max(1, points(stack).size())))));
    tooltip.add(Component.translatable("tooltip.ic2r_addons.relocator_keys"));
  }
}
