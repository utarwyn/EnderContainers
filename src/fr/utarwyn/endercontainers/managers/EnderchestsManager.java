package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class EnderchestsManager {

    public ArrayList<EnderChest> enderchests = new ArrayList<EnderChest>();
    public HashMap<Player, EnderChest> enderchestsOpens = new HashMap<Player, EnderChest>();
    public HashMap<Player, Player> lastEnderchestOpened = new HashMap<Player, Player>();


    public void addEnderChest(EnderChest ec) {
        if (!enderchests.contains(ec))
            enderchests.add(ec);
    }

    public void removeEnderChest(EnderChest ec) {
        if (enderchests.contains(ec))
            enderchests.remove(ec);
    }


    public EnderChest getPlayerEnderchest(Player player, Integer num) {
        String UUID = player.getUniqueId().toString();
        EnderChest ec = null;

        for (EnderChest enderchest : enderchests) {
            if (ec.getOwner().getUniqueId().toString().equalsIgnoreCase(UUID) && num == ec.getNum())
                ec = enderchest;
        }
        if (ec == null) ec = new EnderChest(num, player);


        return ec;
    }

    public void openPlayerEnderChest(Integer num, Player player, Player playerToSpec) {
        Player owner = player;
        if (playerToSpec != null) owner = playerToSpec;

        if (num > Config.maxEnderchests - 1) {
            PluginMsg.cannotOpenEnderchest(player);
            return;
        }

        if (!player.hasPermission(Config.enderchestOpenPerm + num)) {
            PluginMsg.doesNotHavePerm(player);
            return;
        }

        EnderChest ec = getPlayerEnderchest(owner, num);
        MenuContainer menu = new MenuContainer(27, CoreUtils.replaceEnderchestNum(Config.enderchestTitle, (num + 1), owner));

        if(ec == null){
            PluginMsg.enderchestUnknown(player, num);
            return;
        }

        for (Integer index : ec.getItems().keySet()) {
            ItemStack i = ec.getItems().get(index);
            menu.setItem(i, index);
        }

        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
        player.openInventory(menu.getInventory());
    }

    public Player getLastEnderchestOpened(Player player){
        return this.lastEnderchestOpened.get(player);
    }
    public void setLastEnderchestOpened(Player player, Player playerOwner){
        if(this.lastEnderchestOpened.containsKey(player)) this.lastEnderchestOpened.remove(player);
        this.lastEnderchestOpened.put(player, playerOwner);
    }
}
