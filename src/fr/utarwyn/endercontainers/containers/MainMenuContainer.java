package fr.utarwyn.endercontainers.containers;

import fr.utarwyn.endercontainers.EnderChest;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class MainMenuContainer implements InventoryHolder {

    private int rows = 3;
    private String title = "";
    private HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();

    private EnderChest.EnderChestOwner containerOwner;


    public MainMenuContainer(int rows, String title) {
        this.rows = rows;
        this.title = title;
    }

    public EnderChest.EnderChestOwner getOwner(){
        return this.containerOwner;
    }
    public void setOwner(EnderChest.EnderChestOwner owner){
        this.containerOwner = owner;
    }

    public HashMap<Integer, ItemStack> getItems(){
        return this.items;
    }
    public Integer getSize(){
        return this.rows * 9;
    }

    public void setItems(HashMap<Integer, ItemStack> items) {
        this.items = items;
    }
    public void addItem(ItemStack i) {
        if (!items.containsValue(i))
            items.put(items.size(), i);
    }
    public void setItem(ItemStack i, Integer index) {
        if (!items.containsKey(index))
            items.put(index, i);
    }
    public void removeItem(Integer index) {
        if (items.containsKey(index))
            items.remove(index);
    }
    public void clear(){
        this.items.clear();
    }


    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, this.getSize(), title);

        for (Integer index : items.keySet()) {
            ItemStack i = items.get(index);
            inv.setItem(index, i);
        }
        return inv;
    }
}