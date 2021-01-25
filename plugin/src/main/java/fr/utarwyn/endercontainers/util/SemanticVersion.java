package fr.utarwyn.endercontainers.util;

import com.google.common.base.Objects;
import org.apache.commons.lang.Validate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a semantic version. Can be compared to an other version.
 * Supports major.minor.patch version with a suffix for dev builds.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
public class SemanticVersion implements Comparable<SemanticVersion> {

    /**
     * Regex pattern used to parse a semantic version
     */
    private static final String REGEX = "(\\d+)\\.(\\d+)\\.(\\d+)(-dev)?";

    /**
     * Major version
     */
    private final int major;

    /**
     * Minor version
     */
    private final int minor;

    /**
     * Patch version
     */
    private final int patch;

    /**
     * Version in development mode
     */
    private final boolean development;

    /**
     * Constructs a semantic version from a string.
     *
     * @param version semantic version in a string format
     */
    public SemanticVersion(String version) {
        Validate.notNull(version, "semantic version cannot be null");
        Matcher matcher = Pattern.compile(REGEX).matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformated semantic version");
        }

        this.major = Integer.parseInt(matcher.group(1));
        this.minor = Integer.parseInt(matcher.group(2));
        this.patch = Integer.parseInt(matcher.group(3));
        this.development = matcher.group(4) != null;
    }

    /**
     * Retrieves the major version.
     *
     * @return major version
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * Retrieves the minor version.
     *
     * @return minor version
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * Retrieves the patch version.
     *
     * @return patch version
     */
    public int getPatch() {
        return this.patch;
    }

    /**
     * Checks if the version is in development mode.
     *
     * @return development mode
     */
    public boolean isDevelopment() {
        return this.development;
    }

    /**
     * Compares all fiels of two semantic version.
     *
     * @param other other semantic version to compare
     * @return difference between the two versions
     */
    @Override
    public int compareTo(SemanticVersion other) {
        if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }
        if (this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }
        if (this.patch != other.patch) {
            return Integer.compare(this.patch, other.patch);
        }
        if (this.development != other.development) {
            return Boolean.compare(other.development, this.development);
        }

        return 0;
    }

    /**
     * Checks if this object is equal to another.
     *
     * @param o object to compare
     * @return true if same object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticVersion that = (SemanticVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch && development == that.development;
    }

    /**
     * Retrieves object hashcode based on field values.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(major, minor, patch, development);
    }

    /**
     * Converts the semantic version into a human readable string.
     *
     * @return human readable semantic version
     */
    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch
                + (this.development ? "-dev" : "");
    }
}
