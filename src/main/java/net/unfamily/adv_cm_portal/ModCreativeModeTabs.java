package net.unfamily.adv_cm_portal;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.adv_cm_portal.block.ModBlocks;

public final class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRAR =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SimpCmPortal.MODID);

    static {
        REGISTRAR.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.simp_cm_portal.main"))
            .icon(() -> {
                var blocks = ModBlocks.getPortalBlocksById().values();
                var it = blocks.iterator();
                if (!it.hasNext()) return ItemStack.EMPTY;
                return new ItemStack(it.next().get().asItem());
            })
            .displayItems((params, output) -> {
                for (var block : ModBlocks.getPortalBlocksById().values()) {
                    output.accept(block.get().asItem());
                }
            })
            .build());
    }

    private ModCreativeModeTabs() {}
}
