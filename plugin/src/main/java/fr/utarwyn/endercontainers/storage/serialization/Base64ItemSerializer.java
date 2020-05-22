package fr.utarwyn.endercontainers.storage.serialization;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Serialize a list of items into a base64 string.
 * A better way to store items than using a custom serializer.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public class Base64ItemSerializer implements ItemSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(ConcurrentMap<Integer, ItemStack> items) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeInt(items.size());

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            dataOutput.writeInt(entry.getKey());
            dataOutput.writeObject(entry.getValue());
        }

        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> deserialize(String data) throws IOException {
        ConcurrentMap<Integer, ItemStack> items = new ConcurrentHashMap<>();

        byte[] bytes = Base64.getDecoder().decode(data.replaceAll("\n", ""));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        int mapSize = dataInput.readInt();
        int pos;
        ItemStack item;

        for (int i = 0; i < mapSize; i++) {
            pos = dataInput.readInt();
            try {
                item = (ItemStack) dataInput.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("cannot found ItemStack class during deserialization", e);
            }

            items.put(pos, item);
        }

        dataInput.close();

        return items;
    }

}
