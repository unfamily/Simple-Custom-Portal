package net.unfamily.adv_cm_portal.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.unfamily.adv_cm_portal.block.ModBlocks;
import net.unfamily.adv_cm_portal.block.PortalBlock;
import net.unfamily.adv_cm_portal.block.entity.PortalBlockEntity;

/**
 * Teleports player to destLevel. If a portal that returns to originDimension already exists there,
 * teleport to it without building. Otherwise build capsule (interior 3x3x4) and place same portal
 * block with returnTo = originDimension.
 */
public final class SafeZoneHelper {

    private static final int INNER_RAD = 1;
    private static final int INTERIOR_HEIGHT = 4;
    private static final int OUTER_RAD = INNER_RAD + 1;
    private static final int TOTAL_HEIGHT = 1 + INTERIOR_HEIGHT + 1;
    /** Search radius around spawn for existing return portal. */
    private static final int SEARCH_RAD = 32;
    /** Vanilla tag: blocks that must not be replaced by features. */
    private static final TagKey<Block> FEATURES_CANNOT_REPLACE = TagKey.create(
        net.minecraft.core.registries.Registries.BLOCK,
        ResourceLocation.parse("minecraft:features_cannot_replace")
    );
    /** Explosion resistance above this is treated as indestructible (e.g. bedrock). */
    private static final float INDESTRUCTIBLE_RESISTANCE = 3_600_000f;

    private SafeZoneHelper() {}

    /**
     * @param originDimension dimension the player is coming from (for return portal and search).
     * @param originPortalPos position of the portal block in the origin dimension; same x,y,z are used in destLevel.
     */
    public static void buildAndTeleport(MinecraftServer server, ServerPlayer player,
                                       PortalDefinition definition, ServerLevel destLevel,
                                       String originDimension, BlockPos originPortalPos) {
        int x = originPortalPos.getX();
        int y = originPortalPos.getY();
        int z = originPortalPos.getZ();
        BlockPos center = new BlockPos(x, y, z);

        BlockPos existing = findReturnPortal(destLevel, center, originDimension);
        if (existing != null) {
            player.teleportTo(destLevel, existing.getX() + 0.5, existing.getY() + 1, existing.getZ() + 0.5, player.getYRot(), player.getXRot());
            return;
        }

        if (!isZoneSafeForPortal(destLevel, x, y, z)) {
            player.displayClientMessage(Component.translatable("message.simp_cm_portal.invalid_portal_position"), true);
            return;
        }

        Block baseBlock = resolveBlock(definition.baseBlock());
        Block capsuleBlock = resolveBlock(definition.capsuleBlock());

        for (int dx = -OUTER_RAD; dx <= OUTER_RAD; dx++) {
            for (int dz = -OUTER_RAD; dz <= OUTER_RAD; dz++) {
                for (int dy = 0; dy < TOTAL_HEIGHT; dy++) {
                    destLevel.setBlock(new BlockPos(x + dx, y + dy, z + dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        if (baseBlock != null) {
            for (int dx = -OUTER_RAD; dx <= OUTER_RAD; dx++) {
                for (int dz = -OUTER_RAD; dz <= OUTER_RAD; dz++) {
                    destLevel.setBlock(new BlockPos(x + dx, y, z + dz), baseBlock.defaultBlockState(), 3);
                }
            }
        }

        if (capsuleBlock != null) {
            for (int dy = 1; dy < TOTAL_HEIGHT; dy++) {
                for (int dx = -OUTER_RAD; dx <= OUTER_RAD; dx++) {
                    for (int dz = -OUTER_RAD; dz <= OUTER_RAD; dz++) {
                        boolean isWall = (dx == -OUTER_RAD || dx == OUTER_RAD || dz == -OUTER_RAD || dz == OUTER_RAD);
                        boolean isCeiling = (dy == TOTAL_HEIGHT - 1);
                        if (isWall || isCeiling) {
                            destLevel.setBlock(new BlockPos(x + dx, y + dy, z + dz), capsuleBlock.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        BlockPos portalPos = new BlockPos(x, y + 1, z);
        PortalBlock portalBlock = ModBlocks.getPortalBlockById(definition.id());
        if (portalBlock != null) {
            destLevel.setBlock(portalPos, portalBlock.defaultBlockState(), 3);
            var be = destLevel.getBlockEntity(portalPos);
            if (be instanceof PortalBlockEntity pbe) {
                pbe.setReturnTo(originDimension);
            }
        }

        player.teleportTo(destLevel, portalPos.getX() + 0.5, portalPos.getY() + 1, portalPos.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    /**
     * Pre-validation: returns false if any block in the capsule zone is in features_cannot_replace,
     * is bedrock, or has indestructible-level explosion resistance.
     */
    private static boolean isZoneSafeForPortal(ServerLevel level, int x, int y, int z) {
        for (int dx = -OUTER_RAD; dx <= OUTER_RAD; dx++) {
            for (int dz = -OUTER_RAD; dz <= OUTER_RAD; dz++) {
                for (int dy = 0; dy < TOTAL_HEIGHT; dy++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    Block block = state.getBlock();
                    if (state.is(FEATURES_CANNOT_REPLACE)) return false;
                    if (block == Blocks.BEDROCK) return false;
                    if (block.getExplosionResistance() >= INDESTRUCTIBLE_RESISTANCE) return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a block pos of a portal in destLevel that has returnTo == originDimension, or null.
     */
    @org.jetbrains.annotations.Nullable
    private static BlockPos findReturnPortal(ServerLevel destLevel, BlockPos center, String originDimension) {
        for (int dx = -SEARCH_RAD; dx <= SEARCH_RAD; dx++) {
            for (int dy = -SEARCH_RAD; dy <= SEARCH_RAD; dy++) {
                for (int dz = -SEARCH_RAD; dz <= SEARCH_RAD; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!(destLevel.getBlockState(pos).getBlock() instanceof PortalBlock)) continue;
                    var be = destLevel.getBlockEntity(pos);
                    if (be instanceof PortalBlockEntity pbe && originDimension.equals(pbe.getReturnTo())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static Block resolveBlock(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        } catch (Exception e) {
            return null;
        }
    }
}
