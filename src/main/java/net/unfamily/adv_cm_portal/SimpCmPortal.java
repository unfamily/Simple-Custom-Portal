package net.unfamily.adv_cm_portal;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.unfamily.adv_cm_portal.block.ModBlocks;
import net.unfamily.adv_cm_portal.block.entity.ModBlockEntities;
import net.unfamily.adv_cm_portal.command.ReloadPortalsCommand;
import net.unfamily.adv_cm_portal.item.ModItems;
import net.unfamily.adv_cm_portal.portal.PortalLoader;

@Mod(SimpCmPortal.MODID)
public class SimpCmPortal {

    public static final String MODID = "simp_cm_portal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpCmPortal(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener((ModConfigEvent.Loading e) -> Config.onConfigLoad(e));
        modEventBus.addListener((ModConfigEvent.Reloading e) -> Config.onConfigLoad(e));

        ModBlocks.init();
        ModBlocks.REGISTRAR.register(modEventBus);
        ModBlockEntities.REGISTRAR.register(modEventBus);
        ModItems.registerBlockItems();
        ModItems.REGISTRAR.register(modEventBus);
        ModCreativeModeTabs.REGISTRAR.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(ReloadPortalsCommand::register);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ModBlocks.buildDestinationMap();
        PortalLoader.reload();
    }
}
