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
            SoundType soundType = resolveSoundType(def.sound());
            DeferredBlock<Block> block = REGISTRAR.register("portal_" + path, () -> new PortalBlock(def.id(), BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(0.5f)
                .sound(soundType)));
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

    /** Resolves optional sound name from JSON to SoundType. Names from https://kubejs.com/wiki/ref/SoundType Default is GLASS. */
    private static SoundType resolveSoundType(String soundName) {
        if (soundName == null || soundName.isBlank()) return SoundType.GLASS;
        switch (soundName.trim().toLowerCase()) {
            case "rock":
            case "stone":
                return SoundType.STONE;
            case "glass":
                return SoundType.GLASS;
            case "wood":
                return SoundType.WOOD;
            case "metal":
            case "iron":
                return SoundType.METAL;
            case "wool":
                return SoundType.WOOL;
            case "sand":
                return SoundType.SAND;
            case "gravel":
                return SoundType.GRAVEL;
            case "grass":
                return SoundType.GRASS;
            case "lily_pad":
                return SoundType.LILY_PAD;
            case "snow":
                return SoundType.SNOW;
            case "powder_snow":
                return SoundType.POWDER_SNOW;
            case "ladder":
                return SoundType.LADDER;
            case "anvil":
                return SoundType.ANVIL;
            case "slime_block":
                return SoundType.SLIME_BLOCK;
            case "honey_block":
                return SoundType.HONEY_BLOCK;
            case "coral_block":
                return SoundType.CORAL_BLOCK;
            case "bamboo":
                return SoundType.BAMBOO;
            case "bamboo_sapling":
                return SoundType.BAMBOO_SAPLING;
            case "scaffolding":
                return SoundType.SCAFFOLDING;
            case "sweet_berry_bush":
                return SoundType.SWEET_BERRY_BUSH;
            case "crop":
                return SoundType.CROP;
            case "hard_crop":
                return SoundType.HARD_CROP;
            case "vine":
                return SoundType.VINE;
            case "nether_wart":
                return SoundType.NETHER_WART;
            case "lantern":
                return SoundType.LANTERN;
            case "stem":
                return SoundType.STEM;
            case "nylium":
                return SoundType.NYLIUM;
            case "fungus":
                return SoundType.FUNGUS;
            case "roots":
                return SoundType.ROOTS;
            case "shroomlight":
                return SoundType.SHROOMLIGHT;
            case "weeping_vines":
                return SoundType.WEEPING_VINES;
            case "twisting_vines":
                return SoundType.TWISTING_VINES;
            case "soul_sand":
                return SoundType.SOUL_SAND;
            case "soul_soil":
                return SoundType.SOUL_SOIL;
            case "basalt":
                return SoundType.BASALT;
            case "wart_block":
                return SoundType.WART_BLOCK;
            case "netherrack":
                return SoundType.NETHERRACK;
            case "nether_bricks":
                return SoundType.NETHER_BRICKS;
            case "nether_sprouts":
                return SoundType.NETHER_SPROUTS;
            case "nether_ore":
                return SoundType.NETHER_ORE;
            case "bone_block":
                return SoundType.BONE_BLOCK;
            case "netherite_block":
                return SoundType.NETHERITE_BLOCK;
            case "ancient_debris":
                return SoundType.ANCIENT_DEBRIS;
            case "lodestone":
                return SoundType.LODESTONE;
            case "chain":
                return SoundType.CHAIN;
            case "nether_gold_ore":
                return SoundType.NETHER_GOLD_ORE;
            case "gilded_blackstone":
                return SoundType.GILDED_BLACKSTONE;
            case "candle":
                return SoundType.CANDLE;
            case "amethyst":
                return SoundType.AMETHYST;
            case "amethyst_cluster":
                return SoundType.AMETHYST_CLUSTER;
            case "small_amethyst_bud":
                return SoundType.SMALL_AMETHYST_BUD;
            case "medium_amethyst_bud":
                return SoundType.MEDIUM_AMETHYST_BUD;
            case "large_amethyst_bud":
                return SoundType.LARGE_AMETHYST_BUD;
            case "tuff":
                return SoundType.TUFF;
            case "calcite":
                return SoundType.CALCITE;
            case "dripstone_block":
                return SoundType.DRIPSTONE_BLOCK;
            case "pointed_dripstone":
                return SoundType.POINTED_DRIPSTONE;
            case "copper":
                return SoundType.COPPER;
            case "cave_vines":
                return SoundType.CAVE_VINES;
            case "spore_blossom":
                return SoundType.SPORE_BLOSSOM;
            case "azalea":
                return SoundType.AZALEA;
            case "flowering_azalea":
                return SoundType.FLOWERING_AZALEA;
            case "moss_carpet":
                return SoundType.MOSS_CARPET;
            case "pink_petals":
                return SoundType.PINK_PETALS;
            case "moss":
                return SoundType.MOSS;
            case "big_dripleaf":
                return SoundType.BIG_DRIPLEAF;
            case "small_dripleaf":
                return SoundType.SMALL_DRIPLEAF;
            case "rooted_dirt":
                return SoundType.ROOTED_DIRT;
            case "hanging_roots":
                return SoundType.HANGING_ROOTS;
            case "azalea_leaves":
                return SoundType.AZALEA_LEAVES;
            case "sculk_sensor":
                return SoundType.SCULK_SENSOR;
            case "sculk_catalyst":
                return SoundType.SCULK_CATALYST;
            case "sculk":
                return SoundType.SCULK;
            case "sculk_vein":
                return SoundType.SCULK_VEIN;
            case "sculk_shrieker":
                return SoundType.SCULK_SHRIEKER;
            case "glow_lichen":
                return SoundType.GLOW_LICHEN;
            case "deepslate":
                return SoundType.DEEPSLATE;
            case "deepslate_bricks":
                return SoundType.DEEPSLATE_BRICKS;
            case "deepslate_tiles":
                return SoundType.DEEPSLATE_TILES;
            case "polished_deepslate":
                return SoundType.POLISHED_DEEPSLATE;
            case "froglight":
                return SoundType.FROGLIGHT;
            case "frogspawn":
                return SoundType.FROGSPAWN;
            case "mangrove_roots":
                return SoundType.MANGROVE_ROOTS;
            case "muddy_mangrove_roots":
                return SoundType.MUDDY_MANGROVE_ROOTS;
            case "mud":
                return SoundType.MUD;
            case "mud_bricks":
                return SoundType.MUD_BRICKS;
            case "packed_mud":
                return SoundType.PACKED_MUD;
            case "hanging_sign":
                return SoundType.HANGING_SIGN;
            case "nether_wood_hanging_sign":
                return SoundType.NETHER_WOOD_HANGING_SIGN;
            case "bamboo_wood_hanging_sign":
                return SoundType.BAMBOO_WOOD_HANGING_SIGN;
            case "bamboo_wood":
                return SoundType.BAMBOO_WOOD;
            case "nether_wood":
                return SoundType.NETHER_WOOD;
            case "cherry_wood":
                return SoundType.CHERRY_WOOD;
            case "cherry_sapling":
                return SoundType.CHERRY_SAPLING;
            case "cherry_leaves":
                return SoundType.CHERRY_LEAVES;
            case "cherry_wood_hanging_sign":
                return SoundType.CHERRY_WOOD_HANGING_SIGN;
            case "chiseled_bookshelf":
                return SoundType.CHISELED_BOOKSHELF;
            case "suspicious_sand":
                return SoundType.SUSPICIOUS_SAND;
            case "suspicious_gravel":
                return SoundType.SUSPICIOUS_GRAVEL;
            case "decorated_pot":
                return SoundType.DECORATED_POT;
            case "decorated_pot_cracked":
                return SoundType.DECORATED_POT_CRACKED;
            default:
                SimpCmPortal.LOGGER.warn("[simp_cm_portal] Unknown sound '{}', using GLASS. See https://kubejs.com/wiki/ref/SoundType for valid names", soundName);
                return SoundType.GLASS;
        }
    }
}
