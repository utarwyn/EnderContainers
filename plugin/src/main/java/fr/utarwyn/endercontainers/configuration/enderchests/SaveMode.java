package fr.utarwyn.endercontainers.configuration.enderchests;

/**
 * The mode in which the plugin will save enderchests.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public enum SaveMode {
    /**
     * Save enderchests of a player when he logs out.
     * It's the default mode.
     */
    LOGOUT("logout"),
    /**
     * Save enderchests of a player when he closes the inventory.
     */
    ON_CLOSE("on-close"),
    /**
     * Save all enderchests during a world save.
     */
    WORLD_SAVE("world-save"),
    ;

    private final String name;

    SaveMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SaveMode fromName(String name) {
        for (SaveMode mode : SaveMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
