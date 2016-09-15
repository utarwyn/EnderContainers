package fr.utarwyn.endercontainers.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSerializer {

    public static String itemsToString (HashMap<Integer, ItemStack> items){
        String serialization = new String((items.size() + ";").getBytes(), Charset.forName("UTF-8"));

        for (Integer slot : items.keySet()) {
            ItemStack is = items.get(slot);

            if (is != null) {
                String serializedItemStack = new String("".getBytes(), Charset.forName("UTF-8"));

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
                    String[] itemDisplayName = new String(is.getItemMeta().getDisplayName().getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")).split(" ");
                    serializedItemStack += ":n@";

                    for (String anItemDisplayName : itemDisplayName)
                        serializedItemStack += escapeItemDisplayName(anItemDisplayName) + "=";

                }

                if (is.getItemMeta().getLore() != null) {
                    List<String> itemLores = is.getItemMeta().getLore();
                    serializedItemStack += ":l@";

                    for (String itemLore : itemLores)
                        serializedItemStack += escapeItemDisplayName(itemLore) + "=";

                }

                serialization += slot + "#" + serializedItemStack + ";";
            }
        }

        return serialization;
    }

    public static HashMap<Integer, ItemStack> stringToItems(String invString) {
        String[] serializedBlocks = invString.split("(?<!\\\\);");
        HashMap<Integer, ItemStack> items = new HashMap<>();

        for (int i = 1; i < serializedBlocks.length; i++) {
            String[] serializedBlock = serializedBlocks[i].split("(?<!\\\\)#");
            int stackPosition = Integer.valueOf(serializedBlock[0]);

            ItemStack is = null;
            Boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split("(?<!\\\\):");
            for (String itemInfo : serializedItemStack) {
                String[] itemAttribute = itemInfo.split("(?<!\\\\)@");
                if (itemAttribute[0].equals("t")) {
                    is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
                    createdItemStack = true;
                } else if (itemAttribute[0].equals("d") && createdItemStack) {
                    is.setDurability(Short.valueOf(itemAttribute[1]));
                } else if (itemAttribute[0].equals("a") && createdItemStack) {
                    is.setAmount(Integer.valueOf(itemAttribute[1]));
                } else if (itemAttribute[0].equals("e") && createdItemStack) {
                    Enchantment enchantment = Enchantment.getById(Integer.valueOf(itemAttribute[1]));
                    Integer level = Integer.valueOf(itemAttribute[2]);

                    is.addUnsafeEnchantment(enchantment, level);
                } else if (itemAttribute[0].equals("n") && createdItemStack) {
                    ItemMeta meta = is.getItemMeta();
                    String[] displayName = itemAttribute[1].split("(?<!\\\\)=");
                    String finalName = "";

                    for (int m = 0; m < displayName.length; m++) {
                        if (m == displayName.length - 1)
                            finalName += displayName[m];
                        else
                            finalName += displayName[m] + " ";
                    }

                    finalName = finalName.replaceAll("\\\\", "");

                    meta.setDisplayName(finalName);
                    is.setItemMeta(meta);
                } else if (itemAttribute[0].equals("l") && createdItemStack) {
                    ItemMeta meta = is.getItemMeta();
                    String[] lore = itemAttribute[1].split("(?<!\\\\)=");
                    List<String> itemLore = new ArrayList<>();

                    for(String l : lore)
                        itemLore.add(l.replaceAll("\\\\", ""));

                    meta.setLore(itemLore);
                    is.setItemMeta(meta);
                }
            }

            items.put(stackPosition, is);
        }

        return items;
    }


    private static String escapeItemDisplayName(String displayName){
        StringBuilder sb = new StringBuilder();

        for (char c : displayName.toCharArray()){
            switch(c){
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
