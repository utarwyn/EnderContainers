package fr.utarwyn.endercontainers.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnderChest {

    private fr.utarwyn.endercontainers.EnderChest pluginEnderChest;

    public EnderChest fromPluginChest(fr.utarwyn.endercontainers.EnderChest enderChest){
        this.pluginEnderChest = enderChest;
        return (enderChest != null) ? this : null;
    }


    public Player getOwner(){
        return this.pluginEnderChest.getOwner();
    }

    public Integer getNumber(){
        return this.pluginEnderChest.getNum();
    }


    public Map<Integer, ItemStack> getItems(){
        return this.pluginEnderChest.getItems();
    }

    public void addItemAt(Integer slot, ItemStack item){
        this.pluginEnderChest.addItem(slot, item);
        this.pluginEnderChest.save();
    }

    public ItemStack getItemAt(Integer slot){
        return this.getItems().get(slot);
    }

    public void removeItemAt(Integer slot){
        if(this.getItems().containsKey(slot))
            this.getItems().remove(slot);

        this.pluginEnderChest.save();
    }

    public Inventory getInventory(){
        if(this.pluginEnderChest.lastMenuContainer == null) return null;
        return this.pluginEnderChest.lastMenuContainer.getInventory();
    }


    public void openFor(Player player){
        EnderContainersAPI.openEnderChestFor(this, player);
    }
}
