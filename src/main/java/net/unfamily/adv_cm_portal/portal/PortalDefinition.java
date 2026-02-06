package net.unfamily.adv_cm_portal.portal;

import java.util.Collections;
import java.util.List;

/**
 * One portal from JSON: id, destination, optional safe-zone blocks and return dimension.
 */
public record PortalDefinition(
    String id,
    String destination,
    String baseBlock,
    String capsuleBlock,
    String box,
    List<String> usableIn,
    String defaultReturn
) {
    /** Empty = usable in any dimension. */
    public List<String> usableIn() {
        return usableIn == null ? Collections.emptyList() : usableIn;
    }

    public String baseBlock() {
        return baseBlock == null || baseBlock.isBlank() ? null : baseBlock.trim();
    }

    public String capsuleBlock() {
        return capsuleBlock == null || capsuleBlock.isBlank() ? null : capsuleBlock.trim();
    }

    public String defaultReturn() {
        return defaultReturn == null || defaultReturn.isBlank() ? null : defaultReturn.trim();
    }

    public String box() {
        return box == null || box.isBlank() ? null : box.trim();
    }
}
