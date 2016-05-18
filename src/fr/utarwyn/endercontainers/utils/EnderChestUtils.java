package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.*;

public class EnderChestUtils {

    @SuppressWarnings("deprecation")
    public static void openPlayerMainMenu(Player player, Player playerToSpec) {
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

        int availableEnderchests = getPlayerAvailableEnderchests(player);

        // Update player slots (in BDD or on disk)
        EnderContainers.getEnderchestsManager().savePlayerInfo(player);

        if(availableEnderchests == 1){
            mainPlayer.openInventory(player.getEnderChest());
            return;
        }

        int cells = (int) (Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0) * 9);
        if (cells > 6 * 9) cells = 6 * 9;
        MenuContainer menu = new MenuContainer(cells, CoreUtils.replacePlayerName(Config.mainEnderchestTitle, player));

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

            Integer slots = (Config.allowDoubleChest && CoreUtils.playerHasPerm(player, "doublechest." + i)) ? 54 : 27;
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

            if (size > 0)
                suffix = "§a" + suffixDef;
            if (size >= (slots / 2))
                suffix = "§6" + suffixDef;
            if (size == slots)
                suffix = "§4" + suffixDef;
            if (size == 0)
                suffix = "§r" + suffixDef;

            if (!player.hasPermission(Config.enderchestOpenPerm + i) && i != 0 && i >= Config.defaultEnderchestsNumber) {
                meta.setDisplayName("§cEnderchest " + (i + 1) + " " + suffix);
                meta.setLore(Arrays.asList(" ", "This enderchest is locked."));
            }else{
                meta.setDisplayName("§aEnderchest " + (i + 1) + " " + suffix);

                if (size == slots) meta.setLore(Arrays.asList(" ", "§4Inventory full !"));
            }

            if(playerToSpec != null && size == 0) {
                if (!playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())) {
                    if (!player.hasPermission(Config.enderchestOpenPerm + i) && i >= Config.defaultEnderchestsNumber)
                        meta.setLore(Arrays.asList(" ", "§2This enderchest is empty.", "§cThe player don't have access to this chest."));
                    else
                        meta.setLore(Arrays.asList(" ", "§2This enderchest is empty.", "§aClick to show this enderchest."));
                }
            }else if(playerToSpec != null && !playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())){
                if (!player.hasPermission(Config.enderchestOpenPerm + i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", "§cThe player don't have access to this chest."));
                else
                    meta.setLore(Arrays.asList(" ", "§bClick to show the content", "§bof this enderchest."));
            }

            item.setItemMeta(meta);

            items.put(i, item);
        }

        menu.setItems(items);
        mainPlayer.openInventory(menu.getInventory());
    }
    public static void openOfflinePlayerMainMenu(Player player, String playername){
        if(Bukkit.getPlayer(playername) != null){
            openPlayerMainMenu(player, Bukkit.getPlayer(playername));
            return;
        }

        HashMap<Integer, Boolean> accesses = getPlayerAccesses(playername);
        UUID uuid   = null;
        String file = "";

        if(!Bukkit.getOfflinePlayer(playername).hasPlayedBefore() || (!EnderContainers.hasMysql() && !EnderContainers.getConfigClass().isConfigurationSection("players.yml", playername))){
            CoreUtils.errorMessage(player, "This player has never played on the server ! Please retry.");
            return;
        }

        if(!EnderContainers.hasMysql()) {
            uuid = UUID.fromString(EnderContainers.getConfigClass().getString("players.yml", playername + ".uuid"));
            file = Config.saveDir + uuid.toString() + ".yml";
        }else
            uuid = EnderContainers.getMysqlManager().getPlayerUUIDFromPlayername(playername);

        int cells = (int) (Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0) * 9);
        if (cells > 6 * 9) cells = 6 * 9;
        MenuContainer menu = new MenuContainer(cells, CoreUtils.replacePlayerName(Config.mainEnderchestTitle, playername));

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

            Integer slots = (Config.allowDoubleChest && accesses.containsKey(i) && accesses.get(i)) ? 54 : 27;
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
                meta.setLore(Arrays.asList(" ", "§cThis enderchest is locked."));
            }else{
                meta.setDisplayName("§aEnderchest " + (i + 1) + " " + suffix);

                if (size == slots) meta.setLore(Arrays.asList(" ", "§4Inventory full !"));
            }

            if(size == 0) {
                if (!accesses.containsKey(i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", "§2This enderchest is empty.", "§cThe player don't have access to this chest."));
                else
                    meta.setLore(Arrays.asList(" ", "§2This enderchest is empty.", "§aClick to show this enderchest."));
            }else{
                if (!accesses.containsKey(i) && i >= Config.defaultEnderchestsNumber)
                    meta.setLore(Arrays.asList(" ", "§cThe player don't have access to this chest."));
                else
                    meta.setLore(Arrays.asList(" ", "§bClick to show the content", "§bof this enderchest."));
            }

            item.setItemMeta(meta);

            items.put(i, item);
        }

        menu.setItems(items);

        menu.offlineOwnerName = playername;
        menu.offlineOwnerUUID = uuid;
        player.openInventory(menu.getInventory());
    }


    public static void initatePlayerFile(String file, String playername) {
        EnderContainers.getConfigClass().set(file, "playername", playername);
        EnderContainers.getConfigClass().set(file, "lastsaved", -1);
    }

    public static boolean createBackup(String name, CommandSender sender) {
        final String base = "plugins/EnderContainers/enderchests/";
        final long currentTime = System.currentTimeMillis();

        String type      = "all";
        String path = "enderchests/backup_" + currentTime + "/";

        if(!EnderContainers.hasMysql()) {
            String pre = "backups." + name;

            EnderContainers.getConfigClass().loadConfigFile("backups.yml");

            if (EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) return false;

            EnderContainers.getConfigClass().set("backups.yml", pre + ".name", name);
            EnderContainers.getConfigClass().set("backups.yml", pre + ".date", "" + currentTime);
            EnderContainers.getConfigClass().set("backups.yml", pre + ".type", type);
            EnderContainers.getConfigClass().set("backups.yml", pre + ".path", path);

            if (sender != null)
                EnderContainers.getConfigClass().set("backups.yml", pre + ".createdBy", sender.getName());
            else EnderContainers.getConfigClass().set("backups.yml", pre + ".createdBy", "console");

            EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
                @Override
                public void run() {
                    File folder = new File(base + "backup_" + currentTime + "/");
                    if (!folder.exists())
                        folder.mkdirs();

                    File enderFolder = new File(base);
                    File[] enderFiles = enderFolder.listFiles();

                    for (File enderFile : enderFiles) {
                        if (enderFile.getName().contains(".")) {
                            File to = new File(base + "backup_" + currentTime + "/" + enderFile.getName());
                            try {
                                Files.copy(enderFile.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }else{
            String data = "";
            DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);

            if(backup != null) return false;

            List<DatabaseSet> sets = EnderContainers.getMysqlManager().getAllEnderchests();
            for(DatabaseSet set : sets){
                if(set.getTimestamp("last_opening_time") == null) set.setObject("last_opening_time", new Timestamp(0));
                data += set.getInteger("id") + ":"
                        + Base64Coder.encodeString(set.getString("items")) + ":"
                        + set.getInteger("slots_used") + ":"
                        + set.getTimestamp("last_opening_time").getTime() + ":"
                        + set.getTimestamp("last_save_time").getTime() + ":"
                        + Base64Coder.encodeString(set.getString("player_uuid")) + ":"
                        + set.getInteger("enderchest_id") + ";";
            }

            data = data.substring(0, data.length() - 1);

            EnderContainers.getMysqlManager().saveBackup(name, currentTime, type, data, sender);
        }

        return true;
    }

    public static void loadBackup(final String name, final CommandSender p) {
        final String pre = "backups." + name;
        final String base = "plugins/EnderContainers/enderchests/";
        Config.enabled = false;

        if(!EnderContainers.hasMysql()) {
            EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
                @Override
                public void run() {

                    if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                        CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                        Config.enabled = true;
                        return;
                    }

                    File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".path"));
                    if (!backupDir.exists()) {
                        CoreUtils.errorMessage(p, "Backup folder of §6" + name + "§c not exist.");
                        Config.enabled = true;
                        return;
                    }

                    File[] backupFiles = backupDir.listFiles();
                    for (File backupFile : backupFiles) {
                        if (backupFile.getName().contains(".")) {
                            File to = new File(base + backupFile.getName());
                            try {
                                Files.copy(backupFile.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    EnderContainers.getInstance().reloadConfiguration();

                    Config.enabled = true;
                    p.sendMessage(Config.prefix + "§aBackup §b" + name + "§a loaded indefinitly.");
                    p.sendMessage(Config.prefix + "§aPlugin enabled !");
                }
            });
        }else{
            final DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);

            if(backup == null){
                CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                Config.enabled = true;
                return;
            }

            EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
                @Override
                public void run() {
                    String[] backupDatas = backup.getString("data").split(";");
                    EnderContainers.getDB().emptyTable(Config.DB_PREFIX + "enderchests");

                    for(String backupData : backupDatas){
                        String[] enderchestInfo = backupData.split(":");
                        Integer id   = Integer.valueOf(enderchestInfo[0]);
                        String items = Base64Coder.decodeString(enderchestInfo[1]);
                        Integer slotsUsed = Integer.valueOf(enderchestInfo[2]);
                        Timestamp lastOpeningTime = new Timestamp(0L);
                        Timestamp lastSaveTime    = new Timestamp(0L);

                        lastOpeningTime.setTime(Long.valueOf(enderchestInfo[3]));
                        lastSaveTime.setTime(Long.valueOf(enderchestInfo[4]));

                        if(enderchestInfo[3].equalsIgnoreCase("0")) lastOpeningTime = null;
                        if(enderchestInfo[4].equalsIgnoreCase("0")) lastSaveTime = null;

                        String playerUUID    = Base64Coder.decodeString(enderchestInfo[5]);
                        Integer enderchestId = Integer.valueOf(enderchestInfo[6]);

                        EnderContainers.getMysqlManager().saveEnderChestFromABackup(id, items, slotsUsed, lastOpeningTime, lastSaveTime, playerUUID, enderchestId);
                    }

                    CoreUtils.errorMessage(p, "§aBackup §6" + name + " §aloaded ! §2Plugin enabled.");
                    Config.enabled = true;
                }
            });
        }

        return;
    }

    public static void removeBackup(final String name, final CommandSender p) {
        final String pre = "backups." + name;
        final Boolean mysql = EnderContainers.hasMysql();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(!mysql) {
                    if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                        CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                        return;
                    }

                    File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".path"));
                    if (!backupDir.exists()) {
                        CoreUtils.errorMessage(p, "Backup folder of §6" + name + "§c not exist.");
                        return;
                    }

                    for (File f : backupDir.listFiles())
                        f.delete();

                    backupDir.delete();
                    EnderContainers.getConfigClass().removePath("backups.yml", pre);

                    if (EnderContainers.getConfigClass().getConfigurationSection("backups.yml", "backups").getKeys(false).size() == 0)
                        EnderContainers.getConfigClass().removePath("backups.yml", "backups");

                    EnderContainers.getInstance().reloadConfiguration();
                }else{
                    DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);
                    if(backup == null){
                        CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                        return;
                    }

                    EnderContainers.getMysqlManager().removeBackup(name);
                }

                p.sendMessage(Config.prefix + "§aBackup §b" + name + "§a removed.");
            }
        });

        return;
    }


    public static void saveOpenedEnderchests() {
        EnderchestsManager m = EnderContainers.getInstance().enderchestsManager;
        for (Player player : m.enderchestsOpens.keySet()) {
            EnderChest ec = m.enderchestsOpens.get(player);
            InventoryView invView = player.getOpenInventory();
            Inventory inv = null;

            // Select opened inventory
            if (invView == null) continue;
            inv = invView.getTopInventory();
            if (inv == null) continue;

            ec.clearItems();
            int index = 0;
            for (ItemStack i : inv.getContents()) {
                ec.addItem(index, i);
                index++;
            }

            ec.save();
        }
    }

    public static void recalculateItems(Player p, int num) {
        Inventory ecBukkit = p.getEnderChest();
        EnderChest ec = EnderContainers.getInstance().enderchestsManager.getPlayerEnderchest(p, num);

        if ((ec == null) || (ecBukkit == null)) return;
        if (num != 0) return;

        int ecBukkitSize = CoreUtils.getInventorySize(ecBukkit);
        int ecSize = ec.getItems().size();
        int maxSize = ecBukkit.getSize();

        if (ecBukkitSize == 0 && ecSize > 0) {
            ecBukkit.clear();
            Iterator<Integer> localIterator = ec.getItems().keySet().iterator();

            while (localIterator.hasNext()) {
                int index = localIterator.next();
                ecBukkit.setItem(index, ec.getItems().get(index));
            }

            ec.clearItems();
            ec.save();

            if (ecBukkitSize + ecSize <= maxSize) {
                while (localIterator.hasNext()) {
                    int index = localIterator.next();
                    ecBukkit.addItem(new ItemStack[]{(ItemStack) ec.getItems().get(Integer.valueOf(index))});
                }

                ec.clearItems();
                ec.save();
            } else {
                CoreUtils.errorMessage(p, "Your EnderChest doesn't receive all your old items. Please leave slots to get all your items.");
            }
        }
    }

    public static Integer getPlayerAvailableEnderchests(Player p){
        Integer n = 1;

        if(p.isOp()) return Config.maxEnderchests;

        for(int i = 1; i < (Config.maxEnderchests - 1); i++){
            if(p.hasPermission(Config.enderchestOpenPerm + i)) n++;
        }
        if(n < Config.defaultEnderchestsNumber) n = Config.defaultEnderchestsNumber;

        return n;
    }
    public static String playerAvailableEnderchestsToString(Player p){
        String r = "0:false;";

        for(int i = 1; i < Config.maxEnderchests; i++){
            if(p.hasPermission(Config.enderchestOpenPerm + i) || p.isOp() || i < Config.defaultEnderchestsNumber)
                r += i + ":" + (CoreUtils.playerHasPerm(p, "doublechest." + i) || p.isOp()) + ";";
        }

        r = r.substring(0, r.length() - 1);
        return r;
    }
    public static HashMap<Integer, Boolean> getPlayerAccesses(String playername){
        HashMap<Integer, Boolean> accesses = new HashMap<>();

        if(!Bukkit.getOfflinePlayer(playername).hasPlayedBefore())
            return accesses;
        if(!EnderContainers.hasMysql() && !EnderContainers.getConfigClass().isConfigurationSection("players.yml", playername))
            return accesses;

        if(!EnderContainers.hasMysql()){
            String[] accessesStr = EnderContainers.getConfigClass().getString("players.yml", playername + ".accesses").split(";");
            for(String accessStr : accessesStr) {
                accesses.put(Integer.valueOf(accessStr.split(":")[0]), Boolean.valueOf(accessStr.split(":")[1]));
            }
        }else{
            accesses = EnderContainers.getMysqlManager().getPlayerAccesses(playername);
        }

        return accesses;
    }
}
