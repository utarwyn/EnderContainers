package fr.utarwyn.endercontainers.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ConfigClass {

    Plugin plugin;

    FileConfiguration mainConfig;
    HashMap<String, YamlConfiguration> configs = new HashMap<>();

    public Boolean setAutoSaving = true;

    public ConfigClass(Plugin pl) {
        this.plugin = pl;
        mainConfig = this.plugin.getConfig();
    }

    public void loadConfigFile(String file) {
        if (this.configs.containsKey(file)) return;

        YamlConfiguration yaml = null;
        File f = new File(this.plugin.getDataFolder(), file);
        try {
            if (!this.plugin.getDataFolder().exists() && !this.plugin.getDataFolder().isDirectory())
                this.plugin.getDataFolder().mkdir();

            if (file.indexOf("/") != 0) {
                String folds[] = file.split("/");
                String currentFolder = "";

                for (int i = 0; i < folds.length - 1; i++) {
                    File fold = new File(plugin.getDataFolder() + "/" + currentFolder + folds[i]);
                    currentFolder += fold.getName() + "/";
                    if (!fold.exists() || !fold.isDirectory())
                        fold.mkdir();
                }
            }

            if (!f.isFile())
                f.createNewFile();

            yaml = YamlConfiguration.loadConfiguration(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.configs.put(file, yaml);
    }

    public Boolean contains(String file, String path) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.contains(path);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.contains(path);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    public Boolean isConfigurationSection(String file, String path) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.isConfigurationSection(path);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.isConfigurationSection(path);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    public ConfigurationSection getConfigurationSection(String file, String path) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getConfigurationSection(path);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getConfigurationSection(path);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    public Set<String> getKeys(String file, Boolean bool) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getKeys(bool);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getKeys(bool);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

	/*
     * *
	 * * 	Get Section
	 * *
	 */

    public int getInt(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getInt(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getInt(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
    }

    public double getDouble(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getDouble(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getDouble(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
    }

    public long getLong(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getLong(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getLong(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
    }

    public String getString(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getString(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getString(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    public Boolean getBoolean(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getBoolean(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getBoolean(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    public List<String> getStringList(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getStringList(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getStringList(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    public ItemStack getItemStack(String file, String key) {
        if (file.equalsIgnoreCase("main")) {
            return mainConfig.getItemStack(key);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                return config.getItemStack(key);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
	
	/*
	 * *
	 * *	Save Section
	 * *
	 */

    public void set(String file, String key, String value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, int value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, Double value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, Float value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, Boolean value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, List<String> value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void set(String file, String key, ItemStack value) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(key, value);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(key, value);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }

    public void removePath(String file, String path) {
        if (file.equalsIgnoreCase("main")) {
            mainConfig.set(path, null);
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                config.set(path, null);
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.setAutoSaving)
            this.saveConfig(file);
    }


    public void saveConfig(String file) {
        if (file.equalsIgnoreCase("main")) {
            plugin.saveConfig();
        } else {
            if (configs.containsKey(file)) {
                YamlConfiguration config = configs.get(file);
                try {
                    config.save(new File(plugin.getDataFolder(), file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    throw new Exception("Config file " + file + " doesn't added !");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        this.reloadConfig(file);
    }

    public void reloadConfig(String file) {
        if (!file.equalsIgnoreCase("main")) {
            if (configs.containsKey(file)) {
                configs.remove(file);
            }
            this.loadConfigFile(file);
        }
    }

    public void reloadConfigs() {
        List<String> configsTMP = new ArrayList<String>();
        for (String key : this.configs.keySet()) {
            configsTMP.add(key);
        }

        for (String key : configsTMP) {
            this.reloadConfig(key);
        }

        this.plugin.reloadConfig();
        this.mainConfig = this.plugin.getConfig();
    }

}
