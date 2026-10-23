package reforged.mods.gravisuite.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import reforged.mods.gravisuite.GraviSuite;
import reforged.mods.gravisuite.ItemState;
import reforged.mods.gravisuite.items.GraviArmor;
import reforged.mods.gravisuite.items.Relocator;
import reforged.mods.gravisuite.items.UtilityItem;

public final class GraviNetwork {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GraviSuite.ID);
    public static final DeferredHolder<MenuType<?>, MenuType<RelocatorMenu>> RELOCATOR_MENU = MENUS.register("relocator", () -> IMenuTypeExtension.create((id, inventory, data) -> new RelocatorMenu(id, inventory, data.readEnum(net.minecraft.world.InteractionHand.class))));
    private GraviNetwork() {}
    public static void register(RegisterPayloadHandlersEvent event) { event.registrar("1").playToServer(Action.TYPE, Action.CODEC, GraviNetwork::handle); }
    public record Action(int action, int index, String text) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(GraviSuite.id("action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.action); buffer.writeVarInt(packet.index); buffer.writeUtf(packet.text, 48); },
                buffer -> new Action(buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(48)));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    private static void handle(Action packet, IPayloadContext context) { context.enqueueWork(() -> {
        if (context.player() instanceof ServerPlayer player) apply(player, packet);
    }); }
    public static void apply(ServerPlayer player, Action packet) {
        if (!player.isAlive() || player.isSpectator()) return;
        if (packet.action == 0) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof GraviArmor armor && (armor.kind == GraviArmor.Kind.GRAVI || armor.kind == GraviArmor.Kind.JETPACK || armor.kind == GraviArmor.Kind.NANO)) {
                boolean enabled = ItemState.toggle(chest, "engine");
                player.displayClientMessage(Component.translatable("tooltip.gravisuite.engine", Component.translatable(enabled ? "gravisuite.on" : "gravisuite.off")), true);
            }
        } else if (packet.action == 1) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item.getItem() instanceof UtilityItem utility && !utility.voider) { ItemState.toggle(item, "enabled"); break; }
            }
        } else if (player.containerMenu instanceof RelocatorMenu menu && menu.stillValid(player)) {
            ItemStack stack = player.getItemInHand(menu.hand);
            int count = Relocator.points(stack).size();
            switch (packet.action) {
                case 10 -> {
                    if (!Relocator.addPoint(stack, player, packet.text)) player.displayClientMessage(Component.translatable("message.gravisuite.point_limit"), true);
                }
                case 11 -> Relocator.removePoint(stack, packet.index);
                case 12 -> {
                    if (packet.index >= 0 && packet.index < count) ItemState.update(stack, tag -> tag.putInt("selected", packet.index));
                }
                case 13 -> {
                    if (Relocator.personalTeleport(stack, player)) player.closeContainer();
                    else player.displayClientMessage(Component.translatable("message.gravisuite.destination_or_energy"), true);
                }
                case 14 -> ItemState.cycle(stack, "relocator", 3);
                default -> { }
            }
        }
        player.inventoryMenu.broadcastChanges();
    }
}
