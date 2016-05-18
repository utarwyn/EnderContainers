package fr.utarwyn.endercontainers.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {

    public static String itemsToString (HashMap<Integer, ItemStack> items){
        String serialization = items.size() + ";";

        for (Integer slot : items.keySet()) {
            ItemStack is = items.get(slot);

            if (is != null) {
                String serializedItemStack = "";

                String isType = String.valueOf(is.getType().getId());
                serializedItemStack += "t@" + isType;

                if (is.getDurability() != 0) {
                    String isDurability = String.valueOf(is.getDurability());
                    serializedItemStack += ":d@" + isDurability;
                }

                if (is.getAmount() != 1) {
                    String isAmount = String.valueOf(is.getAmount());
                    serializedItemStack += ":a@" + isAmount;
                }

                Map<Enchantment,Integer> isEnch = is.getEnchantments();
                if (isEnch.size() > 0) {
                    for (Map.Entry<Enchantment,Integer> ench : isEnch.entrySet()){
                        serializedItemStack += ":e@" + ench.getKey().getId() + "@" + ench.getValue();
                    }
                }

                if (is.getItemMeta().getDisplayName() != null) {
                    String[] itemDisplayName = is.getItemMeta().getDisplayName().split(" ");
                    serializedItemStack += ":n@";
                    for (int m = 0; m < itemDisplayName.length; m++){
                        serializedItemStack += itemDisplayName[m] + "=";
                    }
                }

                serialization += slot + "#" + serializedItemStack + ";";
            }
        }
        return serialization;
    }

    public static HashMap<Integer, ItemStack> stringToItems(String invString) {
        String[] serializedBlocks = invString.split(";");
        HashMap<Integer, ItemStack> items = new HashMap<>();

        for (int i = 1; i < serializedBlocks.length; i++) {
            String[] serializedBlock = serializedBlocks[i].split("#");
            int stackPosition = Integer.valueOf(serializedBlock[0]);

            ItemStack is = null;
            Boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split(":");
            for (String itemInfo : serializedItemStack) {
                String[] itemAttribute = itemInfo.split("@");
                if (itemAttribute[0].equals("t")) {
                    is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
                    createdItemStack = true;
                } else if (itemAttribute[0].equals("d") && createdItemStack) {
                    is.setDurability(Short.valueOf(itemAttribute[1]));
                } else if (itemAttribute[0].equals("a") && createdItemStack) {
                    is.setAmount(Integer.valueOf(itemAttribute[1]));
                } else if (itemAttribute[0].equals("e") && createdItemStack) {
                    is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
                } else if (itemAttribute[0].equals("n") && createdItemStack) {
                    ItemMeta meta = is.getItemMeta();
                    String[] displayName = itemAttribute[1].split("=");
                    String finalName = "";

                    for (int m = 0; m < displayName.length; m++) {
                        if (m == displayName.length - 1)
                            finalName += displayName[m];
                        else
                            finalName += displayName[m] + " ";
                    }

                    meta.setDisplayName(finalName);
                    is.setItemMeta(meta);
                }
            }

            items.put(stackPosition, is);
        }

        return items;
    }

}
