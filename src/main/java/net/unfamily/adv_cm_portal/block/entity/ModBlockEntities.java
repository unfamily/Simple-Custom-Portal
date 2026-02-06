package net.unfamily.adv_cm_portal.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.adv_cm_portal.SimpCmPortal;
import net.unfamily.adv_cm_portal.block.ModBlocks;


public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRAR =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SimpCmPortal.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalBlockEntity>> PORTAL = REGISTRAR.register(
        "portal",
        () -> {
            Block[] blocks = ModBlocks.getPortalBlocksById().values().stream()
                .map(ref -> ref.get())
                .toArray(Block[]::new);
            return BlockEntityType.Builder.of(PortalBlockEntity::new, blocks).build(null);
        }
    );

    private ModBlockEntities() {}
}
