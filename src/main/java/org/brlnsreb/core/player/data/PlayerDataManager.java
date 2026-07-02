package org.brlnsreb.core.player.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import org.mindrot.jbcrypt.BCrypt;
import org.brlnsreb.core.DatabaseManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.DBResults;

import cn.nukkit.Server;
import cn.nukkit.scheduler.ServerScheduler;

public class PlayerDataManager {

    private static ServerScheduler scheduler;
    public enum Outcome {
        OK,
        ASYNC_TASK_ALREADY_RUNNING,
        PLAYER_ALREADY_LOGGED_IN,
        PLAYER_ALREADY_LOGGED_OUT,
        DB_ERROR,
        INVALID_NAME,
        NAME_ALREADY_IN_USE,
        NAME_NOT_FOUND,
        WRONG_PASSWORD
    }
    
    private static final ConcurrentHashMap<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> nameIdMap = new ConcurrentHashMap<>();

    public static void init() {
        scheduler = Server.getInstance().getScheduler();
    }

    private static void setAccountData(CustomPlayer player, String name, int coins, int exp, DBResults statsResults) {
        PlayerData data = player.getPlayerData();

        data.name = name;
        data.setCoins(coins);
        data.setExp(exp);

        if (statsResults == null || statsResults.isEmpty()) return;

        for (int i = 0; i < statsResults.results.size(); i++) {
            data.setStat(
                statsResults.getInt(i, "minigame_id"), 
                statsResults.getInt(i, "stat_type"),
                statsResults.getInt(i, "value")
            );
        }
    }

    public static CompletableFuture<Outcome> onServerJoin(CustomPlayer player) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        UUID playerId = player.getUniqueId();

        //put empty PlayerData shell in the player
        PlayerData data = new PlayerData();
        dataMap.put(playerId, data);
        player.setPlayerData(data);

        return CompletableFuture.supplyAsync(() -> {
            try {
                //check whether the player's uuid is already linked to an account
                DBResults playersResults = DatabaseManager.executeSelect(
                    "SELECT * FROM players WHERE uuid = ?", 
                    playerId.toString()
                );

                if (playersResults.isEmpty()) return Outcome.OK;

                //the player is not new, search the account and stats
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    playersResults.getString("name")
                );

                
                DBResults statsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM stats WHERE player_name = ?",
                    accountsResults.getString("name")
                );
                
                //update player's data
                scheduler.scheduleTask(() -> {
                    setAccountData(player, 
                        accountsResults.getString("name"), 
                        accountsResults.getInt("coins"), 
                        accountsResults.getInt("exp"),
                        statsResults
                    );
                    player.updatePlayerNameTag();

                    nameIdMap.put(data.name, playerId);
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> registerNewPlayer(CustomPlayer player, String name, String password) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (player.getPlayerData().isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //check name: alphanumeric + underscore, min and max lenght
                if (!name.matches("^[A-Za-z0-9_]{3,26}$")) return Outcome.INVALID_NAME;
                
                //check whether the name is already in use
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    name
                );

                if (!accountsResults.isEmpty()) return Outcome.NAME_ALREADY_IN_USE;

                //creating new account and updating player's data
                if (!createNewAccount(name, password, player.getUniqueId())) return Outcome.DB_ERROR;

                scheduler.scheduleTask(() -> {
                    setAccountData(player, 
                        name, 
                        0, 
                        0,
                        null
                    );
                    player.updatePlayerNameTag();

                    nameIdMap.put(name, player.getUniqueId());
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    private static boolean createNewAccount(String name, String password, UUID playerId) throws SQLException {
        String pwHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        DatabaseManager.executeUpdate(
            """
            INSERT INTO accounts (name, password_hash, exp, coins)
            VALUES (?, ?, ?, ?)
            """,
            name, pwHash, 0, 0        
        );

        DatabaseManager.executeUpdate(
            """
            INSERT INTO players (uuid, name)
            VALUES (?, ?)
            """,
            playerId, name
        );

        return true;
    }

    public static CompletableFuture<Outcome> playerLogin(CustomPlayer player, String name, String password) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (player.getPlayerData().isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //check whether another player is already logged into the account
                DBResults playerResults = DatabaseManager.executeSelect(
                    "SELECT * FROM players WHERE name = ?", 
                    name
                );

                if (!playerResults.isEmpty()) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //retrieve the account
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    name
                );

                if (accountsResults.isEmpty()) return Outcome.NAME_NOT_FOUND;

                DBResults statsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM stats WHERE player_name = ?",
                    name
                );

                //password check
                String pwHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                if (!accountsResults.getString("password_hash").equals(pwHash)) return Outcome.WRONG_PASSWORD;

                //player's data update
                scheduler.scheduleTask(() -> {
                    setAccountData(player, 
                        name, 
                        accountsResults.getInt("coins"), 
                        accountsResults.getInt("exp"),
                        statsResults
                    );

                    nameIdMap.put(name, player.getUniqueId());
                    player.updatePlayerNameTag();
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> playerLogout(CustomPlayer player) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!player.getPlayerData().isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_OUT;

                //delete the player-account association
                DatabaseManager.executeUpdate(
                    "DELETE FROM players WHERE uuid = ?", 
                    player.getUniqueId().toString()
                );

                //player's data update
                scheduler.scheduleTask(() -> {
                    savePlayerData(player.getUniqueId());
                    PlayerData data = player.getPlayerData();
                    
                    nameIdMap.remove(data.name);

                    data.resetData();
                    player.updatePlayerNameTag();
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> savePlayerDataAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            return savePlayerData(playerId);
        });
    }

    public static Outcome savePlayerData(UUID playerId) {
        PlayerData data = dataMap.get(playerId);

        try {
            if (!data.isLogged()) return Outcome.OK;
            
            DatabaseManager.executeUpdate(
                """
                UPDATE accounts
                SET exp = ?, coins = ?
                WHERE name = ?
                """,
                data.getExp(), data.getCoins(), data.name
            );

            return savePlayerStats(data);

        } catch (SQLException e) {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        }
    }

    public static Outcome savePlayerStats(PlayerData data) throws SQLException {
        int totalRows = data.getStatsAmount();
        if (totalRows == 0) return Outcome.OK;

        StringBuilder sql = new StringBuilder("INSERT INTO stats (player_name, minigame_id, stat_type, value) VALUES ");
        for (int i = 0; i < totalRows; i++) {
            sql.append("(?, ?, ?, ?)");
            if (i < totalRows - 1) {
                sql.append(", ");
            }
        }
        sql.append(" ON DUPLICATE KEY UPDATE value = VALUES(value);");

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        
            int i = 1, statTypeId;

            for (Map.Entry<Integer, int[]> entry : data.getStats().entrySet()) {
                int minigameId = entry.getKey();
                int[] values = entry.getValue();

                for (statTypeId = 0; statTypeId < StatType.size; statTypeId++) {
                    int value = values[statTypeId];
                    if (value == -1) continue;

                    stmt.setString(i++, data.name);
                    stmt.setInt(i++, minigameId);
                    stmt.setInt(i++, statTypeId);
                    stmt.setInt(i++, value);
                }
            }

            if (stmt.executeUpdate() < 1) return Outcome.DB_ERROR;
        }

        return Outcome.OK;
    }

    public static CompletableFuture<Outcome> savePlayerData(UUID playerId, String field, Object value) {
        String name = dataMap.get(playerId).name;

        return CompletableFuture.supplyAsync(() -> {
            try {
                DatabaseManager.executeUpdate(
                    """
                    UPDATE accounts
                    SET ? = ?
                    WHERE name = ?
                    """,
                    field, value, name
                );

                return Outcome.OK;

            } catch (SQLException e) {
                e.printStackTrace();
                return Outcome.DB_ERROR;
            }
        });
    }

    public static PlayerData getPlayerData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public static PlayerData getPlayerData(String name) {
        return dataMap.get(nameIdMap.get(name));
    }

    public static UUID getPlayerId(String name) {
        return nameIdMap.get(name);
    }

}
