package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class EnderchestsManager {

    public ArrayList<EnderChest> enderchests = new ArrayList<>();
    public HashMap<Player, EnderChest> enderchestsOpens = new HashMap<>();
    public HashMap<Player, Player> lastEnderchestOpened = new HashMap<>();

    public HashMap<String, HashMap<Integer, EnderChest>> offlineEnderchestsOpened = new HashMap<>();
    public HashMap<String, Inventory> offlineVanillaEnderchestOpened = new HashMap<>();

    public void addEnderChest(EnderChest ec) {
        if (!enderchests.contains(ec))
            enderchests.add(ec);
    }

    public void removeEnderChest(EnderChest ec) {
        if (enderchests.contains(ec))
            enderchests.remove(ec);
    }

    public EnderChest getPlayerEnderchest(Player player, Integer num) {
        return getPlayerEnderchest(player.getUniqueId(), player.getName(), num);
    }
    public EnderChest getPlayerEnderchest(UUID playerUUID, String playerName, Integer num) {
        EnderChest ec = null;

        for (EnderChest enderchest : enderchests) {
            if (enderchest.getOwner() != null && enderchest.getOwner().getUniqueId().equals(playerUUID) && num.equals(enderchest.getNum()))
                ec = enderchest;
            if(enderchest.ownerName != null && enderchest.ownerUUID != null && enderchest.ownerName.equalsIgnoreCase(playerName) && enderchest.ownerUUID.equals(playerUUID) && num.equals(enderchest.getNum()))
                ec = enderchest;
        }
        if (ec == null){
            ec = new EnderChest(num, playerName, playerUUID);
            addEnderChest(ec);
        }


        return ec;
    }


    public void openPlayerEnderChest(Integer num, Player player, Player playerToSpec) {
        Player owner = player;
        if (playerToSpec != null) owner = playerToSpec;

        if (num > Config.maxEnderchests - 1) {
            PluginMsg.cannotOpenEnderchest(player);
            return;
        }

        if (!player.hasPermission(Config.enderchestOpenPerm + num) && num >= Config.defaultEnderchestsNumber) {
            PluginMsg.doesNotHavePerm(player);
            return;
        }

        EnderChest ec = getPlayerEnderchest(owner, num);
        Integer slots = (Config.allowDoubleChest && CoreUtils.playerHasPerm(owner, "doublechest." + num)) ? 54 : 27;

        if(ec != null && ec.lastMenuContainer != null){
            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, ec);

            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
            player.openInventory(ec.lastMenuContainer.getInventory());
            return;
        }

        MenuContainer menu = new MenuContainer(slots, CoreUtils.replaceEnderchestNum(Config.enderchestTitle, (num + 1), owner));

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

        ec.lastMenuContainer = menu;
    }
    public void openOfflinePlayerEnderChest(Integer num, Player player, UUID uuid, String playername){
        if(Bukkit.getPlayer(playername) != null){
            openPlayerEnderChest(num, player, Bukkit.getPlayer(playername));
            return;
        }

        if(num == 0){
            Inventory inv = EnderContainers.getEnderchestsManager().getEnderChestOf(uuid, playername);
            if(inv != null) player.openInventory(inv);
            return;
        }

        HashMap<Integer, Boolean> accesses = EnderChestUtils.getPlayerAccesses(playername);

        if(!accesses.containsKey(num) && num >= Config.defaultEnderchestsNumber) return;
        if(offlineEnderchestsOpened.containsKey(playername) && offlineEnderchestsOpened.get(playername).containsKey(num)){
            EnderChest lastEnderChest = offlineEnderchestsOpened.get(playername).get(num);
            player.openInventory(lastEnderChest.lastMenuContainer.getInventory());

            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, lastEnderChest);

            return;
        }

        EnderChest ec = getPlayerEnderchest(uuid, playername, num);
        Integer slots = (Config.allowDoubleChest && accesses.containsKey(num) && accesses.get(num)) ? 54 : 27;

        if(ec != null && ec.lastMenuContainer != null){
            if (!offlineEnderchestsOpened.containsKey(playername)){
                offlineEnderchestsOpened.put(playername, new HashMap<Integer, EnderChest>());
            }
            if(!offlineEnderchestsOpened.get(playername).containsKey(num))
                offlineEnderchestsOpened.get(playername).put(num, ec);

            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, ec);

            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
            player.openInventory(ec.lastMenuContainer.getInventory());
            return;
        }

        MenuContainer menu = new MenuContainer(slots, CoreUtils.replaceEnderchestNum(Config.enderchestTitle, (num + 1), playername));

        if(ec == null){
            PluginMsg.enderchestUnknown(player, num);
            return;
        }

        for (Integer index : ec.getItems().keySet()) {
            ItemStack i = ec.getItems().get(index);
            menu.setItem(i, index);
        }

        if (!offlineEnderchestsOpened.containsKey(playername))
            offlineEnderchestsOpened.put(playername, new HashMap<Integer, EnderChest>());
        if(!offlineEnderchestsOpened.get(playername).containsKey(num))
            offlineEnderchestsOpened.get(playername).put(num, ec);
        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
        player.openInventory(menu.getInventory());

        menu.offlineOwnerUUID = uuid;menu.offlineOwnerName = playername;
        ec.lastMenuContainer = menu;
    }


    public Player getLastEnderchestOpened(Player player){
        return this.lastEnderchestOpened.get(player);
    }
    public void setLastEnderchestOpened(Player player, Player playerOwner){
        if(this.lastEnderchestOpened.containsKey(player)) this.lastEnderchestOpened.remove(player);
        this.lastEnderchestOpened.put(player, playerOwner);
    }

    public Inventory getEnderChestOf(UUID uuid, String playername){
        if(Bukkit.getPlayer(playername) != null)
            return Bukkit.getPlayer(playername).getEnderChest();

        if(offlineVanillaEnderchestOpened.containsKey(playername))
            return offlineVanillaEnderchestOpened.get(playername);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playername);

        if(!offlinePlayer.hasPlayedBefore())
            return null;

        Player target = NMSHacks.getPlayerObjectOfOfflinePlayer(playername, uuid, NMSHacks.isServerPost16());
        assert target != null;
        target.loadData();

        if(!offlineVanillaEnderchestOpened.containsKey(playername))
            offlineVanillaEnderchestOpened.put(playername, target.getEnderChest());

        return target.getEnderChest();
    }
    public void savePlayerInfo(Player p){
        if(!EnderContainers.hasMysql()){
            ConfigClass cc = EnderContainers.getConfigClass();

            cc.loadConfigFile("players.yml");

            cc.set("players.yml", p.getName() + ".uuid", p.getUniqueId().toString());
            cc.set("players.yml", p.getName() + ".accesses", EnderChestUtils.playerAvailableEnderchestsToString(p));
        }
    }
}
