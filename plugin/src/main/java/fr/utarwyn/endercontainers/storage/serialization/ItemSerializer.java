package fr.utarwyn.endercontainers.storage.serialization;

import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * Serialize a list of items into a formatted string and vice-versa.
 * Use to store them easier than with the {@link ItemStack#toString()} format.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public interface ItemSerializer {

    /**
     * Serialize a map of items to a custom string.
     *
     * @param items map of items to encode
     * @return resulted formatted string with all items data
     * @throws IOException thrown if an error occured during the serialization
     */
    String serialize(ConcurrentMap<Integer, ItemStack> items) throws IOException;

    /**
     * Unserialize a map of items from a custom formatted string.
     *
     * @param data data string to decode
     * @return generated map of items from the string data
     * @throws IOException thrown if an error occured during the deserialization
     */
    ConcurrentMap<Integer, ItemStack> deserialize(String data) throws IOException;

}
