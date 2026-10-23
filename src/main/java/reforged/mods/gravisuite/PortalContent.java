package reforged.mods.gravisuite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reforged.mods.gravisuite.items.Relocator;

public final class PortalContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GraviSuite.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GraviSuite.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GraviSuite.ID);
    public static final net.neoforged.neoforge.registries.DeferredBlock<PortalBlock> PORTAL = BLOCKS.register("relocator_portal", PortalBlock::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalBlockEntity>> PORTAL_BE = BLOCK_ENTITIES.register("relocator_portal", () -> BlockEntityType.Builder.of(PortalBlockEntity::new, PORTAL.get()).build(null));
    public static final DeferredHolder<EntityType<?>, EntityType<RelocatorBall>> RELOCATOR_BALL = ENTITIES.register("relocator_ball", () -> EntityType.Builder.<RelocatorBall>of(RelocatorBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1).build("relocator_ball"));
    private PortalContent() {}

    public static final class PortalBlock extends Block implements EntityBlock {
        public PortalBlock() { super(Properties.of().noCollission().noOcclusion().strength(-1, 3_600_000).lightLevel(state -> 11).noLootTable()); }
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PortalBlockEntity(pos, state); }
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            if (level.isClientSide || type != PORTAL_BE.get()) return null;
            return (world, pos, blockState, blockEntity) -> {
                if (blockEntity instanceof PortalBlockEntity portal && world.getGameTime() >= portal.expires) world.removeBlock(pos, false);
            };
        }
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            if (level.isClientSide || entity.getPersistentData().getLong("gravisuite_portal_cooldown") > System.currentTimeMillis()) return;
            if (level.getBlockEntity(pos) instanceof PortalBlockEntity portal) Relocator.teleport(entity, portal.target);
        }
    }

    public static final class PortalBlockEntity extends BlockEntity {
        public Relocator.Destination target;
        public long expires;
        public PortalBlockEntity(BlockPos pos, BlockState state) { super(PORTAL_BE.get(), pos, state); }
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            if (target != null) tag.put("target", target.save());
            tag.putLong("expires", expires);
        }
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries); target = Relocator.Destination.load(tag.getCompound("target")); expires = tag.getLong("expires");
        }
    }

    public static boolean createPortal(ServerLevel level, BlockPos pos, Relocator.Destination destination, Entity owner) {
        if (destination == null || !level.getWorldBorder().isWithinBounds(pos) || level.isOutsideBuildHeight(pos.above()) || !level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) return false;
        if (owner instanceof Player player && (!player.mayUseItemAt(pos, net.minecraft.core.Direction.UP, player.getMainHandItem()) || !level.mayInteract(player, pos))) return false;
        for (int i = 0; i < 2; i++) {
            BlockPos cell = pos.above(i);
            level.setBlockAndUpdate(cell, PORTAL.get().defaultBlockState());
            if (level.getBlockEntity(cell) instanceof PortalBlockEntity portal) {
                portal.target = destination; portal.expires = level.getGameTime() + GraviConfig.PORTAL_LIFETIME.get(); portal.setChanged();
            }
        }
        return true;
    }

    public static final class RelocatorBall extends ThrowableItemProjectile {
        private Relocator.Destination destination;
        private boolean portal;
        public RelocatorBall(EntityType<? extends RelocatorBall> type, Level level) { super(type, level); }
        protected Item getDefaultItem() { return GraviItems.RELOCATOR.get(); }
        public void configure(Player owner, Relocator.Destination destination, boolean portal) {
            setOwner(owner); this.destination = destination; this.portal = portal;
            setPos(owner.getEyePosition().subtract(0, 0.1, 0));
            shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, 1.8F, 0);
        }
        public void tick() { super.tick(); if (!level().isClientSide && tickCount > 200) discard(); }
        protected void onHit(HitResult hit) {
            super.onHit(hit);
            if (!(level() instanceof ServerLevel level)) return;
            if (portal && hit instanceof BlockHitResult blockHit) createPortal(level, blockHit.getBlockPos().relative(blockHit.getDirection()), destination, getOwner());
            else if (!portal && hit instanceof EntityHitResult entityHit) Relocator.teleport(entityHit.getEntity(), destination);
            discard();
        }
        public void addAdditionalSaveData(CompoundTag tag) {
            super.addAdditionalSaveData(tag);
            if (destination != null) tag.put("destination", destination.save());
            tag.putBoolean("portal", portal);
        }
        public void readAdditionalSaveData(CompoundTag tag) {
            super.readAdditionalSaveData(tag); destination = Relocator.Destination.load(tag.getCompound("destination")); portal = tag.getBoolean("portal");
        }
    }
}
