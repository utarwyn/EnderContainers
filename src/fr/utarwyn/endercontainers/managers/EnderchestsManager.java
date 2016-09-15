package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MainMenuContainer;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnderchestsManager {

    private ArrayList<EnderChest> enderchests = new ArrayList<>();
    private HashMap<Player, EnderChest> enderchestsOpens = new HashMap<>();

    private EnderChest createNewEnderChest(String playername, Integer num){
        EnderChest enderchest = new EnderChest(playername, num);

        enderchests.add(enderchest);
        return enderchest;
    }
    private EnderChest createNewEnderChest(Player player, Integer num){
        EnderChest enderchest = new EnderChest(player, num);

        enderchests.add(enderchest);
        return enderchest;
    }

    public EnderChest getPlayerEnderchestOf(Player player, Integer num) {
        for (EnderChest enderchest : enderchests) {
            if(enderchest.getOwner() == null || !enderchest.getOwner().exists()) continue;
            if(enderchest.getOwner().getPlayerName().equals(player.getName()) && num.equals(enderchest.getNum())){
                return enderchest;
            }

        }

        return createNewEnderChest(player, num);
    }
    public EnderChest getPlayerEnderchestOf(String playername, Integer num) {
        for (EnderChest enderchest : enderchests) {
            if(enderchest.getOwner() == null || !enderchest.getOwner().exists()) continue;
            if(enderchest.getOwner().getPlayerName().equals(playername) && num.equals(enderchest.getNum()))
                return enderchest;
        }

        return createNewEnderChest(playername, num);
    }

    public HashMap<Player, EnderChest> getOpenedEnderchests(){
        return this.enderchestsOpens;
    }


    public void openPlayerMainMenu(Player player, Player playerToSpec) {
        Player mainPlayer = player;
        if (playerToSpec != null && playerToSpec != player) player = playerToSpec;

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

        EnderContainers.getEnderchestsManager().savePlayerInfo(player);

        if(availableEnderchests == 1){
            mainPlayer.openInventory(player.getEnderChest());
            return;
        }

        int rows = (int) Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0);
        if (rows > 6) rows = 6;
        MainMenuContainer menu = new MainMenuContainer(rows, CoreUtils.replacePlayerName(EnderContainers.__("enderchest_main_gui_title"), player));

        HashMap<Integer, ItemStack> items        = new HashMap<>();
        HashMap<Integer, Integer> sqlSlotsChests = new HashMap<>();

        if(EnderContainers.hasMysql()){
            List<DatabaseSet> sqlResults = EnderContainers.getMysqlManager().getPlayerEnderchests(player.getUniqueId());
            if(sqlResults != null) {
                for (DatabaseSet result : sqlResults) {
                    sqlSlotsChests.put(result.getInteger("enderchest_id"), result.getInteger("slots_used"));
                }
            }
        }

        for (int i = 0; i < Config.maxEnderchests; i++) {
            ItemStack item;
            int size = 0;

            if(EnderContainers.hasMysql()){
                if(sqlSlotsChests.containsKey(i)) size = sqlSlotsChests.get(i);
            }else {
                EnderChest ec = EnderContainers.getEnderchestsManager().getPlayerEnderchestOf(player, i);
                assert ec != null;

                if (ec.getContainer() != null) size = ec.getContainer().getFilledSlotsNumber();
            }

            if(i == 0) size = CoreUtils.getInventorySize(player.getEnderChest());

            Integer slots = EnderChestUtils.getAllowedRowsFor(player, i) * 9;
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

            String metaTitle = ((hasPerm) ? "§a" : "§c") + ((i > 0) ? EnderContainers.__("enderchest_glasspane_title") : EnderContainers.__("enderchest_default_glasspane_title"));

            if (size > 0)
                suffix = "§a" + suffixDef;
            if (size >= (slots / 2))
                suffix = "§6" + suffixDef;
            if (size == slots)
                suffix = "§4" + suffixDef;
            if (size == 0)
                suffix = "§r" + suffixDef;

            metaTitle = metaTitle.replace("%num%", String.valueOf(i)).replace("%suffix%", suffix);

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

        menu.setOwner(new EnderChest.EnderChestOwner(player));

        menu.setItems(items);
        mainPlayer.openInventory(menu.getInventory());
    }
    public void openOfflinePlayerMainMenu(Player player, String playername){
        if(Bukkit.getPlayer(playername) != null){
            openPlayerMainMenu(player, Bukkit.getPlayer(playername));
            return;
        }

        if(!EnderChestUtils.playerWasRegistered(playername)){
            CoreUtils.errorMessage(player, EnderContainers.__("enderchest_player_never_connected"));
            return;
        }

        HashMap<Integer, Integer> accesses = EnderChestUtils.getPlayerAccesses(playername);
        UUID uuid   = EnderChestUtils.getPlayerUUIDFromPlayername(playername);
        String file = Config.saveDir + uuid.toString() + ".yml";

        int rows = (int) Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0);
        if (rows > 6) rows = 6;
        MainMenuContainer menu = new MainMenuContainer(rows, CoreUtils.replacePlayerName(EnderContainers.__("enderchest_main_gui_title"), playername));

        HashMap<Integer, ItemStack> items = new HashMap<>();
        HashMap<Integer, Integer> sqlSlotsChests  = new HashMap<>();

        if(EnderContainers.hasMysql()){
            List<DatabaseSet> sqlResults = EnderContainers.getMysqlManager().getPlayerEnderchests(uuid);
            for(DatabaseSet result : sqlResults){
                sqlSlotsChests.put(result.getInteger("enderchest_id"), result.getInteger("slots_used"));
            }
        }

        for (int i = 0; i < Config.maxEnderchests; i++) {
            ItemStack item;
            int size       = 0;

            if(EnderContainers.hasMysql()){
                if(sqlSlotsChests.containsKey(i)) size = sqlSlotsChests.get(i);
            }else{
                EnderContainers.getConfigClass().loadConfigFile(file);
                size = EnderContainers.getConfigClass().getInt(file, "enderchestsSize." + i);
            }

            if(i == 0) size = CoreUtils.getInventorySize(EnderChestUtils.getVanillaEnderChestOf(playername, uuid));

            Integer slots = (accesses.containsKey(i)) ? accesses.get(i) * 9 : ((i == 0) ? 27 : 0);
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

        menu.setOwner(new EnderChest.EnderChestOwner(playername));

        menu.setItems(items);
        player.openInventory(menu.getInventory());
    }

    public void openPlayerEnderChest(Integer num, Player player, Player playerToSpec) {
        Player owner = player;
        if (playerToSpec != null && playerToSpec != player) owner = playerToSpec;

        // Verification hooks
        if (num > Config.maxEnderchests - 1) {
            PluginMsg.cannotOpenEnderchest(player);
            return;
        }
        if (num > 0 && !player.hasPermission(Config.enderchestOpenPerm + num) && num >= Config.defaultEnderchestsNumber) {
            PluginMsg.doesNotHavePerm(player);
            return;
        }

        EnderChest ec = getPlayerEnderchestOf(owner, num);
        assert ec != null;

        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        CoreUtils.playSoundTo(Config.openingChestSound, player);
        player.openInventory(ec.getContainer().getInventory());
    }
    public void openOfflinePlayerEnderChest(Integer num, Player player, String playername){
        if(Bukkit.getPlayer(playername) != null){
            openPlayerEnderChest(num, player, Bukkit.getPlayer(playername));
            return;
        }

        if (num > Config.maxEnderchests - 1) {
            PluginMsg.cannotOpenEnderchest(player);
            return;
        }

        EnderChest ec = getPlayerEnderchestOf(playername, num);
        assert ec != null;

        if (enderchestsOpens.containsKey(player)) enderchestsOpens.remove(player);
        enderchestsOpens.put(player, ec);

        CoreUtils.playSoundTo(Config.openingChestSound, player);
        player.openInventory(ec.getContainer().getInventory());
    }


    public void savePlayerInfo(Player p) {
        if (!EnderContainers.hasMysql()) {
            ConfigClass cc = EnderContainers.getConfigClass();

            cc.loadConfigFile("players.yml");

            cc.set("players.yml", p.getName() + ".uuid", p.getUniqueId().toString());
            cc.set("players.yml", p.getName() + ".accesses", EnderChestUtils.playerAvailableEnderchestsToString(p));
        } else {
            EnderContainers.getMysqlManager().updatePlayerUUID(p);
        }
    }
    public void refreshEnderChestsOf(Player p){
        for(EnderChest enderchest : getOpenedEnderchests().values()){
            EnderChest.EnderChestOwner owner = enderchest.getOwner();
            if(owner == null) continue;

            if(!owner.ownerIsOnline() && owner.getPlayerName().equals(p.getName())){
                enderchest.getContainer().refresh();
                enderchest.save();

                enderchest.setNewOwner(new EnderChest.EnderChestOwner(p));

                if(enderchest.getNum() == 0) {
                    EnderChestUtils.reloadVanillaEnderChest(enderchest);
                    enderchest.getContainer().setNewInventory(p.getEnderChest());
                }
            }
        }
    }

}
