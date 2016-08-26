package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderChest;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.containers.EnderChestContainer;
import fr.utarwyn.endercontainers.containers.MainMenuContainer;
import fr.utarwyn.endercontainers.dependencies.FactionsProtection;
import fr.utarwyn.endercontainers.dependencies.PlotSquaredProtection;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;
import org.bukkit.Location;
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
        } else if (Config.disabledWorlds.contains(p.getWorld().getName())) {
            return;
        }

        if (b.getType().equals(Material.ENDER_CHEST)) {
            if (EnderContainers.getDependenciesManager().isDependencyLoaded("Factions")) {
                if (!FactionsProtection.canOpenEnderChestInFaction(b, p)) {
                    e.setCancelled(true);
                    PluginMsg.cantUseHereFaction(p);
                    return;
                }
            }
            if (EnderContainers.getDependenciesManager().isDependencyLoaded("PlotSquared")) {
                if (!PlotSquaredProtection.canOpenEnderChestInPlot(b, p)) {
                    e.setCancelled(true);
                    PluginMsg.cantUseHereFaction(p);
                    return;
                }
            }

            playSoundInWorld(Config.openingChestSound, p, b.getLocation());

            EnderContainers.getEnderchestsManager().openPlayerMainMenu(p, null);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Integer index = e.getRawSlot();

        // Verify slot index
        if (index >= e.getInventory().getSize()) return;
        if (index < 0) return;
        if (index >= Config.maxEnderchests) return;


        Sound clickSound = CoreUtils.soundExists("CLICK") ? Sound.valueOf("CLICK") : Sound.valueOf("UI_BUTTON_CLICK");

        if (!(e.getInventory().getHolder() instanceof MainMenuContainer)) return;
        MainMenuContainer container = (MainMenuContainer) e.getInventory().getHolder();

        e.setCancelled(true);

        assert container.getOwner() != null;
        if(!container.getOwner().exists()) return;

        if (container.getOwner().getPlayerName().equals(p.getName())) { // Own main enderchest
            if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) return;

            if (p.hasPermission(Config.enderchestOpenPerm + index) || index < Config.defaultEnderchestsNumber) {
                p.playSound(p.getLocation(), clickSound, 1, 1);
                EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, null);
            }else{
                Sound glassSound = CoreUtils.soundExists("GLASS") ? Sound.valueOf("GLASS") : Sound.valueOf("BLOCK_GLASS_HIT");
                p.playSound(p.getLocation(), glassSound, 1, 1);
            }
        }else if(container.getOwner().ownerIsOnline()){ // Player who open another enderchest
            Player playerOwner = container.getOwner().getPlayer();

            p.playSound(p.getLocation(), clickSound, 1, 1);

            EnderChest ec = EnderContainers.getEnderchestsManager().getPlayerEnderchestOf(playerOwner, index);
            if(ec == null || index > (EnderChestUtils.getPlayerAvailableEnderchests(playerOwner) - 1)) return;

            EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, playerOwner);
        }else if(!container.getOwner().ownerIsOnline()){ // Player who open an offline enderchest
            String playername = container.getOwner().getPlayerName();

            EnderContainers.getEnderchestsManager().openOfflinePlayerEnderChest(index, p, playername);
        }
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent e) {
        Player p      = (Player) e.getPlayer();
        Inventory inv = e.getInventory();

        EnderchestsManager ecm = EnderContainers.getEnderchestsManager();

        if(!(inv.getHolder() instanceof EnderChestContainer) && !inv.getName().equals("container.enderchest")) return;
        if (!ecm.getOpenedEnderchests().containsKey(p)) return;

        EnderChest ec = ecm.getOpenedEnderchests().get(p);

        assert ec != null;
        ecm.getOpenedEnderchests().remove(p);

        ec.getContainer().refresh();
        ec.save();

        playSoundTo(Config.closingChestSound, p);
    }

    private void playSoundTo(String soundName, Player player){
        if(CoreUtils.soundExists(soundName)) {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1F, 1F);
        }else
            CoreUtils.log("§cThe sound §6" + soundName + "§c doesn't exists. Please change it in the config.", true);
    }
    private void playSoundInWorld(String soundName, Player player, Location location){
        if(CoreUtils.soundExists(soundName)) {
            player.getWorld().playSound(location, Sound.valueOf(soundName), 1F, 1F);
        }else
            CoreUtils.log("§cThe sound §6" + soundName + "§c doesn't exists. Please change it in the config.", true);
    }
}