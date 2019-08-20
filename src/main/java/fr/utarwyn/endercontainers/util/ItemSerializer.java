package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Class used to convert a list of items into string and vice-versa.
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class ItemSerializer {

    private ItemSerializer() {
        // Not implemented
    }

    /**
     * Build a string with a map of ItemStacks.
     *
     * @param items Map of ItemStacks to convert.
     * @return The ItemStacks formatted to string.
     */
    public static String serialize(ConcurrentMap<Integer, ItemStack> items) {
        if (Files.getConfiguration().isUseExperimentalSavingSystem())
            return ItemSerializer.experimentalSerialization(items);
        else
            return ItemSerializer.base64Serialization(items);
    }

    /**
     * Restore the map of ItemStacks from a formatted string.
     *
     * @param data String to parse.
     * @return Generated map of Itemstacks.
     */
    public static ConcurrentMap<Integer, ItemStack> deserialize(String data) {
        if (Files.getConfiguration().isUseExperimentalSavingSystem())
            return ItemSerializer.experimentalDeserialization(data);
        else
            return ItemSerializer.base64Deserialization(data);
    }

    /**
     * Build a string with a map of ItemStacks. Experimental version.
     *
     * @param items Map of ItemStacks to convert.
     * @return The ItemStacks formatted to string.
     */
    private static String experimentalSerialization(ConcurrentMap<Integer, ItemStack> items) {
        StringBuilder serialization = new StringBuilder(new String((items.size() + ";").getBytes(), StandardCharsets.UTF_8));

        for (ConcurrentMap.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack is = entry.getValue();

            if (is != null) {
                StringBuilder serializedItemStack = new StringBuilder(new String("".getBytes(), StandardCharsets.UTF_8));

                // Item type
                String isType = String.valueOf(is.getType());
                serializedItemStack.append("t@").append(isType);

                // Item durability
                if (is.getDurability() != 0) {
                    String isDurability = String.valueOf(is.getDurability());
                    serializedItemStack.append(":d@").append(isDurability);
                }

                // Amount of the itemstack
                if (is.getAmount() != 1) {
                    String isAmount = String.valueOf(is.getAmount());
                    serializedItemStack.append(":a@").append(isAmount);
                }

                // Enchantments
                Map<Enchantment, Integer> isEnch = is.getEnchantments();

                if (isEnch.size() > 0) {
                    for (ConcurrentHashMap.Entry<Enchantment, Integer> enchEntry : isEnch.entrySet()) {
                        serializedItemStack.append(":e@")
                                .append(CompatibilityHelper.enchantmentToString(enchEntry.getKey()))
                                .append("@").append(enchEntry.getValue());
                    }
                }

                // Display name
                if (is.getItemMeta().getDisplayName() != null) {
                    String[] itemDisplayName = new String(
                            is.getItemMeta().getDisplayName().getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8
                    ).split(" ");

                    serializedItemStack.append(":n@");

                    for (String anItemDisplayName : itemDisplayName) {
                        serializedItemStack.append(escapeItemDisplayName(anItemDisplayName)).append("=");
                    }
                }

                // Item descriptions
                if (is.getItemMeta().getLore() != null) {
                    List<String> itemLores = is.getItemMeta().getLore();
                    serializedItemStack.append(":l@");

                    for (String itemLore : itemLores)
                        serializedItemStack.append(escapeItemDisplayName(itemLore)).append("=");

                }

                // Slot where the itemstack is stored
                serialization.append(entry.getKey()).append("#").append(serializedItemStack).append(";");
            }
        }

        return serialization.toString();
    }

    /**
     * Build a string with a map of ItemStacks. Base64 version.
     *
     * @param items Map of ItemStacks to convert.
     * @return The ItemStacks formatted to string.
     */
    private static String base64Serialization(ConcurrentMap<Integer, ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.size());

            for (ConcurrentMap.Entry<Integer, ItemStack> entry : items.entrySet()) {
                dataOutput.writeInt(entry.getKey());
                dataOutput.writeObject(entry.getValue());
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot serialize the items list", e);
            return "";
        }
    }

    /**
     * Restore the map of ItemStacks from a formatted string. Experimental version.
     *
     * @param data String to parse.
     * @return Generated map of Itemstacks.
     */
    private static ConcurrentMap<Integer, ItemStack> experimentalDeserialization(String data) {
        String[] serializedBlocks = data.split("(?<!\\\\);");
        ConcurrentMap<Integer, ItemStack> items = new ConcurrentHashMap<>();

        // Parse every item in the string
        for (int i = 1; i < serializedBlocks.length; i++) {
            String[] serializedBlock = serializedBlocks[i].split("(?<!\\\\)#");
            int stackPosition = Integer.parseInt(serializedBlock[0]);

            ItemStack is = null;
            boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split("(?<!\\\\):");
            for (String itemInfo : serializedItemStack) {
                String[] itemAttribute = itemInfo.split("(?<!\\\\)@");
                // Item type
                if (itemAttribute[0].equals("t")) {
                    String value = itemAttribute[1];
                    Material material;

                    // Material ids is an old way to store items, now we use material names (more reliable).
                    if (StringUtils.isNumeric(value)) {
                        material = CompatibilityHelper.materialFromId(Integer.parseInt(value));
                    } else {
                        material = CompatibilityHelper.matchMaterial(value);
                    }

                    if (material == null) {
                        continue;
                    }

                    is = new ItemStack(material);
                    createdItemStack = true;
                } else
                    // Item durability
                    if (itemAttribute[0].equals("d") && createdItemStack) {
                        is.setDurability(Short.parseShort(itemAttribute[1]));
                    } else
                        // Itemstack amount
                        if (itemAttribute[0].equals("a") && createdItemStack) {
                            is.setAmount(Integer.parseInt(itemAttribute[1]));
                        } else
                            // Enchantments
                            if (itemAttribute[0].equals("e") && createdItemStack) {
                                Enchantment enchantment = CompatibilityHelper.enchantmentFromString(itemAttribute[1]);
                                int level = Integer.parseInt(itemAttribute[2]);

                                is.addUnsafeEnchantment(enchantment, level);
                            } else
                                // Itemstack display name
                                if (itemAttribute[0].equals("n") && createdItemStack) {
                                    ItemMeta meta = is.getItemMeta();
                                    String[] displayName = itemAttribute[1].split("(?<!\\\\)=");
                                    StringBuilder finalName = new StringBuilder();

                                    for (int m = 0; m < displayName.length; m++) {
                                        if (m == displayName.length - 1)
                                            finalName.append(displayName[m]);
                                        else
                                            finalName.append(displayName[m]).append(" ");
                                    }

                                    finalName = new StringBuilder(finalName.toString().replaceAll("\\\\", ""));

                                    meta.setDisplayName(finalName.toString());
                                    is.setItemMeta(meta);
                                } else
                                    // Item description
                                    if (itemAttribute[0].equals("l") && createdItemStack) {
                                        ItemMeta meta = is.getItemMeta();
                                        String[] lore = itemAttribute[1].split("(?<!\\\\)=");
                                        List<String> itemLore = new ArrayList<>();

                                        for (String l : lore)
                                            itemLore.add(l.replaceAll("\\\\", ""));

                                        meta.setLore(itemLore);
                                        is.setItemMeta(meta);
                                    }
            }

            // Put the created itemstack in the map
            items.put(stackPosition, is);
        }

        return items;
    }

    /**
     * Restore the map of ItemStacks from a formatted string. Base64 version.
     *
     * @param data String to parse.
     * @return Generated map of Itemstacks.
     */
    private static ConcurrentMap<Integer, ItemStack> base64Deserialization(String data) {
        ConcurrentMap<Integer, ItemStack> items = new ConcurrentHashMap<>();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int mapSize = dataInput.readInt();
            int pos;
            ItemStack item;

            for (int i = 0; i < mapSize; i++) {
                pos = dataInput.readInt();
                item = (ItemStack) dataInput.readObject();

                items.put(pos, item);
            }

            dataInput.close();
        } catch (IOException | ClassNotFoundException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot deserialize the items string", e);
        }

        return items;
    }

    /**
     * Escape specials characters from the display name to avoid bug.
     *
     * @param displayName The display name to securize.
     * @return The formatted ItemStack display name.
     */
    private static String escapeItemDisplayName(String displayName) {
        StringBuilder sb = new StringBuilder();

        for (char c : displayName.toCharArray()) {
            switch (c) {
                case ';':
                case ':':
                case '@':
                case '=':
                case '#':
                case '\\':
                    sb.append("\\\\");
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

}
