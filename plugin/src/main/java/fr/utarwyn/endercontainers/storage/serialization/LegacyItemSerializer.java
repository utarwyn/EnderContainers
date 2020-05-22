package fr.utarwyn.endercontainers.storage.serialization;

import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Serialize a list of items into a legacy string.
 * A custom serializer which is not powerful/efficient but creates tiny strings.
 * TODO remove this old and unefficient serializer.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public class LegacyItemSerializer implements ItemSerializer {

    /**
     * Escape specials characters from the display name
     * to ensure the custom format cannot be deflected.
     *
     * @param displayName display name to escape
     * @return formatted and escaped item display name
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(ConcurrentMap<Integer, ItemStack> items) {
        StringBuilder serialization = new StringBuilder(new String((items.size() + ";").getBytes(), StandardCharsets.UTF_8));

        items.forEach((index, is) -> {
            if (is == null) return;
            StringBuilder builder = new StringBuilder();

            // Item type
            String isType = String.valueOf(is.getType());
            builder.append("t@").append(isType);

            // Item durability
            if (is.getDurability() != 0) {
                String isDurability = String.valueOf(is.getDurability());
                builder.append(":d@").append(isDurability);
            }

            // Amount of the itemstack
            if (is.getAmount() != 1) {
                String isAmount = String.valueOf(is.getAmount());
                builder.append(":a@").append(isAmount);
            }

            // Enchantments
            is.getEnchantments().forEach((enchant, force) -> builder.append(":e@")
                    .append(CompatibilityHelper.enchantmentToString(enchant))
                    .append("@").append(force));

            // Display name
            if (is.getItemMeta() != null && !is.getItemMeta().getDisplayName().equals("")) {
                String[] itemDisplayName = new String(
                        is.getItemMeta().getDisplayName().getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                ).split(" ");

                builder.append(":n@");

                for (String anItemDisplayName : itemDisplayName) {
                    builder.append(escapeItemDisplayName(anItemDisplayName)).append("=");
                }
            }

            // Item descriptions
            if (is.getItemMeta() != null && is.getItemMeta().getLore() != null) {
                List<String> itemLores = is.getItemMeta().getLore();
                builder.append(":l@");

                for (String itemLore : itemLores) {
                    builder.append(escapeItemDisplayName(itemLore)).append("=");
                }
            }

            // Slot where the itemstack is stored
            serialization.append(index).append("#")
                    .append(builder).append(";");
        });

        return serialization.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> deserialize(String data) {
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

                if (!createdItemStack && itemAttribute[0].equals("t")) {
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

                } else if (createdItemStack) {
                    switch (itemAttribute[0]) {
                        // Item durability
                        case "d":
                            is.setDurability(Short.parseShort(itemAttribute[1]));
                            break;
                        // Itemstack amount
                        case "a":
                            is.setAmount(Integer.parseInt(itemAttribute[1]));
                            break;
                        // Enchantments
                        case "e":
                            Enchantment enchantment = CompatibilityHelper
                                    .enchantmentFromString(itemAttribute[1]);

                            int level = Integer.parseInt(itemAttribute[2]);
                            is.addUnsafeEnchantment(enchantment, level);
                            break;
                        // Itemstack display name
                        case "n":
                            ItemMeta meta = is.getItemMeta();
                            String[] displayName = itemAttribute[1].split("(?<!\\\\)=");
                            StringBuilder finalName = new StringBuilder();

                            for (int m = 0; m < displayName.length; m++) {
                                if (m == displayName.length - 1) {
                                    finalName.append(displayName[m]);
                                } else {
                                    finalName.append(displayName[m]).append(" ");
                                }
                            }

                            finalName = new StringBuilder(finalName.toString()
                                    .replaceAll("\\\\", ""));

                            meta.setDisplayName(finalName.toString());
                            is.setItemMeta(meta);
                            break;
                        // Item description
                        case "l":
                            meta = is.getItemMeta();
                            String[] lore = itemAttribute[1].split("(?<!\\\\)=");
                            List<String> itemLore = new ArrayList<>();

                            for (String l : lore) {
                                itemLore.add(l.replaceAll("\\\\", ""));
                            }

                            meta.setLore(itemLore);
                            is.setItemMeta(meta);
                            break;
                    }
                }
            }

            // Put the created itemstack in the map
            items.put(stackPosition, is);
        }

        return items;
    }

}
