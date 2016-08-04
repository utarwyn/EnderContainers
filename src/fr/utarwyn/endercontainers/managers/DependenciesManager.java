package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;

public class DependenciesManager {
    private EnderContainers instance;
    private HashMap<String, Boolean> dependencies = new HashMap<String, Boolean>();

    public DependenciesManager() {
        this.instance = EnderContainers.getInstance();

        loadDependencies();
    }

    private void loadDependencies() {
        CoreUtils.log("§8[§6EnderContainers§8] -----------§8[§bDependencies§8]§7-----------", true);
        if (Config.factionsSupport) {
            loadDependency("Factions");
        } else {
            this.dependencies.put("Factions", false);
        }
        if (Config.plotSquaredSupport) {
            loadDependency("PlotSquared");
        } else {
            this.dependencies.put("PlotSquared", false);
        }
        if (Config.citizensSupport) {
            loadDependency("Citizens");
        } else {
            this.dependencies.put("Citizens", false);
        }
        CoreUtils.log("§8[§6EnderContainers§8] ------------------------------------", true);
    }

    private HashMap<String, Boolean> getDependencies() {
        return this.dependencies;
    }

    public boolean isDependencyLoaded(String dependency) {
        if (getDependencies().containsKey(dependency)) {
            return ((Boolean) getDependencies().get(dependency)).booleanValue();
        }
        return false;
    }

    private void loadDependency(String dependency) {
        PluginManager pm = this.instance.getServer().getPluginManager();
        boolean isEnabled = false;
        if ((pm.getPlugin(dependency) != null) && (pm.getPlugin(dependency).isEnabled())) {
            isEnabled = true;
        }

        this.dependencies.put(dependency, isEnabled);

        if (isEnabled) {
            CoreUtils.log("§8[§6EnderContainers§8] §e   - " + dependency + "§r: " + ChatColor.GREEN + "[Enabled]", true);
        } else {
            CoreUtils.log("§8[§6EnderContainers§8] §e   - " + dependency + "§r: " + ChatColor.RED + "[Disabled]", true);
        }
    }
}
