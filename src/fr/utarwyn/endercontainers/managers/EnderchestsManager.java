package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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

    @SuppressWarnings("deprecation")
    public void openPlayerMainMenu(Player player, Player playerToSpec) {
        Player mainPlayer = player;
        if (playerToSpec != null) player = playerToSpec;

        if(playerToSpec != null){
            if(playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())){
                if (!CoreUtils.playerHasPerm(mainPlayer, "command.global")) {
                    PluginMsg.doesNotHavePerm(mainPlayer);
                    return;
                }
            }else{
                if (!CoreUtils.playerHasPerm(mainPlayer, "command.openchests")) {
                    PluginMsg.doesNotHavePerm(mainPlayer);
                    return;
                }
            }
        }

        int availableEnderchests = EnderChestUtils.getPlayerAvailableEnderchests(player);

        // Update player slots (in BDD or on disk)
        EnderContainers.getEnderchestsManager().savePlayerInfo(player);

        if(availableEnderchests == 1){
            mainPlayer.openInventory(player.getEnderChest());
            return;
        }

        int cells = (int) (Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0) * 9);
        if (cells > 6 * 9) cells = 6 * 9;
        MenuContainer menu = new MenuContainer(cells, CoreUtils.replacePlayerName(EnderContainers.__("enderchest_main_gui_title"), player));

        HashMap<Integer, ItemStack> items = new HashMap<>();
        HashMap<Integer, Integer> sqlSlotsChests  = new HashMap<>();

        if(EnderContainers.hasMysql()){
            List<DatabaseSet> sqlResults = EnderContainers.getMysqlManager().getPlayerEnderchests(player.getUniqueId());
            for(DatabaseSet result : sqlResults){
                sqlSlotsChests.put(result.getInteger("enderchest_id"), result.getInteger("slots_used"));
            }
        }

        for (int i = 0; i < Config.maxEnderchests; i++) {
            ItemStack item = null;
            int size       = 0;

            if(EnderContainers.hasMysql()){
                if(sqlSlotsChests.containsKey(i)) size = sqlSlotsChests.get(i);
            }else {
                EnderChest ec = EnderContainers.getEnderchestsManager().getPlayerEnderchest(player, i);
                if (ec != null) size = ec.getRealSize();
            }

            if(i == 0) size = CoreUtils.getInventorySize(player.getEnderChest());

            Integer slots = EnderChestUtils.getEnderChestAllowedRows(player, i) * 9;
            if(size > slots) size = slots;

            if (!player.hasPermission(Config.enderchestOpenPerm + i) && i >= Config.defaultEnderchestsNumber) {
                item = new ItemStack(160, 1, (short) 0, (byte) 15);
            } else {
                item = new ItemStack(160, 1);

                if (size > 0)
                    item = new ItemStack(160, 1, (short) 0, (byte) 5);
                if (size >= (slots / 2))
                    item = new ItemStack(160, 1, (short) 0, (byte) 1);
                if (size == slots)
                    item = new ItemStack(160, 1, (short) 0, (byte) 14);
            }

            ItemMeta meta    = item.getItemMeta();
            String suffixDef = "(" + size + "/" + slots + ")";
            String suffix    = suffixDef;
            Boolean hasPerm  = !(!player.hasPermission(Config.enderchestOpenPerm + i) && i != 0 && i >= Config.defaultEnderchestsNumber);

            String metaTitle = ((hasPerm) ? "§a" : "§c") + EnderContainers.__("enderchest_glasspane_title");

            if (size > 0)
                suffix = "§a" + suffixDef;
            if (size >= (slots / 2))
                suffix = "§6" + suffixDef;
            if (size == slots)
                suffix = "§4" + suffixDef;
            if (size == 0)
                suffix = "§r" + suffixDef;

            metaTitle = metaTitle.replace("%num%", String.valueOf(i + 1)).replace("%suffix%", suffix);

            if (!hasPerm) {
                meta.setDisplayName(metaTitle);
                meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_locked")));
            }else{
                meta.setDisplayName(metaTitle);

                if (size >= slots) meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_inventoryfull")));
            }

            if(playerToSpec != null && size == 0) {
                if (!playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())) {
                    if (!player.hasPermission(Config.enderchestOpenPerm + i) && i >= Config.defaultEnderchestsNumber)
                        meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_empty"), EnderContainers.__("enderchest_player_denied")));
                    else
                        meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_empty"), EnderContainers.__("enderchest_show_contents")));
                }
            }else if(playerToSpec != null && !playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())){
                if (!player.hasPermission(Config.enderchestOpenPerm + i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_player_denied")));
                else
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_show_contents")));
            }

            item.setItemMeta(meta);

            items.put(i, item);
        }

        menu.setItems(items);
        mainPlayer.openInventory(menu.getInventory());
    }
    public void openOfflinePlayerMainMenu(Player player, String playername){
        if(Bukkit.getPlayer(playername) != null){
            openPlayerMainMenu(player, Bukkit.getPlayer(playername));
            return;
        }

        HashMap<Integer, Integer> accesses = EnderChestUtils.getPlayerAccesses(playername);
        UUID uuid   = null;
        String file = "";

        if(!Bukkit.getOfflinePlayer(playername).hasPlayedBefore() || (!EnderContainers.hasMysql() && !EnderContainers.getConfigClass().isConfigurationSection("players.yml", playername))){
            CoreUtils.errorMessage(player, EnderContainers.__("enderchest_player_never_connected"));
            return;
        }

        if(!EnderContainers.hasMysql()) {
            uuid = UUID.fromString(EnderContainers.getConfigClass().getString("players.yml", playername + ".uuid"));
            file = Config.saveDir + uuid.toString() + ".yml";
        }else
            uuid = EnderContainers.getMysqlManager().getPlayerUUIDFromPlayername(playername);

        int cells = (int) (Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0) * 9);
        if (cells > 6 * 9) cells = 6 * 9;
        MenuContainer menu = new MenuContainer(cells, CoreUtils.replacePlayerName(EnderContainers.__("enderchest_main_gui_title"), playername));

        HashMap<Integer, ItemStack> items = new HashMap<>();
        HashMap<Integer, Integer> sqlSlotsChests  = new HashMap<>();

        if(EnderContainers.hasMysql()){
            List<DatabaseSet> sqlResults = EnderContainers.getMysqlManager().getPlayerEnderchests(uuid);
            for(DatabaseSet result : sqlResults){
                sqlSlotsChests.put(result.getInteger("enderchest_id"), result.getInteger("slots_used"));
            }
        }

        for (int i = 0; i < Config.maxEnderchests; i++) {
            ItemStack item = null;
            int size       = 0;

            if(EnderContainers.hasMysql()){
                if(sqlSlotsChests.containsKey(i)) size = sqlSlotsChests.get(i);
            }else{
                EnderContainers.getConfigClass().loadConfigFile(file);
                size = EnderContainers.getConfigClass().getInt(file, "enderchestsSize." + i);
            }

            if(i == 0) size = CoreUtils.getInventorySize(EnderContainers.getEnderchestsManager().getEnderChestOf(uuid, playername));

            Integer slots = accesses.get(i) * 9;
            if(size > slots) size = slots;

            if (!accesses.containsKey(i) && i >= Config.defaultEnderchestsNumber) {
                item = new ItemStack(160, 1, (short) 0, (byte) 15);
            } else {
                item = new ItemStack(160, 1);

                if (size > 0)
                    item = new ItemStack(160, 1, (short) 0, (byte) 5);
                if (size >= (slots / 2))
                    item = new ItemStack(160, 1, (short) 0, (byte) 1);
                if (size == slots)
                    item = new ItemStack(160, 1, (short) 0, (byte) 14);
            }

            ItemMeta meta    = item.getItemMeta();
            String suffixDef = "(" + size + "/" + slots + ")";
            String suffix    = suffixDef;

            if (size > 0)
                suffix = "§a" + suffixDef;
            if (size >= (slots / 2))
                suffix = "§6" + suffixDef;
            if (size == slots)
                suffix = "§4" + suffixDef;
            if (size == 0)
                suffix = "§r" + suffixDef;

            if (!accesses.containsKey(i) && i != 0 && i >= Config.defaultEnderchestsNumber) {
                meta.setDisplayName("§cEnderchest " + (i + 1) + " " + suffix);
                meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_locked")));
            }else{
                meta.setDisplayName("§aEnderchest " + (i + 1) + " " + suffix);

                if (size == slots) meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_inventoryfull")));
            }

            if(size == 0) {
                if (!accesses.containsKey(i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_empty"), EnderContainers.__("enderchest_player_denied")));
                else
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_empty"), EnderContainers.__("enderchest_show_contents")));
            }else{
                if (!accesses.containsKey(i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_player_denied")));
                else
                    meta.setLore(Arrays.asList(" ", EnderContainers.__("enderchest_show_contents")));
            }

            item.setItemMeta(meta);

            items.put(i, item);
        }

        menu.setItems(items);

        menu.offlineOwnerName = playername;
        menu.offlineOwnerUUID = uuid;
        player.openInventory(menu.getInventory());
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
        Integer slots = EnderChestUtils.getEnderChestAllowedRows(owner, num) * 9;

        if(ec != null && ec.lastMenuContainer != null){
            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, ec);

            int size = ec.lastMenuContainer.getInventory().getSize();

            if(size != slots){
                removeEnderChest(ec);
                ec = getPlayerEnderchest(owner, num);
            }else{
                playSoundTo(Config.openingChestSound, player);

                player.openInventory(ec.lastMenuContainer.getInventory());
                return;
            }
        }

        MenuContainer menu = new MenuContainer(slots, CoreUtils.replaceEnderchestNum(EnderContainers.__("enderchest_gui_title"), (num + 1), owner));

        if(ec == null){
            PluginMsg.enderchestUnknown(player, num);
            return;
        }

        for (Integer index : ec.getItems().keySet()) {
            if(index + 1 > slots) continue;

            ItemStack i = ec.getItems().get(index);
            menu.setItem(i, index);
        }

        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        playSoundTo(Config.openingChestSound, player);
        player.openInventory(menu.getInventory());

        ec.lastMenuContainer = menu;
    }
    public void openOfflinePlayerEnderChest(Integer num, Player player, UUID uuid, String playername){
        if(player.getName().equals(playername)){
            CoreUtils.errorMessage(player, "You can't open one of your own enderchests ! Please retry.");
            return;
        }

        if(Bukkit.getPlayer(playername) != null){
            openPlayerEnderChest(num, player, Bukkit.getPlayer(playername));
            return;
        }

        if(num == 0){
            Inventory inv = EnderContainers.getEnderchestsManager().getEnderChestOf(uuid, playername);
            if(inv != null) player.openInventory(inv);
            return;
        }

        HashMap<Integer, Integer> accesses = EnderChestUtils.getPlayerAccesses(playername);

        if(!accesses.containsKey(num) && num >= Config.defaultEnderchestsNumber) return;
        if(offlineEnderchestsOpened.containsKey(playername) && offlineEnderchestsOpened.get(playername).containsKey(num)){
            EnderChest lastEnderChest = offlineEnderchestsOpened.get(playername).get(num);
            player.openInventory(lastEnderChest.lastMenuContainer.getInventory());

            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, lastEnderChest);

            return;
        }

        EnderChest ec = getPlayerEnderchest(uuid, playername, num);
        Integer slots = accesses.get(num) * 9;

        if(ec != null && ec.lastMenuContainer != null){
            if (!offlineEnderchestsOpened.containsKey(playername)){
                offlineEnderchestsOpened.put(playername, new HashMap<Integer, EnderChest>());
            }
            if(!offlineEnderchestsOpened.get(playername).containsKey(num))
                offlineEnderchestsOpened.get(playername).put(num, ec);

            if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
            enderchestsOpens.put(player, ec);

            playSoundTo(Config.openingChestSound, player);

            player.openInventory(ec.lastMenuContainer.getInventory());
            return;
        }

        MenuContainer menu = new MenuContainer(slots, CoreUtils.replaceEnderchestNum(EnderContainers.__("enderchest_gui_title"), (num + 1), playername));

        if(ec == null){
            PluginMsg.enderchestUnknown(player, num);
            return;
        }

        for (Integer index : ec.getItems().keySet()) {
            if(index + 1 > slots) continue;

            ItemStack i = ec.getItems().get(index);
            menu.setItem(i, index);
        }

        if (!offlineEnderchestsOpened.containsKey(playername))
            offlineEnderchestsOpened.put(playername, new HashMap<Integer, EnderChest>());
        if(!offlineEnderchestsOpened.get(playername).containsKey(num))
            offlineEnderchestsOpened.get(playername).put(num, ec);
        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        playSoundTo(Config.openingChestSound, player);
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

    private void playSoundTo(String soundName, Player player){
        if(CoreUtils.soundExists(soundName))
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1F, 1F);
        else
            CoreUtils.log("§cThe sound §6" + soundName + "§c doesn't exists. Please change it in the config.", true);
    }
}
