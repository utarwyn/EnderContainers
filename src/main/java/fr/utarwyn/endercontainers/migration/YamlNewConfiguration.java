package fr.utarwyn.endercontainers.migration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * An implementation of {@link YamlConfiguration} which saves all files in Yaml.
 * The particularity of this one is that it supports comments!
 * Note that this implementation is not synchronized.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public class YamlNewConfiguration extends YamlConfiguration {

	/**
	 * Stores all comments of all paths of a configuration
	 */
	private Map<String, List<String>> commentMap;

	/**
	 * Creates an empty {@link YamlNewConfiguration} with no default values and no stored comments.
	 */
	private YamlNewConfiguration() {
		this.commentMap = new HashMap<>();
	}

	/**
	 * Save the configuration into a string object
	 * and supports configuration comments!
	 * @return Configuration saved into a formatted string
	 */
	@Override
	public String saveToString() {
		String contents = super.saveToString();
		StringBuilder newContents = new StringBuilder();

		/*  Now put stored comments comments into the string  */

		String path;
		int lastBlanks = 0;
		LinkedList<String> pathConnector = new LinkedList<>();

		for (String line : contents.split("\n")) {
			if (!line.contains(":")) {
				newContents.append(line).append("\n");
				continue;
			}

			String trimmedLine = line.trim();

			int blanks = 0;
			String key = trimmedLine.split(":")[0];

			// Get number of blanks
			while (line.charAt(blanks) == ' ')
				blanks++;

			// Update the path in terms of the number of blanks
			if (lastBlanks >= blanks && pathConnector.size() > 0) {
				pathConnector.removeLast();

				if (lastBlanks > blanks && pathConnector.size() > 0)
					pathConnector.removeLast();
			}

			lastBlanks = blanks;

			// Generate path for current value
			pathConnector.add(key);
			path = StringUtils.join(pathConnector, ".");

			// Load comments before the line
			List<String> comments = this.commentMap.get(path);
			if (comments != null)
				for (String comment : comments)
					newContents.append(comment).append("\n");

			newContents.append(line).append("\n");
		}

		return newContents.toString();
	}

	/**
	 * Loads this {@link YamlNewConfiguration} from the specified string, as
	 * opposed to from file. Loads comments into memory too.
	 * <p>
	 * All the values contained within this configuration will be removed,
	 * leaving only settings and defaults, and the new values will be loaded
	 * from the given string. This script SUPPORTS COMMENTS! :-)
	 * <p>
	 * If the string is invalid in any way, an exception will be thrown.
	 *
	 * @param contents Contents of a Configuration to load.
	 * @throws InvalidConfigurationException Thrown if the specified string is
	 *     invalid.
	 * @throws IllegalArgumentException Thrown if contents is null.
	 */
	@Override
	public void loadFromString(String contents) throws InvalidConfigurationException {
		super.loadFromString(contents);

		String path;
		int lastBlanks = 0;
		List<String> lastComments = new ArrayList<>();
		LinkedList<String> pathConnector = new LinkedList<>();

		for (String line : contents.split("\n")) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith(COMMENT_PREFIX.trim())) {
				lastComments.add(line);
			} else {
				if (lastComments.size() > 0) {
					int blanks = 0;
					String key = trimmedLine.split(":")[0];

					// Get number of blanks
					while (line.charAt(blanks) == ' ')
						blanks++;

					// Update the path in terms of the number of blanks
					if (lastBlanks >= blanks && pathConnector.size() > 0) {
						pathConnector.removeLast();

						if (lastBlanks > blanks && pathConnector.size() > 0)
							pathConnector.removeLast();
					}

					lastBlanks = blanks;

					// Generate path for current value
					pathConnector.add(key);
					path = StringUtils.join(pathConnector, ".");

					// Save comments for the current path in memory
					commentMap.put(path, new ArrayList<>(lastComments));
					lastComments.clear();
				}
			}
		}

		// Remove the default header
		options().header("");
	}

	/**
	 * Apply a configuration to this one by using a link map.
	 * The link map will be used to link old keys to new keys in this
	 * new configuration. (old key -> new key)
	 * <p>
	 * So, values will be copied from the old key to the location targetted by the new key.
	 * Old keys that are not binded will be ignored.
	 * </p>
	 *
	 * @param configuration Configuration to apply on this one
 	 * @param linkMap Link map used to connect old keys and new keys
	 */
	public void applyConfiguration(YamlConfiguration configuration, Map<String, String> linkMap) {
		for (Map.Entry<String, String> link : linkMap.entrySet()) {
			// Check existance of both values
			if (!configuration.contains(link.getKey()) || !this.contains(link.getValue()))
				continue;

			// Set the old config value at the location of the new value
			this.set(link.getValue(), configuration.get(link.getKey()));
		}
	}

	/**
	 * Apply a configuration to this one.
	 * If keys doesn't exist in this one, they will not be created.
	 * They will replace only exisiting values.
	 *
	 * @param configuration Configuration to apply on this one
	 */
	public void applyConfiguration(YamlConfiguration configuration) {
		this.applySection(configuration.getRoot());
	}

	/**
	 * Apply a configuration section on this configuration
	 * @param section Configuration section to apply.
	 */
	private void applySection(ConfigurationSection section) {
		String path = section.getCurrentPath();

		ConfigurationSection thisSection = this.getConfigurationSection(path);
		if (thisSection == null) return;

		for (String key : thisSection.getKeys(false)) {
			if (thisSection.isConfigurationSection(key) && section.isConfigurationSection(key))
				applySection(section.getConfigurationSection(key));
			else if (section.contains(key))
				thisSection.set(key, section.get(key));
		}
	}

	/**
	 * Creates a new {@link YamlNewConfiguration}, loading from the given file.
	 * <p>
	 * Any errors loading the Configuration will be logged and then ignored.
	 * If the specified input is not a valid config, a blank config will be
	 * returned.
	 * <p>
	 * The encoding used may follow the system dependent default.
	 *
	 * @param file Input file
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if file is null
	 */
	public static YamlNewConfiguration loadConfiguration(File file) {
		Validate.notNull(file, "File cannot be null");

		YamlNewConfiguration config = new YamlNewConfiguration();

		try {
			config.load(file);
		} catch (FileNotFoundException ignored) {
		} catch (IOException | InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		}

		return config;
	}

	/**
	 * Creates a new {@link YamlNewConfiguration}, loading from the given stream.
	 * <p>
	 * Any errors loading the Configuration will be logged and then ignored.
	 * If the specified input is not a valid config, a blank config will be
	 * returned.
	 *
	 * @param stream Input stream
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if stream is null
	 * @deprecated does not properly consider encoding
	 * @see #load(InputStream)
	 * @see #loadConfiguration(Reader)
	 */
	@Deprecated
	public static YamlNewConfiguration loadConfiguration(InputStream stream) {
		Validate.notNull(stream, "Stream cannot be null");

		YamlNewConfiguration config = new YamlNewConfiguration();

		try {
			config.load(stream);
		} catch (IOException | InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
		}

		return config;
	}

	/**
	 * Creates a new {@link YamlNewConfiguration}, loading from the given reader.
	 * <p>
	 * Any errors loading the Configuration will be logged and then ignored.
	 * If the specified input is not a valid config, a blank config will be
	 * returned.
	 *
	 * @param reader input
	 * @return resulting configuration
	 * @throws IllegalArgumentException Thrown if stream is null
	 */
	public static YamlNewConfiguration loadConfiguration(Reader reader) {
		Validate.notNull(reader, "Stream cannot be null");

		YamlNewConfiguration config = new YamlNewConfiguration();

		try {
			config.load(reader);
		} catch (IOException | InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
		}

		return config;
	}

}
