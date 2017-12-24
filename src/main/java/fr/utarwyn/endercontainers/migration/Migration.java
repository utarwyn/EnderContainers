package fr.utarwyn.endercontainers.migration;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.MysqlManager;
import fr.utarwyn.endercontainers.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Represents a migration which can be executed
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class Migration {

	/**
	 * Database object used for MySQL migrations
	 */
	private static Database database;

	/**
	 * Old version of the plugin that the migration needs to be runned
	 */
	private String fromVers;

	/**
	 * New version of the plugin that the migration needs to be runned
	 */
	private String toVers;

	/**
	 * Version of stored data of the plugin
	 */
	private String dataVersion;

	/**
	 * Constructs a new migration
	 * @param fromVers Old version of the plugin for detections
	 * @param toVers New version of the plugin for detections
	 */
	public Migration(String fromVers, String toVers) {
		this.fromVers = fromVers;
		this.toVers = toVers;

	}

	/**
	 * Calculates if the migration has to be runned or not
	 * @return True if the migration has to be runned
	 */
	public boolean hasToBePerformed() {
		Pattern pFrom = Pattern.compile(this.fromVers);
		Pattern pTo = Pattern.compile(this.toVers);

		String dataFromVers = this.getDataVersion();
		String dataToVers = Migration.getPluginVersion();

		return pFrom.matcher(dataFromVers).find() && pTo.matcher(dataToVers).find();
	}

	/**
	 * Gets the current version of stored data
	 * @return Version of stored data (for EnderContainers)
	 */
	protected String getDataVersion() {
		if (this.dataVersion != null)
			return this.dataVersion;

		// By default, gets the version from the version file in the data folder
		File file = new File(EnderContainers.getInstance().getDataFolder(), MigrationManager.VERSION_FILE);

		if (file.exists()) {
			try {
				this.dataVersion = new Scanner(new FileInputStream(file)).nextLine();
			} catch (FileNotFoundException e) {
				System.err.println("Cannot get the local version of the plugin! Please fix that by creating a \"version\" file in the data folder of the plugin with the version of the plugin inside (" + getPluginVersion() + ").");
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Gets the data folder of the plugin
	 * @return Data folder of the plugin
	 */
	protected File getDataFolder() {
		return EnderContainers.getInstance().getDataFolder();
	}

	/**
	 * Prepares the migration
	 */
	void prepare() {
		File backupFolder = this.getBackupFolder();

		Log.log(" ", true);
		Log.log("|----------------------------------|", true);
		Log.log(String.format("| Migration from %5s to %5s    |", this.fromVers, this.toVers), true);
		Log.log("|----------------------------------|", true);
		Log.log(" ", true);
		Log.log("Backuping data files into the \"EnderContainers/" + backupFolder.getName() + "/\" folder...", true);
		Log.log(" ", true);

		// Create the backup folder
		Migration.recursiveCopy(this.getDataFolder(), backupFolder);
	}

	/**
	 * Announces the migration
	 */
	void announce() {
		Log.log("ALERT! A migration have to be applied before continue using the plugin!", true);
		Log.log("Migration detected from version " + this.fromVers + " to version " + this.toVers + "!", true);
		Log.log("Starting migration...", true);
	}

	/**
	 * Ends the migration
	 */
	void end() {
		Log.log("", true);
		Log.log("Migration from " + this.fromVers + " to " + this.toVers + " finished!", true);
		Log.log("Please restart the server to enable the plugin!", true);
		Log.log("If you have any error after the restart, please contact the plugin's author!", true);
	}

	/**
	 * Called to perform the migration
	 */
	public abstract void perform();

	/**
	 * Do a recursive copy from a folder to another folder
	 * @param fSource Source folder
	 * @param fDest Destination folder
	 */
	private static void recursiveCopy(File fSource, File fDest) {
		try {
			if (fSource.isDirectory()) {
				if (!fDest.exists() && !fDest.mkdirs())
					return;

				// Create list of files and directories on the current source
				String[] fList = fSource.list();
				assert fList != null;

				for (String aFList : fList) {
					File dest = new File(fDest, aFList);
					File source = new File(fSource, aFList);

					if (aFList.equals(fDest.getName()))
						continue;

					recursiveCopy(source, dest);
				}
			}
			else {
				// Found a file. Copy it into the destination, which is already created in 'if' condition above
				FileInputStream fInStream = new FileInputStream(fSource);
				FileOutputStream fOutStream = new FileOutputStream(fDest);

				byte[] buffer = new byte[2048];
				int iBytesReads;

				while ((iBytesReads = fInStream.read(buffer)) >= 0)
					fOutStream.write(buffer, 0, iBytesReads);

				fInStream.close();
				fOutStream.close();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the current version stored in the plugin.yml file
	 * @return Current version of the code
	 */
	static String getPluginVersion() {
		return EnderContainers.getInstance().getDescription().getVersion();
	}

	/**
	 * Gets the File object of the backup folder for the migration
	 * @return Backup folder for the migration
	 */
	protected File getBackupFolder() {
		return new File(this.getDataFolder(), "old_" + this.fromVers.replaceAll("\\*", "X").replaceAll("\\.", "_") + "/");
	}

	/**
	 * Gets the database object for MySQL migrations.
	 * <p>
	 * This method devious the normal access of the database object
	 * to have a global access to the database.
	 * (Because migrations normally do special requests to upgrade tables)
	 * </p>
	 *
	 * @return The database object
	 */
	protected static Database getDatabase() {
		if (database != null)
			return database;

		MysqlManager manager = EnderContainers.getInstance().getInstance(MysqlManager.class);
		if (!manager.isReady()) return null;

		// Not very clear but its necessary to modify tables like we want
		try {
			Field f = manager.getClass().getDeclaredField("database");

			f.setAccessible(true);
			database = (Database) f.get(manager);
			f.setAccessible(false);

			return database;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Runs the <b>init method</b> of the MySQL manager.
	 * <p>
	 * This method uses a trick because the init method is normally in
	 * private access. But we need to access to this method because it
	 * creates tables if don't exist.
	 * </p>
	 */
	protected static void recreateMySQLTables() {
		MysqlManager manager = EnderContainers.getInstance().getInstance(MysqlManager.class);
		if (!manager.isReady()) return;

		// Not very clear but its necessary to recreate tables easily from a migration
		try {
			Method init = manager.getClass().getDeclaredMethod("init");

			init.setAccessible(true);
			init.invoke(manager);
			init.setAccessible(false);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
