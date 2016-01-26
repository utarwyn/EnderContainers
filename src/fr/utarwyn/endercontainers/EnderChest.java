package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EnderChest {

    private Integer num = -1;
    private Player owner;

    private HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();


    public EnderChest(Integer num, Player owner) {
        this.num = num;
        this.owner = owner;

        load();
    }

    public Integer getNum() {
        return this.num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public HashMap<Integer, ItemStack> getItems() {
        return this.items;
    }

    public void addItem(ItemStack i) {
        items.put(items.size(), i);
    }

    public void addItem(Integer index, ItemStack i) {
        if (items.containsKey(index))
            items.remove(index);

        items.put(index, i);
    }

    public void removeItem(ItemStack i) {
        if (items.containsKey(i))
            items.remove(i);
    }

    public boolean isItem(ItemStack i) {
        return items.containsKey(i);
    }

    public boolean isItemInSlot(Integer n) {
        return items.containsValue(n);
    }

    public void clearItems() {
        this.items.clear();
    }

    public void load() {
        if (owner == null || num == -1) return;
        String file = Config.saveDir + owner.getUniqueId().toString() + ".yml";
        String path = "enderchests.enderchest" + num;

        EnderContainers.getConfigClass().loadConfigFile(file);
        if (!EnderContainers.getConfigClass().isConfigurationSection(file, path)) return;
        items.clear();

        for (String key : EnderContainers.getConfigClass().getConfigurationSection(file, path).getKeys(false)) {
            if (!StringUtils.isNumeric(key)) continue;
            Integer index = Integer.parseInt(key);
            ItemStack i = EnderContainers.getConfigClass().getItemStack(file, path + "." + index);

            addItem(index, i);
        }
    }

    public void save() {
        if (owner == null || num == -1) return;
        String file = Config.saveDir + owner.getUniqueId().toString() + ".yml";
        String path = "enderchests.enderchest" + num;
        EnderContainers.getConfigClass().loadConfigFile(file);
        int count = 0;

        if (!EnderContainers.getConfigClass().isConfigurationSection(file, "enderchests"))
            EnderChestUtils.initatePlayerFile(file, owner);

        EnderContainers.getConfigClass().removePath(file, path);

        EnderContainers.getConfigClass().setAutoSaving = false;

        for (Integer index : items.keySet()) {
            ItemStack i = items.get(index);

            EnderContainers.getConfigClass().set(file, path + "." + index, i);

            if (i != null)
                count++;
        }

        // Save current timestamp
        EnderContainers.getConfigClass().set(file, "lastsaved", System.currentTimeMillis() + "");

        EnderContainers.getConfigClass().setAutoSaving = true;
        EnderContainers.getConfigClass().saveConfig(file);


        CoreUtils.log(owner.getName() + "'s enderchest saved ! (" + count + " items)");
    }
}
