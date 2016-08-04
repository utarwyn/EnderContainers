package fr.utarwyn.endercontainers.api;

import fr.utarwyn.endercontainers.containers.EnderChestContainer;
import org.bukkit.entity.Player;

@SuppressWarnings({"WeakerAccess", "unused"})
public class EnderChest {

    private fr.utarwyn.endercontainers.EnderChest pluginEnderChest;

    public EnderChest fromPluginChest(fr.utarwyn.endercontainers.EnderChest enderChest){
        this.pluginEnderChest = enderChest;
        return (enderChest != null) ? this : null;
    }


    public fr.utarwyn.endercontainers.EnderChest.EnderChestOwner getOwner(){
        return this.pluginEnderChest.getOwner();
    }

    public Integer getNumber(){
        return this.pluginEnderChest.getNum();
    }

    public EnderChestContainer getContainer(){
        return this.pluginEnderChest.getContainer();
    }


    public void openFor(Player player){
        EnderContainersAPI.openEnderChestFor(this, player);
    }
}
