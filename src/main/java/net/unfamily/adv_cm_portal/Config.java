package net.unfamily.adv_cm_portal;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.comment("Paths and options for future features").push("general");
    }
    private static final ModConfigSpec.ConfigValue<String> EXTERNAL_SCRIPTS_PATH = BUILDER
            .comment("Base path for external JSON data. Default: kubejs/external_scripts")
            .define("externalScriptsPath", "kubejs/external_scripts");
    static {
        BUILDER.pop();
    }

    /** Resolved at config load. Null before first load. */
    public static String externalScriptsPath;

    static final ModConfigSpec SPEC = BUILDER.build();

    public static void onConfigLoad(ModConfigEvent event) {
        externalScriptsPath = EXTERNAL_SCRIPTS_PATH.get();
    }
}
