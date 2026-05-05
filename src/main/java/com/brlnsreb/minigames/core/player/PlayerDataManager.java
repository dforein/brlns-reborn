package com.brlnsreb.minigames.core.player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import com.brlnsreb.minigames.core.DatabaseManager;

public class PlayerDataManager {
    
    private final HashMap<UUID, PlayerData> dataMap;

    public PlayerDataManager() {
        this.dataMap = new HashMap<>();
    }

    public void newPlayer(CustomPlayer player) {
        PlayerData data = new PlayerData();
        this.dataMap.put(player.getUniqueId(), data);
        player.setPlayerData(data);
        
        if (DatabaseManager.isEnabled()) {
            try {
                Connection conn = DatabaseManager.getConnection();
            } catch (SQLException e) {

            }
        }
    }

    public void savePlayerData(UUID playerId) {
        dataMap.get(playerId);

        if (DatabaseManager.isEnabled()) {
            try {
                Connection conn = DatabaseManager.getConnection();
            } catch (SQLException e) {

            }
        }
    }

    public void onMatchEnd(UUID winnerId) {
        onMatchEnd(winnerId, 0, false);
    }

    public void onMatchEnd(UUID winnerId, int kills) {
        onMatchEnd(winnerId, kills, true);
    }

    public void onMatchEnd(UUID winnerId, int kills, boolean pvp) {
        if (DatabaseManager.isEnabled()) {
            try {
                Connection conn = DatabaseManager.getConnection();
            } catch (SQLException e) {

            }
        }
    }

}
