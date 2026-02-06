package net.unfamily.adv_cm_portal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.adv_cm_portal.block.entity.PortalBlockEntity;
import net.unfamily.adv_cm_portal.portal.PortalDefinition;
import net.unfamily.adv_cm_portal.portal.PortalLoader;
import net.unfamily.adv_cm_portal.portal.SafeZoneHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Portal block: right-click to teleport. If this block has returnTo (return portal), go there;
 * else if we are in destination dimension use defaultReturn; else go to destination.
 * usableIn applies to all portals (normal and return) so one-way portals work when usableIn omits the destination.
 */
public class PortalBlock extends Block implements EntityBlock {

    private final String portalId;

    public PortalBlock(String portalId, BlockBehaviour.Properties properties) {
        super(properties);
        this.portalId = portalId;
    }

    public String getPortalId() {
        return portalId;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = getPortalShape();
        return shape != null ? shape : super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = getPortalShape();
        return shape != null ? shape : super.getVisualShape(state, level, pos, context);
    }

    /** Parses definition box (e.g. "1 1 1" or "0.5 1 0.5") into collision/visual shape; null if full block or invalid. */
    private VoxelShape getPortalShape() {
        PortalDefinition def = PortalLoader.getById(portalId);
        if (def == null) return null;
        return parseBox(def.box());
    }

    /**
     * Parses box string: "width height depth" as fractions 0–1 (space or comma separated), centered in the block.
     * One value = cube (e.g. "0.5" = 0.5×0.5×0.5). Omitted or invalid = full block (returns null).
     */
    private static VoxelShape parseBox(String box) {
        if (box == null || box.isBlank()) return null;
        String[] parts = box.trim().split("[,\\s]+");
        double w = 1, h = 1, d = 1;
        if (parts.length == 1) {
            try {
                w = h = d = parseFraction(parts[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (parts.length >= 3) {
            try {
                w = parseFraction(parts[0]);
                h = parseFraction(parts[1]);
                d = parseFraction(parts[2]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        if (w <= 0 || h <= 0 || d <= 0 || w > 1 || h > 1 || d > 1) return null;
        double minX = (1 - w) * 0.5 * 16;
        double minY = (1 - h) * 0.5 * 16;
        double minZ = (1 - d) * 0.5 * 16;
        return Shapes.box(minX / 16, minY / 16, minZ / 16, (minX + w * 16) / 16, (minY + h * 16) / 16, (minZ + d * 16) / 16);
    }

    private static double parseFraction(String s) {
        double v = Double.parseDouble(s.trim());
        return Math.max(0.001, Math.min(1, v));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        PortalDefinition def = PortalLoader.getById(portalId);
        // Block still in world but id removed from JSON (e.g. after reload): do nothing, no crash
        if (def == null) return InteractionResult.PASS;

        String currentKey = level.dimension().location().toString();
        var usableIn = def.usableIn();
        if (!usableIn.isEmpty() && !usableIn.contains(currentKey)) {
            return InteractionResult.FAIL;
        }

        String targetKey;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PortalBlockEntity pbe && pbe.getReturnTo() != null) {
            targetKey = pbe.getReturnTo();
        } else {
            if (currentKey.equals(def.destination())) {
                targetKey = def.defaultReturn();
            } else {
                targetKey = def.destination();
            }
        }
        // Empty defaultReturn: cannot use portal from destination → no second portal placed elsewhere
        if (targetKey == null || targetKey.isBlank()) {
            return InteractionResult.FAIL;
        }

        MinecraftServer server = level.getServer();
        if (server == null) return InteractionResult.PASS;
        ServerLevel destLevel = null;
        for (ServerLevel lvl : server.getAllLevels()) {
            if (lvl.dimension().location().toString().equals(targetKey)) {
                destLevel = lvl;
                break;
            }
        }
        if (destLevel == null) return InteractionResult.FAIL;

        SafeZoneHelper.buildAndTeleport(server, serverPlayer, def, destLevel, currentKey, pos);
        return InteractionResult.SUCCESS;
    }
}
