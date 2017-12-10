package fr.utarwyn.endercontainers.storage;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.MysqlManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows to manage storage using different technologies with same methods.
 * (for exemple MySQL and flatfile)
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class StorageWrapper {

	/**
	 * Class cache used to improve performance to find classes using reflection
	 */
	private static Map<Class<? extends StorageWrapper>, Class<? extends StorageWrapper>> classCacheMap;

	/**
	 * Cache used to improve performance when a script searchs for a specific storage
	 */
	private static Map<Class<? extends StorageWrapper>, List<StorageWrapper>> cacheMap;

	/**
	 * The MySQL manager used by storages using MySQL
	 */
	private static MysqlManager mysqlManager;

	static {
		mysqlManager = EnderContainers.getInstance().getInstance(MysqlManager.class);
		classCacheMap = new HashMap<>();
		cacheMap = new HashMap<>();
	}

	/**
	 * Returns the MySQL manager used by some MySQL storages
	 * @return The MySQL manager
	 */
	protected static MysqlManager getMysqlManager() {
		return mysqlManager;
	}

	/**
	 * Called when a storage needs to load something
	 */
	protected abstract void load();

	/**
	 * Called when a storage needs to save something
	 */
	protected abstract void save();

	/**
	 * Used to get a specific storage with generic params which can be recognized
	 * by all storages wrappers (initialized with these params too).
	 *
	 * @param params Generic params to perform the comparison
	 * @return true if all params match with the stored params in wrapper
	 */
	protected abstract boolean equals(Object... params);

	/**
	 * Allows to get is the storage is unused or not.
	 * @return true if the storage is not used
	 */
	protected abstract boolean isUnused();

	/**
	 * Get or create a specific storage wrapper by its class and params.
	 * If the same storage wrapper with the same class and the same params exists,
	 * this one will be returned. If it was not found, a new storage wrapper will be
	 * created with the given parameters and stored in the cache map.
	 *
	 * @param clazz The class of storage wrapper to load
	 * @param params Params to use for the comparison or the initialization of the storage wrapper
	 * @param <T> Generic type which represents the wanted storage wrapper
	 * @return The storage wrapper got or created
	 */
	public static <T extends StorageWrapper> T get(Class<T> clazz, Object... params) {
		if (!cacheMap.containsKey(clazz))
			cacheMap.put(clazz, new ArrayList<>());

		for (StorageWrapper storageWrapper : cacheMap.get(clazz))
			if (storageWrapper.equals(params) && clazz.isInstance(storageWrapper))
				return clazz.cast(storageWrapper);

		// Class not in cache yet? OMG
		// Create it from the class name passed through the method.
		Class<? extends StorageWrapper> usedDataClazz = null;

		if (!classCacheMap.containsKey(clazz)) {
			String dataClazzName = (mysqlManager.isReady()) ? clazz.getName().replace("Data", "MySQLData") : clazz.getName().replace("Data", "FlatData");

			try {
				usedDataClazz = (Class<? extends StorageWrapper>) Class.forName(dataClazzName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			classCacheMap.put(clazz, usedDataClazz);
		} else {
			usedDataClazz = classCacheMap.get(clazz);
		}

		try {
			Constructor cstr = usedDataClazz.getDeclaredConstructors()[0];
			cstr.setAccessible(true);
			Object obj;

			if (params.length > 0) {
				obj = cstr.newInstance(params);
			} else
				obj = cstr.newInstance();

			if (usedDataClazz.isInstance(obj)) {
				T objUsedData = (T) usedDataClazz.cast(obj);
				cacheMap.get(clazz).add(objUsedData);
				return objUsedData;
			}

			return null;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Clear the cache by deleting storage wrappers with given class that are unused.
	 * @param clazz Class to match with storage wrappers to check
	 */
	public static void clearUnusedCache(Class<? extends StorageWrapper> clazz) {
		if (!cacheMap.containsKey(clazz)) return;
		cacheMap.get(clazz).removeIf(StorageWrapper::isUnused);
	}

	/**
	 * Unload a storage wrapper to free the memory with given class and params.
	 * @param clazz Class to match with storage wrappers
	 * @param params Params to match with storage wrappers
	 */
	public static void unload(Class<? extends StorageWrapper> clazz, Object... params) {
		if (!cacheMap.containsKey(clazz)) return;

		cacheMap.get(clazz).removeIf(storageWrapper -> storageWrapper.equals(params));

		if (params.length == 0 || cacheMap.get(clazz).size() == 0) {
			classCacheMap.remove(clazz);
			cacheMap.remove(clazz);
		}
	}

}
