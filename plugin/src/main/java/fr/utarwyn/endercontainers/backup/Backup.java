package fr.utarwyn.endercontainers.backup;

import java.sql.Timestamp;

/**
 * Represents a backup
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Backup {

    /**
     * Backup name
     */
    private final String name;

    /**
     * Backup creation date
     */
    private final Timestamp date;

    /**
     * Name of the entity who created the backup
     */
    private final String createdBy;

    /**
     * Construct a new backup.
     *
     * @param name      backup name
     * @param date      backup creation date
     * @param createdBy name of the entity who created the backup
     */
    public Backup(String name, Timestamp date, String createdBy) {
        this.name = name;
        this.date = date;
        this.createdBy = createdBy;
    }

    /**
     * Returns the name of the backup
     *
     * @return The name of the backup
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the date of the backup
     *
     * @return The date of the backup
     */
    public Timestamp getDate() {
        return this.date;
    }

    /**
     * Returns the name of the player who created the backup.
     *
     * @return The player who created the backup
     */
    public String getCreatedBy() {
        return this.createdBy;
    }

}
