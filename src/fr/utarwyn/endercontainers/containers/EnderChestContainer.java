package fr.utarwyn.endercontainers.containers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EnderChestContainer implements InventoryHolder {

    private int rows = 3;
    private String title = "";
    private HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
    private Inventory inventory;


    public EnderChestContainer(int rows, String title) {
        this.rows = rows;
        this.title = title;
    }
    public EnderChestContainer(Inventory inventory){ // For vanilla enderchest (num == 0)
        this.inventory = inventory;
        this.rows      = inventory.getSize() / 9;

        HashMap<Integer, ItemStack> items = new HashMap<>();
        for(int i = 0; i < inventory.getSize(); i++){
            Integer index = i;
            items.put(index, inventory.getItem(index));
        }
        this.items = items;
    }


    public HashMap<Integer, ItemStack> getItems(){
        return this.items;
    }
    public Integer getSize(){
        return this.rows * 9;
    }
    public Integer getFilledSlotsNumber(){
        int n = 0;

        for(Integer index : getItems().keySet()){
            ItemStack item = getItems().get(index);
            if(item != null) n++;
        }

        return n;
    }

    public boolean hasItemAt(Integer slot){
        return getItems().containsKey(slot);
    }
    public boolean containsItem(ItemStack item){
        return getItems().containsValue(item);
    }
    public boolean containsItemWithMaterial(Material material){
        for(ItemStack item : items.values()){
            if(item.getType().equals(material))
                return true;
        }
        return false;
    }

    public void setItems(HashMap<Integer, ItemStack> items) {
        this.items = items;
        reloadInventoryItems();
    }
    public void addItem(ItemStack i) {
        if (!items.containsValue(i))
            items.put(items.size(), i);
        reloadInventoryItems();
    }
    public void setItem(ItemStack i, Integer index) {
        if (!items.containsKey(index))
            items.put(index, i);
        reloadInventoryItems();
    }
    public void removeItem(Integer index) {
        if (items.containsKey(index))
            items.remove(index);
        reloadInventoryItems();
    }
    private void clear(){
        this.items.clear();
    }

    public void refresh(){
        if(this.inventory == null) return;

        this.clear();

        HashMap<Integer, ItemStack> items = new HashMap<>();
        for(int index = 0; index < this.inventory.getSize(); index++){
            ItemStack item = this.inventory.getItem(index);
            if(item == null) continue;

            items.put(index, item);
        }

        setItems(items);
    }


    @Override
    public Inventory getInventory() {
        if(this.inventory != null && this.getSize() == this.inventory.getSize())
            return this.inventory;

        Inventory inv = Bukkit.createInventory(this, this.getSize(), title);
        this.inventory = inv;

        reloadInventoryItems();

        return inv;
    }
    private void reloadInventoryItems(){
        if(this.inventory == null) return;

        this.inventory.clear();
        for (Integer index : items.keySet()) {
            ItemStack i = items.get(index);
            if(i == null || i.getAmount() == 0) continue;

            this.inventory.setItem(index, i);
        }
    }
    public void setNewInventory(Inventory inventory){
        if(this.inventory != null){
            Object[] viewers = this.inventory.getViewers().toArray();

            for(Object entity : viewers){
                if(!(entity instanceof Player)) continue;

                Player player = (Player) entity;
                player.openInventory(inventory);
            }
        }

        this.inventory = inventory;
        reloadInventoryItems();
    }
}
