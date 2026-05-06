package com.brlnsreb.minigames.core.player;

import java.util.*;

import com.brlnsreb.minigames.core.DatabaseManager;

public class PlayerDataManager {
    
    private final HashMap<UUID, PlayerData> dataMap;

    public PlayerDataManager() {
        this.dataMap = new HashMap<>();
    }

    private boolean addNewPlayer(String playerId) {
        if (DatabaseManager.executeUpdate(
                """
                INSERT INTO players (uuid, player_name, exp, coins)
                VALUES (?, ?, ?, ?)
                """,
                playerId, null, 0, 0
                
            ) < 1) return false;

        return true;
    }

    public boolean newPlayer(CustomPlayer player) {
        String playerId = player.getUniqueId().toString();

        List<Map<String, Object>> results = DatabaseManager.executeSelect(
            "SELECT * FROM players WHERE uuid = ?", 
            playerId
        );

        if (results == null) return false;

        PlayerData data = new PlayerData();
        this.dataMap.put(player.getUniqueId(), data);
        player.setPlayerData(data);

        if (results.isEmpty()) {
            if (addNewPlayer(playerId)) return true;
            return false;

        } else {
            Map<String, Object> rawData = results.getFirst();
            data.name = (String) rawData.get("player_name");
            data.coins = (int) rawData.get("coins");
            data.setExp((long) rawData.get("exp"));
            
            return true;
        }
    }

    public boolean savePlayerData(UUID playerId) {
        PlayerData data = dataMap.get(playerId);

        if (DatabaseManager.executeUpdate(
            """
            UPDATE players
            SET exp = ?, coins = ?
            WHERE uuid = ?
            """,
            data.getExp(), data.coins, playerId.toString()

        ) < 1) return false;

        return true;
    }

    public boolean savePlayerData(UUID playerId, String field, Object value) {
        if (DatabaseManager.executeUpdate(
            """
            UPDATE players
            SET ? = ?
            WHERE uuid = ?
            """,
            field, value, playerId.toString()

        ) < 1) return false;

        return true;
    }

    public void onMatchEnd(UUID winnerId) {
        onMatchEnd(winnerId, 0, false);
    }

    public void onMatchEnd(UUID winnerId, int kills) {
        onMatchEnd(winnerId, kills, true);
    }

    public void onMatchEnd(UUID winnerId, int kills, boolean pvp) {
        
    }

}
