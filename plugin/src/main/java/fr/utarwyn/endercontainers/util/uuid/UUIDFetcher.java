package fr.utarwyn.endercontainers.util.uuid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Allows to convert an UUID to a playername and vice-versa
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class UUIDFetcher {

    /**
     * Date when name changes were introduced
     *
     * @see UUIDFetcher#getUUIDAt(String, long)
     */
    public static final long NAME_CHANGES_INTRODUCED = 1422748800000L;

    /**
     * URL used to convert an UUID to a playername at a specific time
     */
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";

    /**
     * URL used to convert a playername to an UUID
     */
    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";

    /**
     * Gson library object used to parse the retrieved JSON data and convert it to object
     */
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    /**
     * Cache playername -> UUID
     */
    private static final Cache<String, UUID> nameCache;

    /**
     * Cache UUID -> playername
     */
    private static final Cache<UUID, String> idCache;

    /**
     * Pool to execute fetch tasks with an optimized queue
     */
    private static final ExecutorService pool;

    static {
        nameCache = new Cache<>();
        idCache = new Cache<>();

        pool = Executors.newCachedThreadPool();
    }

    /**
     * Name used to temporary store data retrieved from the Mojang API
     */
    private String name;

    /**
     * Id used to temporary store data retrieved from the Mojang API
     */
    private UUID id;

    /**
     * Utility class. Cannot be instanciated.
     */
    private UUIDFetcher() {
    }

    /**
     * Fetches the uuid synchronously and returns it
     *
     * @param name The name
     * @return The uuid
     */
    public static UUID getUUID(String name) {
        return getUUIDAt(name, System.currentTimeMillis());
    }

    /**
     * Fetches the uuid synchronously for a specified name and time
     *
     * @param name      The name
     * @param timestamp Time when the player had this name in milliseconds
     * @see UUIDFetcher#NAME_CHANGES_INTRODUCED
     */
    private static UUID getUUIDAt(String name, long timestamp) {
        // UUID in cache!
        if (nameCache.contains(name))
            return nameCache.get(name);

        // Player connected!
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.isOnline()) return player.getUniqueId();

        // For an offline server, it's better to use the Bukkit local cache.
        if (!Bukkit.getOnlineMode()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

            if (offlinePlayer.hasPlayedBefore()) {
                nameCache.put(name, offlinePlayer.getUniqueId());
                idCache.put(offlinePlayer.getUniqueId(), name);

                return offlinePlayer.getUniqueId();
            }

            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000)).openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher data = GSON.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

            nameCache.put(name, data.id);
            idCache.put(data.id, data.name);

            return data.id;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetches the name asynchronously and passes it to the consumer
     *
     * @param uuid   The uuid
     * @param action Do what you want to do with the name her
     */
    public static void getName(UUID uuid, Consumer<String> action) {
        pool.execute(() -> action.accept(getName(uuid)));
    }

    /**
     * Fetches the name synchronously and returns it
     *
     * @param uuid The uuid
     * @return The name
     */
    public static String getName(UUID uuid) {
        // Name in cache!
        if (idCache.contains(uuid))
            return idCache.get(uuid);

        // Player connected!
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) return player.getName();

        // For an offline server, it's better to use the Bukkit local cache.
        if (!Bukkit.getOnlineMode()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            if (offlinePlayer.hasPlayedBefore()) {
                idCache.put(uuid, offlinePlayer.getName());
                nameCache.put(offlinePlayer.getName(), uuid);

                return offlinePlayer.getName();
            }

            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher[] nameHistory = GSON.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
            UUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];

            nameCache.put(currentNameData.name.toLowerCase(), uuid);
            idCache.put(uuid, currentNameData.name);

            return currentNameData.name;
        } catch (IOException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the player name from the Mojang Api", e);
        }

        return null;
    }

    /**
     * Optimized cache class (Key -> Value) with
     *
     * @param <K> Key
     * @param <V> Value
     */
    private static class Cache<K, V> {

        private long expireTime = 1000L * 60 * 5; // add 5 min by default

        private ConcurrentHashMap<K, CachedEntry<V>> map = new ConcurrentHashMap<>();

        public boolean contains(K key) {
            return map.containsKey(key) && get(key) != null;
        }

        public V get(K key) {
            CachedEntry<V> entry = map.get(key);
            if (entry == null) return null;
            if (entry.isExpired()) {
                map.remove(key);
                return null;
            } else {
                return entry.getValue();
            }
        }

        public void put(K key, V value) {
            map.put(key, new CachedEntry<>(value, expireTime));
        }

        /**
         * Store a cached value
         *
         * @param <V> Entry type to cache
         */
        private static class CachedEntry<V> {

            private final SoftReference<V> value; //Caching is low memory priortiy

            private final long expires;

            CachedEntry(V value, long expireTime) {
                this.value = new SoftReference<>(value);
                this.expires = expireTime + System.currentTimeMillis();
            }

            V getValue() {
                return (isExpired()) ? null : value.get();
            }

            boolean isExpired() {
                return value.get() == null || expires != -1 && expires > System.currentTimeMillis();
            }

        }

    }

}
