package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;

public class MysqlManager {

    private Database database;

    public void setDatabase(Database database){
        this.database = database;
    }



    // Players table
    public void updatePlayerUUID(Player player){
        if(!Database.isConnected()) return;

        List<DatabaseSet> playerInfo = database.find(Config.DB_PREFIX + "players", DatabaseSet.makeConditions("player_uuid", player.getUniqueId().toString()));

        if(playerInfo.size() == 0) database.save(Config.DB_PREFIX + "players", DatabaseSet.makeFields("player_name", player.getName(), "player_uuid", player.getUniqueId().toString(), "accesses", EnderChestUtils.playerAvailableEnderchestsToString(player)));
    }
    public HashMap<Integer, Integer> getPlayerAccesses(String playername){
        String table = Config.DB_PREFIX + "players";
        HashMap<Integer, Integer> r = new HashMap<>();

        List<DatabaseSet> accesses = database.find(table, DatabaseSet.makeConditions("player_name", playername), null, Collections.singletonList("accesses"));

        for(DatabaseSet set : accesses){
            for(String accessStr : set.getString("accesses").split(";")) {
                String val   = accessStr.split(":")[1];
                Integer rows = val.equals("true") ? 6 : 3;

                if(StringUtils.isNumeric(val)) rows = Integer.parseInt(val);

                r.put(Integer.valueOf(accessStr.split(":")[0]), rows);
            }
        }

        return r;
    }
    public UUID getPlayerUUIDFromPlayername(String playername){
        String table = Config.DB_PREFIX + "players";
        List<DatabaseSet> players = database.find(table, DatabaseSet.makeConditions("player_name", playername), null, Collections.singletonList("player_uuid"));

        if(players.size() == 0) return null;
        return UUID.fromString(players.get(0).getString("player_uuid"));
    }


    // Enderchests table
    public void savePlayerEnderchest(UUID playerUUID, Integer chestIndex, Integer slotsUsed, String rawItems){
        String table = Config.DB_PREFIX + "enderchests";
        List<DatabaseSet> enderchestInfo = database.find(table, DatabaseSet.makeConditions("player_uuid", playerUUID.toString(), "enderchest_id", chestIndex + ""));

        if(enderchestInfo == null || enderchestInfo.size() == 0){
            database.save(table, DatabaseSet.makeFields(
                    "items", rawItems,
                    "player_uuid", playerUUID.toString(),
                    "enderchest_id", chestIndex,
                    "slots_used", slotsUsed
            ));
        }else{
            database.save(table, DatabaseSet.makeFields(
                    "items", rawItems,
                    "slots_used", slotsUsed
            ), DatabaseSet.makeConditions(
                    "player_uuid", playerUUID.toString(),
                    "enderchest_id", chestIndex + ""
            ));
        }
    }
    public void saveEnderChestFromABackup(Integer id, String items, Integer slotsUsed, Timestamp lastOpeningTime, Timestamp lastSaveTime, String playerUUID, Integer enderChestId){
        String table = Config.DB_PREFIX + "enderchests";
        database.save(table, DatabaseSet.makeFields(
                "id", id,
                "items", items,
                "last_opening_time", lastOpeningTime,
                "last_save_time", lastSaveTime,
                "player_uuid", playerUUID,
                "enderchest_id", enderChestId,
                "slots_used", slotsUsed
        ));
    }

    public List<DatabaseSet> getAllEnderchests() {
        String table = Config.DB_PREFIX + "enderchests";
        List<DatabaseSet> enderchests = database.find(table);
        return enderchests;
    }
    public DatabaseSet getPlayerEnderchest(UUID playerUUID, Integer chestIndex) {
        String table = Config.DB_PREFIX + "enderchests";
        List<DatabaseSet> enderchestInfo = database.find(table, DatabaseSet.makeConditions("player_uuid", playerUUID.toString(), "enderchest_id", chestIndex + ""));

        if (enderchestInfo == null || enderchestInfo.size() == 0) {
            return null;
        } else {
            return enderchestInfo.get(0);
        }

    }
    public DatabaseSet getEnderchestByUUID(String UUID, Integer chestIndex) {
        String table = Config.DB_PREFIX + "enderchests";
        List<DatabaseSet> enderchestInfo = database.find(table, DatabaseSet.makeConditions("player_uuid", UUID, "enderchest_id", chestIndex + ""));

        if (enderchestInfo == null || enderchestInfo.size() == 0) {
            return null;
        } else {
            return enderchestInfo.get(0);
        }

    }
    public List<DatabaseSet> getPlayerEnderchests(UUID playerUUID){
        String table = Config.DB_PREFIX + "enderchests";
        List<DatabaseSet> enderchests = database.find(table, DatabaseSet.makeConditions("player_uuid", playerUUID.toString()), null, Arrays.asList("enderchest_id", "slots_used"));
        return enderchests;
    }



    // Backups table
    public List<DatabaseSet> getBackups(){
        List<DatabaseSet> backups = database.find(Config.DB_PREFIX + "backups", null, null, Arrays.asList("name", "created_by"));

        for(DatabaseSet backup : backups){
            if(backup.getString("created_by").equalsIgnoreCase("CONSOLE")){
                backup.setObject("created_by_name", backup.getString("created_by"));
                continue;
            }

            List<DatabaseSet> playerInfo = database.find(Config.DB_PREFIX + "players", DatabaseSet.makeConditions("player_uuid", backup.getString("created_by")));
            if(playerInfo != null && playerInfo.size() > 0) backup.setObject("created_by_name", playerInfo.get(0).getString("player_name"));
        }

        return backups;
    }
    public DatabaseSet getBackup(String name){
        List<DatabaseSet> backups = database.find(Config.DB_PREFIX + "backups", DatabaseSet.makeConditions("name", name));
        DatabaseSet backup;

        if(backups == null || backups.size() == 0) return null;
        else backup = backups.get(0);

        if(backup.getString("created_by").equalsIgnoreCase("CONSOLE")){
            backup.setObject("created_by_name", backup.getString("created_by"));
        }else{
            List<DatabaseSet> playerInfo = database.find(Config.DB_PREFIX + "players", DatabaseSet.makeConditions("player_uuid", backup.getString("created_by")));
            if (playerInfo != null && playerInfo.size() > 0)
                backup.setObject("created_by_name", playerInfo.get(0).getString("player_name"));
        }

        return backup;
    }
    public void saveBackup(String name, long date, String type, String data, CommandSender createdBy){
        String createdbyStr = "CONSOLE";
        if(createdBy instanceof Player) createdbyStr = ((Player) createdBy).getUniqueId().toString();

        database.save(Config.DB_PREFIX + "backups", DatabaseSet.makeFields(
                "name", name,
                "date", new Timestamp(date),
                "type", type,
                "data", data,
                "created_by", createdbyStr
        ));
    }
    public void removeBackup(String name){
        database.delete(Config.DB_PREFIX + "backups", DatabaseSet.makeConditions("name", name));
    }

}
