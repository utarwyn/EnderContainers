package fr.utarwyn.endercontainers.migration.migration2_0_1;

import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class MigrationFlat2_0_1 extends Migration2_0_1 {

    @Override
    void reconfigureChestsContent() {
        List<File> chestFiles = this.getChestFiles();
        if (chestFiles == null) return;

        YamlConfiguration conf;
        String contents;

        for (File chestFile : chestFiles) {
            conf = YamlConfiguration.loadConfiguration(chestFile);

            if (!conf.isConfigurationSection("enderchests")) {
                continue;
            }

            for (String key : conf.getConfigurationSection("enderchests").getKeys(false)) {
                contents = conf.getString("enderchests." + key + ".contents");

                if (!isBase64Encoded(contents)) {
                    conf.set("enderchests." + key + ".contents", ItemSerializer.base64Serialization(ItemSerializer.experimentalDeserialization(contents)));
                }
            }

            try {
                conf.save(chestFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot save the player's enderchests file", e);
            }
        }

    }

}
