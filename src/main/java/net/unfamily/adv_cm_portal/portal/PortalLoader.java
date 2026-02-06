package net.unfamily.adv_cm_portal.portal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.unfamily.adv_cm_portal.SimpCmPortal;
import net.unfamily.adv_cm_portal.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads portal definitions from JSON under &lt;externalScriptsPath&gt;/simp_cm_portal_portals/.
 * Accepts type "simp_cm_portal:portals" or "iska_utils:portals".
 */
public final class PortalLoader {

    private static final Gson GSON = new GsonBuilder().create();
    private static final List<PortalDefinition> DEFINITIONS = new ArrayList<>();

    private PortalLoader() {}

    public static List<PortalDefinition> getDefinitions() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    public static PortalDefinition getById(String id) {
        for (PortalDefinition d : DEFINITIONS) {
            if (d.id().equals(id)) return d;
        }
        return null;
    }

    /** Call after config is available. Loads from game dir + externalScriptsPath + /simp_cm_portal_portals/ */
    public static void reload() {
        DEFINITIONS.clear();
        String base = Config.externalScriptsPath;
        if (base == null || base.isBlank()) base = "kubejs/external_scripts";
        Path dir = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(base).resolve("simp_cm_portal_portals");
        try {
            if (!Files.isDirectory(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            SimpCmPortal.LOGGER.warn("[simp_cm_portal] Could not create portals dir: {}", e.getMessage());
            return;
        }
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                .forEach(PortalLoader::loadFile);
        } catch (Exception e) {
            SimpCmPortal.LOGGER.warn("[simp_cm_portal] Failed to list portals dir: {}", e.getMessage());
        }
        try {
            Files.writeString(dir.resolve("PORTALS.md"), getDocumentation());
        } catch (Exception e) {
            SimpCmPortal.LOGGER.debug("[simp_cm_portal] Could not write PORTALS.md: {}", e.getMessage());
        }
        SimpCmPortal.LOGGER.info("[simp_cm_portal] Loaded {} portal definitions", DEFINITIONS.size());
    }

    private static String getDocumentation() {
        return """
            # Simple Custom Portal — Documentation

            This file is overwritten on every world load and on `/simp_cm_portal_reload` so it stays up to date.

            ## Config path

            Portal definitions are loaded from JSON files in this folder: `<game dir>/<externalScriptsPath>/simp_cm_portal_portals/`.
            `externalScriptsPath` is set in mod config (default: `kubejs/external_scripts`). Only `.json` files are read.

            ## JSON format

            Each JSON file can contain one or more portal definitions. Root structure:

            - **type** (required): `"simp_cm_portal:portals"` or `"iska_utils:portals"`.
            - **overwritable** (optional): currently unused.
            - **portals** (required): array of portal objects. You can define as many portals as you want in the same file; each entry must have a unique **id**.

            ### Portal object

            - **id** (required): unique identifier (e.g. `"custom_dimensions-portal_void"`). Used for the block registry; only portals loaded at game startup get a block. After a reload, only the logic of existing ids is updated.
            - **destination** (required): dimension resource key the portal sends to (e.g. `"minecraft:overworld"`, `"custom_dimensions:void"`).
            - **baseBlock** (optional): block id for the floor of the safe-zone capsule (e.g. `"minecraft:stone"`). If empty, floor is not replaced.
            - **capsuleBlock** (optional): block id for walls and ceiling of the capsule (e.g. `"minecraft:glass"`). If empty, walls/ceiling are not built.
            - **box** (optional): hitbox (collision and visual) as a variable-size box centered in the block. Format: `"width height depth"` as fractions from 0 to 1, space or comma separated. Example: `"1 1 1"` or omit = full block; `"0.5 1 0.5"` = half width and depth, full height (thin pillar); `"0.5"` = cube 0.5×0.5×0.5. Invalid or empty = full block.
            - **usableIn** (optional): array of dimension keys. If non-empty, the portal can only be used when the player is in one of these dimensions. Applies to both normal and return portals — use this for one-way portals (e.g. only `["minecraft:overworld"]` so the return portal in the destination does not work).
            - **defaultReturn** (optional): when the player is already in **destination** and uses a portal without a stored return (e.g. a manually placed block), they are sent to this dimension. If empty, using the portal from the destination does nothing (no second portal is created elsewhere).

            JSON can contain `//` and `/* */` comments; they are stripped before parsing.

            ## Examples

            **Minimal (two-way, no safe-zone styling):**
            ```json
            {
              "type": "simp_cm_portal:portals",
              "portals": [
                {
                  "id": "my_portal_void",
                  "destination": "custom_dimensions:void"
                }
              ]
            }
            ```

            **Two-way with safe-zone and default return (e.g. overworld ↔ void):**
            ```json
            {
              "type": "simp_cm_portal:portals",
              "portals": [
                {
                  "id": "portal_void",
                  "destination": "custom_dimensions:void",
                  "baseBlock": "minecraft:stone",
                  "capsuleBlock": "minecraft:glass",
                  "usableIn": [],
                  "defaultReturn": "minecraft:overworld"
                }
              ]
            }
            ```

            **One-way (only from overworld to void; return portal does not work):**
            ```json
            {
              "type": "simp_cm_portal:portals",
              "portals": [
                {
                  "id": "one_way_void",
                  "destination": "custom_dimensions:void",
                  "baseBlock": "minecraft:stone",
                  "capsuleBlock": "minecraft:glass",
                  "usableIn": ["minecraft:overworld"],
                  "defaultReturn": ""
                }
              ]
            }
            ```

            **Custom hitbox (thin pillar, half width/depth, full height):**
            ```json
            {
              "type": "simp_cm_portal:portals",
              "portals": [
                {
                  "id": "slim_portal",
                  "destination": "minecraft:the_nether",
                  "box": "0.5 1 0.5"
                }
              ]
            }
            ```
            Other `box` examples: `"1 1 1"` full block; `"0.5"` small centered cube.

            **Multiple portals in one file:**
            ```json
            {
              "type": "simp_cm_portal:portals",
              "portals": [
                {
                  "id": "portal_overworld",
                  "destination": "minecraft:overworld",
                  "baseBlock": "minecraft:grass_block",
                  "capsuleBlock": "minecraft:oak_planks",
                  "defaultReturn": "custom_dimensions:void"
                },
                {
                  "id": "portal_void",
                  "destination": "custom_dimensions:void",
                  "baseBlock": "minecraft:stone",
                  "capsuleBlock": "minecraft:glass",
                  "usableIn": ["minecraft:overworld"],
                  "defaultReturn": "minecraft:overworld"
                }
              ]
            }
            ```

            ## Behaviour

            - **Click**: right-click on a portal block to teleport. The target dimension is: the block’s stored **return** dimension if it has one, otherwise **defaultReturn** when already in **destination**, otherwise **destination**.
            - **Same coordinates**: the destination portal (or capsule) is created at the same x, y, z as the portal you clicked.
            - **Return portal**: when the mod builds a capsule in the destination, it places a portal block of the same type with a stored **return** dimension (where you came from). That block then always sends you back to that dimension.
            - **Capsule only if needed**: if in the destination dimension there is already a portal with the same return dimension in a 32-block radius, the player is teleported there and no capsule is built.
            - **Safe zone**: when built, the capsule has interior 3×3×4, with floor (baseBlock), walls and ceiling (capsuleBlock), and the return portal at the centre.

            ## Validation before building

            Before building the capsule, the mod checks that no block in the zone has:
            - the vanilla tag `minecraft:features_cannot_replace`
            - the block is bedrock
            - explosion resistance ≥ 3,600,000 (indestructible)
            If any of these fail, the player gets an action-bar message (e.g. "Invalid position for portal") and no build/teleport happens.

            ## Command

            - **/simp_cm_portal_reload** (permission level 2): reloads portal definitions from all JSON files in this folder and refreshes the destination map. Only logic is updated; new ids in JSON do not get new blocks until the game is restarted.

            ## Portals removed from JSON

            If a portal block still exists in the world but its **id** was removed from every JSON (e.g. after a reload), the block does nothing and does not crash: click returns `PASS`. After a game restart, that block type may no longer exist depending on registration.

            ---
            Generated by Simple Custom Portal (simp_cm_portal).
            """;
    }

    private static void loadFile(Path path) {
        try {
            String raw = Files.readString(path);
            String json = stripJsonComments(raw);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            String type = root.has("type") ? root.get("type").getAsString() : "";
            if (!type.equals("simp_cm_portal:portals") && !type.equals("iska_utils:portals")) {
                SimpCmPortal.LOGGER.debug("[simp_cm_portal] Skip {}: type '{}' not supported", path.getFileName(), type);
                return;
            }
            JsonArray portals = root.getAsJsonArray("portals");
            if (portals == null) return;
            for (JsonElement el : portals) {
                JsonObject o = el.getAsJsonObject();
                String id = getString(o, "id");
                if (id == null || id.isBlank()) continue;
                String destination = getString(o, "destination");
                if (destination == null || destination.isBlank()) continue;
                List<String> usableIn = new ArrayList<>();
                if (o.has("usableIn") && o.get("usableIn").isJsonArray()) {
                    for (JsonElement u : o.getAsJsonArray("usableIn")) {
                        if (u.isJsonPrimitive()) usableIn.add(u.getAsString());
                    }
                    usableIn = List.copyOf(usableIn);
                } else {
                    usableIn = List.of();
                }
                DEFINITIONS.add(new PortalDefinition(
                    id.trim(),
                    destination.trim(),
                    getString(o, "baseBlock"),
                    getString(o, "capsuleBlock"),
                    getString(o, "box"),
                    usableIn,
                    getString(o, "defaultReturn")
                ));
            }
        } catch (JsonParseException e) {
            SimpCmPortal.LOGGER.warn("[simp_cm_portal] Invalid JSON in {}: {}", path.getFileName(), e.getMessage());
        } catch (Exception e) {
            SimpCmPortal.LOGGER.warn("[simp_cm_portal] Error loading {}: {}", path.getFileName(), e.getMessage());
        }
    }

    private static String getString(JsonObject o, String key) {
        if (!o.has(key)) return null;
        JsonElement e = o.get(key);
        return e.isJsonNull() ? null : e.getAsString();
    }

    /** Remove // and /* *\/ comments so JSON with comments can be parsed. */
    private static String stripJsonComments(String raw) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        boolean inString = false;
        char quote = 0;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (inBlockComment) {
                if (c == '*' && i + 1 < raw.length() && raw.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i += 2;
                } else {
                    i++;
                }
                continue;
            }
            if (inLineComment) {
                if (c == '\n' || c == '\r') inLineComment = false;
                else { i++; continue; }
            }
            if (inString) {
                out.append(c);
                if (c == '\\' && i + 1 < raw.length()) { out.append(raw.charAt(i + 1)); i += 2; continue; }
                if (c == quote) inString = false;
                i++;
                continue;
            }
            if (c == '"' || c == '\'') { inString = true; quote = c; out.append(c); i++; continue; }
            if (c == '/' && i + 1 < raw.length()) {
                char n = raw.charAt(i + 1);
                if (n == '/') { inLineComment = true; i += 2; continue; }
                if (n == '*') { inBlockComment = true; i += 2; continue; }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }
}
