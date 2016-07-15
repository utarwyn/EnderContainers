package fr.utarwyn.endercontainers.api;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class EnderContainersAPI {

    public static Plugin getPlugin(){
        return EnderContainers.getInstance();
    }


    public static EnderChest getPlayerEnderChest(Player player, Integer number){
        return new EnderChest().fromPluginChest(EnderContainers.getEnderchestsManager().getPlayerEnderchest(player, number));
    }


    public static void openEnderChestFor(EnderChest enderChest, Player player){
        if(enderChest == null){
            throw new NullPointerException("EnderChest seems to be null");
        }
        if(player == null){
            throw new NullPointerException("Player seems to be null");
        }

        Player owner = enderChest.getOwner();

        if(owner == null){
            throw new NullPointerException("EnderChest's owner seems to be null");
        }
        if(!owner.isOnline()){
            throw new IllegalAccessError("EnderChest's owner have to be connected!");
        }

        EnderContainers.getEnderchestsManager().openPlayerEnderChest(enderChest.getNumber(), owner, player);
    }

    public static void openEnderChestFor(EnderChest enderChest, Collection<Player> players){
        for(Player player : players) openEnderChestFor(enderChest, player);
    }
}
