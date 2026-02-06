package net.unfamily.adv_cm_portal.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.adv_cm_portal.SimpCmPortal;
import net.unfamily.adv_cm_portal.portal.PortalDefinition;
import net.unfamily.adv_cm_portal.portal.PortalLoader;

import java.util.HashMap;
import java.util.Map;

public final class ModBlocks {

    public static final DeferredRegister.Blocks REGISTRAR = DeferredRegister.createBlocks(SimpCmPortal.MODID);

    /** Portal definition id -> block. Filled after registration. */
    private static final Map<String, DeferredBlock<Block>> PORTAL_BLOCKS_BY_ID = new HashMap<>();
    /** Dimension key -> block to place as return portal. Filled after registration. */
    private static final Map<String, Block> BLOCK_BY_DESTINATION = new HashMap<>();

    private ModBlocks() {}

    /** Call before register(). Loads portals and registers one block per definition. */
    public static void init() {
        PortalLoader.reload();
        PORTAL_BLOCKS_BY_ID.clear();
        BLOCK_BY_DESTINATION.clear();
        for (PortalDefinition def : PortalLoader.getDefinitions()) {
            String path = sanitizeId(def.id());
            DeferredBlock<Block> block = REGISTRAR.register("portal_" + path, () -> new PortalBlock(def.id(), BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(0.5f)
                .sound(SoundType.GLASS)));
            PORTAL_BLOCKS_BY_ID.put(def.id(), block);
        }
    }

    /** Call from mod init after blocks are registered (e.g. in FMLCommonSetupEvent) to build destination map. */
    public static void buildDestinationMap() {
        BLOCK_BY_DESTINATION.clear();
        for (PortalDefinition def : PortalLoader.getDefinitions()) {
            DeferredBlock<Block> ref = PORTAL_BLOCKS_BY_ID.get(def.id());
            if (ref != null) {
                BLOCK_BY_DESTINATION.put(def.destination(), ref.get());
            }
        }
    }

    public static Block getPortalBlockForDimension(String dimensionKey) {
        return BLOCK_BY_DESTINATION.get(dimensionKey);
    }

    /** Same block type for this portal definition (used for return portal placement). */
    public static PortalBlock getPortalBlockById(String portalId) {
        DeferredBlock<Block> ref = PORTAL_BLOCKS_BY_ID.get(portalId);
        return ref == null ? null : (PortalBlock) ref.get();
    }

    public static Map<String, DeferredBlock<Block>> getPortalBlocksById() {
        return Map.copyOf(PORTAL_BLOCKS_BY_ID);
    }

    private static String sanitizeId(String id) {
        return sanitizeIdPublic(id);
    }

    public static String sanitizeIdPublic(String id) {
        return id.replace(":", "_").replace("-", "_");
    }
}
