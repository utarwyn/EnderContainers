package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.containers.EnderChestContainer;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.ItemSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class EnderChest {

    private Integer num = -1;
    private EnderChestOwner owner;

    private EnderChestContainer container;


    public EnderChest(EnderChestOwner owner, Integer num) {
        this.owner = owner;
        this.num   = num;

        load();
    }
    public EnderChest(Player owner, Integer num){
        this(new EnderChestOwner(owner), num);
    }
    public EnderChest(String owner, Integer num){
        this(new EnderChestOwner(owner), num);
    }


    public Integer getNum() {
        return this.num;
    }
    public EnderChestOwner getOwner() {
        return this.owner;
    }
    public EnderChestContainer getContainer(){
        return this.container;
    }

    public void setNewOwner(EnderChestOwner owner){
        this.owner = owner;
    }


    private void load() {
        if (!getOwner().exists() || num == -1) return;

        // Generate menu container
        generateMenuContainer();

        if(EnderContainers.hasMysql()){
            this.loadFromMysql();
            return;
        }

        // Load items from disk
        UUID uuid   = getOwner().getPlayerUniqueId();
        String file = Config.saveDir + uuid.toString() + ".yml";
        String path = "enderchests.enderchest" + num;

        EnderContainers.getConfigClass().loadConfigFile(file);
        if (!EnderContainers.getConfigClass().isConfigurationSection(file, path)) return;

        HashMap<Integer, ItemStack> items = new HashMap<>();
        for (String key : EnderContainers.getConfigClass().getConfigurationSection(file, path).getKeys(false)) {
            if (!StringUtils.isNumeric(key)) continue;
            Integer index = Integer.parseInt(key);
            ItemStack i = EnderContainers.getConfigClass().getItemStack(file, path + "." + index);

            items.put(index, i);
        }
        getContainer().setItems(items);
    }
    private void generateMenuContainer(){
        if(getNum() == 0){
            if(getOwner().ownerIsOnline()){
                container = new EnderChestContainer(getOwner().getPlayer().getEnderChest());
            }else{
                Inventory inventory = EnderChestUtils.getVanillaEnderChestOf(getOwner().getPlayerName(), getOwner().getPlayerUniqueId());
                assert inventory != null;

                container = new EnderChestContainer(inventory);
            }
        }else{
            Integer rows = EnderChestUtils.getAllowedRowsFor(this);
            String title = CoreUtils.replaceEnderchestNum(EnderContainers.__("enderchest_gui_title"), this.getNum(), getOwner().getPlayerName());

            this.container = new EnderChestContainer(rows, title);
        }
    }
    public void save() {
        if (!getOwner().exists() || num == -1 || getContainer() == null) return;

        if(!getOwner().ownerIsOnline() && num == 0){
            EnderChestUtils.saveVanillaEnderChest(this);
            return;
        }
        if(num == 0) return;

        if(EnderContainers.hasMysql()){
            this.saveToMysql();
            return;
        }

        UUID uuid         = getOwner().getPlayerUniqueId();
        String playername = getOwner().getPlayerName();

        String file = Config.saveDir + uuid.toString() + ".yml";
        String path = "enderchests.enderchest" + num;
        EnderContainers.getConfigClass().loadConfigFile(file);
        int count = 0;

        if (!EnderContainers.getConfigClass().isConfigurationSection(file, "enderchests"))
            EnderChestUtils.initatePlayerFile(file, playername);

        EnderContainers.getConfigClass().removePath(file, path);
        EnderContainers.getConfigClass().setAutoSaving = false;

        for (Integer index : getContainer().getItems().keySet()) {
            ItemStack i = getContainer().getItems().get(index);

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

    private void loadFromMysql(){
        UUID uuid       = getOwner().getPlayerUniqueId();
        DatabaseSet set = EnderContainers.getMysqlManager().getPlayerEnderchest(uuid, num);

        if(set != null){
            String rawItems = set.getString("items");
            getContainer().setItems(ItemSerializer.stringToItems(rawItems));
        }
    }
    private void saveToMysql(){
        UUID uuid          = owner.getPlayerUniqueId();
        Integer slotsUsed  = 0;

        for(Integer slot : getContainer().getItems().keySet()){
            ItemStack item = getContainer().getItems().get(slot);
            if(item == null) continue;

            slotsUsed++;
        }

        String stringItems = ItemSerializer.itemsToString(getContainer().getItems());
        EnderContainers.getMysqlManager().savePlayerEnderchest(uuid, num, slotsUsed, stringItems);
    }



    public static class EnderChestOwner{
        private Player owner;
        private String ownerName;

        public EnderChestOwner(Player owner){
            this.owner = owner;
            this.ownerName = owner.getName();
        }
        public EnderChestOwner(String ownerName){
            this.ownerName = ownerName;
        }

        public boolean exists(){
            return (this.owner != null || this.ownerName != null);
        }

        public boolean ownerIsOnline(){
            return (this.owner != null && this.owner.isOnline());
        }
        public Player getPlayer(){
            return this.owner;
        }
        public String getPlayerName(){
            return this.ownerName;
        }
        public UUID getPlayerUniqueId(){
            if(ownerIsOnline()) return getPlayer().getUniqueId();
            return EnderChestUtils.getPlayerUUIDFromPlayername(this.ownerName);
        }

        @Override
        public String toString(){
            return "{exists:" + exists() + ", online:" + ownerIsOnline() + ", playername: " + getPlayerName() + ", playeruuid: " + getPlayerUniqueId() + ", player:" + ((getPlayer() != null) ? getPlayer().toString() : "null") + "}";
        }
    }
}
