package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.MenuContainer;
import fr.utarwyn.endercontainers.dependencies.FactionsProtection;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EnderChestListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (b == null) return;
        if (!Config.enabled){
            e.setCancelled(true);
            PluginMsg.pluginDisabled(p);
            return;
        }

        if (b.getType().equals(Material.ENDER_CHEST)) {
            if (EnderContainers.getInstance().getDependenciesManager().isDependencyLoaded("Factions")) {
                if (!FactionsProtection.canOpenEnderChestInFaction(b, p)) {
                    e.setCancelled(true);
                    PluginMsg.cantUseHereFaction(p);
                    return;
                }
            }

            p.playSound(p.getLocation(), Sound.valueOf(Config.openingChestSound), 1F, 1F);
            EnderChestUtils.openPlayerMainMenu(p, null);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        String invname = e.getInventory().getTitle();

        if (!(e.getInventory().getHolder() instanceof MenuContainer)) return;

        Player playerOwner = EnderContainers.getEnderchestsManager().getLastEnderchestOpened(p);

        if (invname.equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, p))) { // Own main enderchest
            Integer index = e.getRawSlot();

            if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)){
                e.setCancelled(true);
                return;
            }

            if (index >= e.getInventory().getSize()) return;

            e.setCancelled(true);
            if (index < 0) return;
            if (index >= Config.maxEnderchests) return;

            EnderChestUtils.recalculateItems(p, index);

            if (index == 0) {
                p.playSound(p.getLocation(), Sound.valueOf(Config.openingChestSound), 1, 1);
                p.openInventory(p.getEnderChest());
                return;
            }

            if (p.hasPermission(Config.enderchestOpenPerm + index) || index < Config.defaultEnderchestsNumber) {
                p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
                EnderContainers.getInstance().enderchestsManager.openPlayerEnderChest(index, p, null);
            }else{
                p.playSound(p.getLocation(), Sound.GLASS, 1, 1);
            }
        }else if(playerOwner != null && invname.equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, playerOwner))){ // Player who open another enderchest
            Integer index = e.getRawSlot();

            if (index >= e.getInventory().getSize()) return;
            e.setCancelled(true);

            if (index < 0) return;
            if (index >= Config.maxEnderchests) return;

            p.playSound(p.getLocation(), Sound.CLICK, 1, 1);

            EnderChest ec = EnderContainers.getEnderchestsManager().getPlayerEnderchest(playerOwner, index);
            if(ec == null || index > (EnderChestUtils.getPlayerAvailableEnderchests(playerOwner) - 1)) return;

            if (index == 0) {
                p.playSound(p.getLocation(), Sound.valueOf(Config.openingChestSound), 1, 1);
                p.openInventory(playerOwner.getEnderChest());
                return;
            }

            if (p.hasPermission(Config.enderchestOpenPerm + index) || index < Config.defaultEnderchestsNumber)
                EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, playerOwner);
        }else if(playerOwner == null){ // Player who open an offline enderchest
            if(EnderContainers.getEnderchestsManager().enderchestsOpens.containsKey(p)) return;

            MenuContainer menu = (MenuContainer) e.getInventory().getHolder();
            Integer index      = e.getRawSlot();
            String playername  = menu.offlineOwnerName;
            UUID uuid          = menu.offlineOwnerUUID;

            if(index >= e.getInventory().getSize()) return;
            if(index < 0) return;

            if(invname.equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, playername))){
                e.setCancelled(true);
                EnderContainers.getEnderchestsManager().openOfflinePlayerEnderChest(index, p, uuid, playername);
            }
        }
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        EnderChest ec = null;
        EnderchestsManager ecm = EnderContainers.getEnderchestsManager();

        Player playerOwner = ecm.getLastEnderchestOpened(p);

        if(inv.getName().equalsIgnoreCase("container.enderchest")){
            p.playSound(p.getLocation(), Sound.valueOf(Config.closingChestSound), 1F, 1F);
            return;
        }

        if (inv.getName().equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, p))) return;
        if (playerOwner != null && inv.getName().equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, playerOwner))) return;
        if (!ecm.enderchestsOpens.containsKey(p)) return;
        ec = ecm.enderchestsOpens.get(p);

        if(ec != null && ec.getOwner() == null){ // Close offline player's chest (or main menu)
            if(inv.getName().equalsIgnoreCase(CoreUtils.replacePlayerName(Config.mainEnderchestTitle, ec.ownerName))) return;
        }

        ecm.enderchestsOpens.remove(p);

        assert ec != null;
        ec.clearItems();

        int index = 0;
        for (ItemStack i : inv.getContents()) {
            ec.addItem(index, i);
            index++;
        }

        ec.save();

        if(ec.lastMenuContainer == null || ec.lastMenuContainer.getInventory() == null)
            EnderContainers.getEnderchestsManager().removeEnderChest(ec);

        p.playSound(p.getLocation(), Sound.valueOf(Config.closingChestSound), 1F, 1F);
    }
}