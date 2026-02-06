package net.unfamily.adv_cm_portal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.unfamily.adv_cm_portal.SimpCmPortal;
import net.unfamily.adv_cm_portal.block.ModBlocks;
import net.unfamily.adv_cm_portal.portal.PortalLoader;

/**
 * Registers the simp_cm_portal_reload command: reloads portal definitions from JSON and refreshes the destination map.
 * Only the logic is updated; blocks/items already loaded at startup are unchanged (new ids in JSON won't get new blocks).
 */
public final class ReloadPortalsCommand {

    private static final String NAME = SimpCmPortal.MODID + "_reload";

    private ReloadPortalsCommand() {}

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal(NAME)
                .requires(cs -> cs.hasPermission(2))
                .executes(ReloadPortalsCommand::run)
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        PortalLoader.reload();
        ModBlocks.buildDestinationMap();
        context.getSource().sendSuccess(() -> Component.translatable("message.simp_cm_portal.reload_success"), true);
        return 1;
    }
}
