package fr.utarwyn.endercontainers.configuration;

/**
 * Stores keys for locale-based messages.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public enum LocaleKey {

    CMD_BACKUP_CREATED("commands.backups.created"),
    CMD_BACKUP_CREATION_STARTED("commands.backups.creation_starting"),
    CMD_BACKUP_EXISTS("commands.backups.exists"),
    CMD_BACKUP_INFO("commands.backups.info"),
    CMD_BACKUP_LOADED("commands.backups.loaded"),
    CMD_BACKUP_LOADING_STARTED("commands.backups.loading_starting"),
    CMD_BACKUP_REMOVED("commands.backups.removed"),
    CMD_BACKUP_UNKNOWN("commands.backups.unknown"),
    CMD_BACKUP_ZERO("commands.backups.zero"),
    CMD_BACKUP_LABEL_NAME("commands.backups.label_name"),
    CMD_BACKUP_LABEL_DATE("commands.backups.label_date"),
    CMD_BACKUP_LABEL_BY("commands.backups.label_by"),
    CMD_BACKUP_LABEL_LOADCMD("commands.backups.label_loadcmd"),
    CMD_BACKUP_LABEL_RMCMD("commands.backups.label_rmcmd"),
    CMD_CONFIG_RELOADED("commands.config_reloaded"),
    CMD_NO_UPDATE("commands.no_update"),

    MENU_MAIN_TITLE("menus.main_title"),
    MENU_CHEST_TITLE("menus.chest_title"),
    MENU_PANE_TITLE("menus.pane_title"),
    MENU_CHEST_EMPTY("menus.chest_empty"),
    MENU_CHEST_FULL("menus.chest_full"),
    MENU_CHEST_LOCKED("menus.chest_locked"),
    MENU_PREV_PAGE("menus.previous_page"),
    MENU_NEXT_PAGE("menus.next_page"),

    ERR_NOPERM_OPEN_CHEST("errors.noperm_open_chest"),
    ERR_NOPERM_CONSOLE("errors.noperm_console"),
    ERR_NOPERM_PLAYER("errors.noperm_player"),
    ERR_WORLD_DISABLED("errors.plugin_world_disabled"),
    ERR_CMD_INVALID_PARAM("errors.cmd_invalid_parameter"),
    ERR_CMD_ARG_COUNT("errors.cmd_wrong_argument_count"),
    ERR_DEP_FACTIONS("dependencies.access_denied_factions"),
    ERR_DEP_PLOTSQ("dependencies.access_denied_plotsq"),

    MISC_CHEST_NAMETAG("miscellaneous.chest_nametag");

    /**
     * Key of the message in the Yaml file
     */
    private final String key;

    /**
     * Construct a new locale key.
     *
     * @param key key of the message in the Yaml file
     */
    LocaleKey(String key) {
        this.key = key;
    }

    /**
     * Retrieve the key to search the message in the Yaml file.
     *
     * @return Yaml key of the message
     */
    public String getKey() {
        return this.key;
    }

}
