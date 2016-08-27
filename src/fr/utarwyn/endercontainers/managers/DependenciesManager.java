package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DependenciesManager {
    private EnderContainers instance;
    private HashMap<String, Boolean> dependencies = new HashMap<String, Boolean>();

    public DependenciesManager() {
        this.instance = EnderContainers.getInstance();

        loadDependencies();
    }

    private void loadDependencies() {
        CoreUtils.log("§8[§6EnderContainers§8] -----------§8[§bDependencies§8]§7-----------", true);

        for(String dependency : Config.dependencies){
            loadDependency(dependency);
        }

        CoreUtils.log("§8[§6EnderContainers§8] §e  - Enabled: §a" + Arrays.toString(getEnabledDependencies().toArray()), true);
        CoreUtils.log("§8[§6EnderContainers§8] §e  - Disabled: §c" + Arrays.toString(getDisabledDependencies().toArray()), true);

        CoreUtils.log("§8[§6EnderContainers§8] ------------------------------------", true);
    }

    private HashMap<String, Boolean> getDependencies() {
        return this.dependencies;
    }
    private List<String> getDisabledDependencies(){
        List<String> dps = new ArrayList<>();

        for(String name : getDependencies().keySet()){
            if(!getDependencies().get(name)) dps.add(name);
        }

        return dps;
    }
    private List<String> getEnabledDependencies(){
        List<String> dps = new ArrayList<>();

        for(String name : getDependencies().keySet()){
            if(getDependencies().get(name)) dps.add(name);
        }

        return dps;
    }

    public boolean dependencyIsLoaded(String dependency) {
        return getDependencies().containsKey(dependency) && getDependencies().get(dependency);
    }

    private void loadDependency(String dependency) {
        PluginManager pm  = this.instance.getServer().getPluginManager();
        boolean isEnabled = (pm.getPlugin(dependency) != null && pm.getPlugin(dependency).isEnabled());

        this.dependencies.put(dependency, isEnabled);
    }
}
