package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.ItemSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.UUID;

public class EnderChest {

    private Integer num = -1;
    private Player owner;

    public String ownerName;
    public UUID ownerUUID;

    public MenuContainer lastMenuContainer;

    private HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();


    public EnderChest(Integer num, Player owner) {
        this.num = num;
        this.owner = owner;

        load();
    }
    public EnderChest(Integer num, String ownerName, UUID ownerUUID) {
        this.num = num;
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;

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

    public int getRealSize(){
        int n = 0;

        for(Integer index : getItems().keySet()){
            ItemStack item = getItems().get(index);
            if(item != null) n++;
        }

        return n;
    }

    public void clearItems() {
        this.items.clear();
    }

    public void load() {
        if ((owner == null && ownerName == null) && num == -1) return;

        if(EnderContainers.hasMysql()){
            this.loadFromMysql();
            return;
        }

        UUID uuid = (owner != null) ? owner.getUniqueId() : ownerUUID;

        String file = Config.saveDir + uuid.toString() + ".yml";
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
        if ((owner == null && ownerName == null) && num == -1) return;

        if(EnderContainers.hasMysql()){
            this.saveToMysql();
            return;
        }

        UUID uuid         = (owner != null) ? owner.getUniqueId() : ownerUUID;
        String playername = (owner != null) ? owner.getName() : ownerName;

        String file = Config.saveDir + uuid.toString() + ".yml";
        String path = "enderchests.enderchest" + num;
        EnderContainers.getConfigClass().loadConfigFile(file);
        int count = 0;

        if (!EnderContainers.getConfigClass().isConfigurationSection(file, "enderchests"))
            EnderChestUtils.initatePlayerFile(file, playername);

        EnderContainers.getConfigClass().removePath(file, path);

        EnderContainers.getConfigClass().setAutoSaving = false;

        for (Integer index : items.keySet()) {
            ItemStack i = items.get(index);

            EnderContainers.getConfigClass().set(file, path + "." + index, i);

            if (i != null)
                count++;
        }

        // Save current timestamp & current chest size
        EnderContainers.getConfigClass().set(file, "lastsaved", System.currentTimeMillis() + "");
        EnderContainers.getConfigClass().set(file, "enderchestsSize." + num, count);

        EnderContainers.getConfigClass().setAutoSaving = true;
        EnderContainers.getConfigClass().saveConfig(file);


        CoreUtils.log(playername + "'s enderchest saved ! (" + count + " items)");
    }

    public void loadFromMysql(){
        UUID uuid       = (owner != null) ? owner.getUniqueId() : ownerUUID;
        DatabaseSet set = EnderContainers.getMysqlManager().getPlayerEnderchest(uuid, num);

        if(set != null){
            String rawItems = set.getString("items");
            HashMap<Integer, ItemStack> items = ItemSerializer.stringToItems(rawItems);

            this.items = items;
        }
    }
    public void saveToMysql(){
        UUID uuid          = (owner != null) ? owner.getUniqueId() : ownerUUID;
        Integer slotsUsed  = 0;

        for(Integer slot : items.keySet()){
            ItemStack item = items.get(slot);
            if(item == null) continue;

            slotsUsed++;
        }

        EnderContainers.getMysqlManager().savePlayerEnderchest(uuid, num, slotsUsed, ItemSerializer.itemsToString(items));
    }
}
