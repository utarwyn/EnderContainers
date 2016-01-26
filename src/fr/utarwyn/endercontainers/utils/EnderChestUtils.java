package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class EnderChestUtils {

    @SuppressWarnings("deprecation")
    public static void openPlayerMainMenu(Player player, Player playerToSpec) {
        Player mainPlayer = player;
        if (playerToSpec != null) player = playerToSpec;

        if(playerToSpec != null){
            if(playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())){
                if (!mainPlayer.hasPermission("endercontainers.openeverywhere") && !mainPlayer.isOp()) {
                    PluginMsg.doesNotHavePerm(mainPlayer);
                    return;
                }
            }else{
                if (!mainPlayer.hasPermission("endercontainers.admin") && !mainPlayer.isOp()) {
                    PluginMsg.doesNotHavePerm(mainPlayer);
                    return;
                }
            }
        }

        int cells = (int) (Math.ceil(EnderContainers.getConfigClass().getDouble("main", "enderchests.max") / 9.0) * 9);
        if (cells > 6 * 9) cells = 6 * 9;
        MenuContainer menu = new MenuContainer(cells, CoreUtils.replacePlayerName(Config.mainEnderchestTitle, player));

        HashMap<Integer, ItemStack> items = new HashMap<>();

        for (int i = 0; i < Config.maxEnderchests; i++) {
            ItemStack item = null;
            EnderChest ec = EnderContainers.getEnderchestsManager().getPlayerEnderchest(player, i);

            if (!player.hasPermission(Config.enderchestOpenPerm + i) && i != 0) {
                item = new ItemStack(160, 1, (short) 0, (byte) 15);
            } else {
                item = new ItemStack(160, 1);

                if (ec.getItems().size() > 0)
                    item = new ItemStack(160, 1, (short) 0, (byte) 5);
                if (ec.getItems().size() >= 22)
                    item = new ItemStack(160, 1, (short) 0, (byte) 1);
                if (ec.getItems().size() == 27)
                    item = new ItemStack(160, 1, (short) 0, (byte) 14);
            }

            int invSize = ec.getItems().size();
            if(i == 0) invSize = CoreUtils.getInventorySize(mainPlayer.getEnderChest());

            ItemMeta meta = item.getItemMeta();
            String suffixDef = "(" + invSize + "/27)";
            String suffix = suffixDef;

            if (invSize > 0)
                suffix = "§a" + suffixDef;
            if (invSize >= 22)
                suffix = "§6" + suffixDef;
            if (invSize == 27)
                suffix = "§4" + suffixDef;
            if (invSize == 0)
                suffix = "§r" + suffixDef;

            if (!player.hasPermission(Config.enderchestOpenPerm + i) && i != 0) {
                meta.setDisplayName("§cEnderchest n°" + (i + 1) + " " + suffix);
                meta.setLore(Arrays.asList(" ", "This enderchest is locked."));
            }else{
                meta.setDisplayName("§aEnderchest n°" + (i + 1) + " " + suffix);
            }

            if (ec.getItems().size() == 27)
                meta.setLore(Arrays.asList(" ", "§4Inventory full !"));

            if(playerToSpec != null && i != 0 && invSize == 0) {
                if (!playerToSpec.getName().equalsIgnoreCase(mainPlayer.getName())) {
                    meta.setLore(Arrays.asList(" ", "This enderchest is empty.", "§cYou can't open this enderchest."));
                }
            }

            item.setItemMeta(meta);

            items.put(i, item);
        }

        menu.setItems(items);


        mainPlayer.openInventory(menu.getInventory());
    }


    public static void initatePlayerFile(String file, Player player) {
        EnderContainers.getConfigClass().set(file, "playername", player.getName());
        EnderContainers.getConfigClass().set(file, "lastsaved", -1);
    }

    public static boolean createBackup(String name, Player player) {
        String pre = "backups." + name;
        final String base = "plugins/EnderContainers/enderchests/";
        final long curentTime = System.currentTimeMillis();

        EnderContainers.getConfigClass().loadConfigFile("backups.yml");

        if (EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) return false;

        EnderContainers.getConfigClass().set("backups.yml", pre + ".name", name);
        EnderContainers.getConfigClass().set("backups.yml", pre + ".date", "" + curentTime);
        EnderContainers.getConfigClass().set("backups.yml", pre + ".type", "all");
        EnderContainers.getConfigClass().set("backups.yml", pre + ".directory", "enderchests/backup_" + curentTime + "/");

        if (player != null) EnderContainers.getConfigClass().set("backups.yml", pre + ".createdBy", player.getName());
        else EnderContainers.getConfigClass().set("backups.yml", pre + ".createdBy", "console");

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                File folder = new File(base + "backup_" + curentTime + "/");
                if (!folder.exists())
                    folder.mkdirs();

                File enderFolder = new File(base);
                File[] enderFiles = enderFolder.listFiles();

                for (File enderFile : enderFiles) {
                    if (enderFile.getName().contains(".")) {
                        File to = new File(base + "backup_" + curentTime + "/" + enderFile.getName());
                        try {
                            Files.copy(enderFile.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        return true;
    }

    public static void loadBackup(final String name, final Player p) {
        final String pre = "backups." + name;
        final String base = "plugins/EnderContainers/enderchests/";
        Config.enabled = false;

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {

                if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                    CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                    Config.enabled = true;
                    return;
                }

                File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".directory"));
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

        return;
    }

    public static void removeBackup(final Player p, final String name) {
        final String pre = "backups." + name;

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {

                if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                    CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                    return;
                }

                File backupDir = new File("plugins/EnderContainers/" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".directory"));
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
        EnderChest ec = EnderContainers.getInstance().enderchestsManager.getPlayerEnderchest(p, Integer.valueOf(num));

        if ((ec == null) || (ecBukkit == null)) return;
        if (num != 0) return;

        int ecBukkitSize = CoreUtils.getInventorySize(ecBukkit);
        int ecSize = ec.getItems().size();
        int maxSize = ecBukkit.getSize();

        if (ecBukkitSize == 0 && ecSize > 0) {
            ecBukkit.clear();
            Iterator<Integer> localIterator = ec.getItems().keySet().iterator();

            while (localIterator.hasNext()) {
                int index = ((Integer) localIterator.next()).intValue();
                ecBukkit.setItem(index,
                        (ItemStack) ec.getItems().get(Integer.valueOf(index)));
            }

            ec.clearItems();
            ec.save();

            if (ecBukkitSize + ecSize <= maxSize) {
                while (localIterator.hasNext()) {
                    int index = ((Integer) localIterator.next()).intValue();
                    ecBukkit.addItem(new ItemStack[]{(ItemStack) ec.getItems().get(Integer.valueOf(index))});
                }

                ec.clearItems();
                ec.save();
            } else {
                CoreUtils.errorMessage(p, "Your EnderChest doesn't receive all your old items. Please leave slots to get all your items.");
                return;
            }
        }
    }
}
