package net.unfamily.adv_cm_portal.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.adv_cm_portal.SimpCmPortal;
import net.unfamily.adv_cm_portal.block.ModBlocks;

import java.util.Map;

public final class ModItems {

    public static final DeferredRegister.Items REGISTRAR = DeferredRegister.createItems(SimpCmPortal.MODID);

    private ModItems() {}

    /** Call after ModBlocks.init(). Registers BlockItem for each portal block. */
    public static void registerBlockItems() {
        for (Map.Entry<String, DeferredBlock<Block>> e : ModBlocks.getPortalBlocksById().entrySet()) {
            String id = e.getKey();
            DeferredBlock<Block> block = e.getValue();
            REGISTRAR.register("portal_" + ModBlocks.sanitizeIdPublic(id), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    public static DeferredRegister.Items getRegistrar() {
        return REGISTRAR;
    }
}
