package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginListener implements Listener {

    private boolean disabling = false;

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (disabling) return;
        disabling = true;

        CoreUtils.log("Save all opened enderchests...");
        EnderChestUtils.saveOpenedEnderchests();
        CoreUtils.log("All enderchests are now saved in the config ! See you soon :P");
    }

}
