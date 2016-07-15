package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EnderChestUtils {

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
                        CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
                        Config.enabled = true;
                        return;
                    }

                    File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".path"));
                    if (!backupDir.exists()) {
                        CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_folder_unknown").replace("%backup_name%", name));
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
                    p.sendMessage(Config.prefix + EnderContainers.__("cmd_backup_loaded").replace("%backup_name%", name));
                }
            });
        }else{
            final DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);

            if(backup == null){
                CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
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

                    CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_loaded").replace("%backup_name%", name));
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
                        CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
                        return;
                    }

                    File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".path"));
                    if (!backupDir.exists()) {
                        CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_folder_unknown").replace("%backup_name%", name));
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
                        CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
                        return;
                    }

                    EnderContainers.getMysqlManager().removeBackup(name);
                }

                p.sendMessage(Config.prefix + EnderContainers.__("cmd_backup_removed").replace("%backup_name%", name));
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
        String r = "0:3;";

        for(int i = 1; i < Config.maxEnderchests; i++){
            if(p.hasPermission(Config.enderchestOpenPerm + i) || p.isOp() || i < Config.defaultEnderchestsNumber)
                r += i + ":" + EnderChestUtils.getEnderChestAllowedRows(p, i) + ";";
        }

        r = r.substring(0, r.length() - 1);
        return r;
    }
    public static HashMap<Integer, Integer> getPlayerAccesses(String playername){
        HashMap<Integer, Integer> accesses = new HashMap<>();

        if(!EnderContainers.hasMysql()) EnderContainers.getConfigClass().loadConfigFile("players.yml");

        if(!Bukkit.getOfflinePlayer(playername).hasPlayedBefore())
            return accesses;
        if(!EnderContainers.hasMysql() && !EnderContainers.getConfigClass().isConfigurationSection("players.yml", playername))
            return accesses;

        if(!EnderContainers.hasMysql()){
            String[] accessesStr = EnderContainers.getConfigClass().getString("players.yml", playername + ".accesses").split(";");
            for(String accessStr : accessesStr) {
                String val   = accessStr.split(":")[1];
                Integer rows = val.equals("true") ? 6 : 3;

                if(StringUtils.isNumeric(val)) rows = Integer.parseInt(val);

                accesses.put(Integer.valueOf(accessStr.split(":")[0]), rows);
            }
        }else{
            accesses = EnderContainers.getMysqlManager().getPlayerAccesses(playername);
        }

        return accesses;
    }

    public static Integer getEnderChestAllowedRows(Player player, Integer enderchestNumber){
        if(CoreUtils.playerHasPerm(player, "doublechest." + enderchestNumber) || CoreUtils.playerHasPerm(player, "doublechest.*")) return 6;

        for(int row = 1; row <= 6; row++) {
            if (CoreUtils.playerHasPerm(player, "slot" + enderchestNumber + ".row" + row)) return row;
            else if (CoreUtils.playerHasPerm(player, "slots.row" + row)) return row;
        }

        return 3;
    }
}
