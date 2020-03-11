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
     * The name of the backup
     */
    private String name;

    /**
     * The date of the backup
     */
    private Timestamp date;

    /**
     * The type of the backup (only "all" at the moment)
     */
    private String type;

    /**
     * Store the name of the player which creates the backup
     */
    private String createdBy;

    /**
     * Construct a new backup
     *
     * @param name      Name of the backup
     * @param date      Date of the backup
     * @param type      Type of the backup
     * @param createdBy Player who creates the backup
     */
    public Backup(String name, Timestamp date, String type, String createdBy) {
        this.name = name;
        this.date = date;
        this.type = type;
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
     * Returns the type of the backup
     *
     * @return The type of the backup
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the name of the player who created the backup
     *
     * @return The player who created the backup
     */
    public String getCreatedBy() {
        return this.createdBy;
    }

}
