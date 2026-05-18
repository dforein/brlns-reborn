package com.brlnsreb.minigames.core.player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.mindrot.jbcrypt.BCrypt;

import com.brlnsreb.minigames.core.DatabaseManager;
import com.brlnsreb.minigames.core.auth.AuthMenu;
import com.brlnsreb.minigames.utils.DBResults;

import cn.nukkit.Server;
import cn.nukkit.scheduler.ServerScheduler;

public class PlayerDataManager {

    private static AuthMenu auth = new AuthMenu();
    private static ServerScheduler scheduler = Server.getInstance().getScheduler();
    public enum Outcome {
        OK,
        ASYNC_TASK_ALREADY_RUNNING,
        PLAYER_ALREADY_LOGGED_IN,
        DB_ERROR,
        INVALID_NAME,
        NAME_ALREADY_IN_USE,
        NAME_NOT_FOUND,
        WRONG_PASSWORD
    }
    
    private static final HashMap<UUID, PlayerData> dataMap = new HashMap<>();

    public static CompletableFuture<Outcome> newPlayer(CustomPlayer player) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        String playerId = player.getUniqueId().toString();

        //put empty PlayerData shell in the player
        PlayerData data = new PlayerData();
        dataMap.put(player.getUniqueId(), data);
        player.setPlayerData(data);

        return CompletableFuture.supplyAsync(() -> {
            try {
                //check whether the player's uuid is already linked to an account
                DBResults playersResults = DatabaseManager.executeSelect(
                    "SELECT * FROM players WHERE uuid = ?", 
                    playerId
                );

                if (playersResults.isEmpty()) return Outcome.OK;

                //the player is not new, search the account
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    playersResults.getString("name")
                );
                
                //update player's data
                data.name = accountsResults.getString("name");
                data.coins = accountsResults.getInt("coins");
                data.setExp(accountsResults.getInt("exp"));

                scheduler.scheduleTask(() -> {
                    player.updatePlayerNameTag();
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
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
                if (player.getPlayerData().name != null) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //check name: alphanumeric + underscore, min and max lenght
                if (!name.matches("^[A-Za-z0-9_]{3,26}$")) return Outcome.INVALID_NAME;
                
                //check whether the name is already in use
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    name
                );

                if (!accountsResults.isEmpty()) return Outcome.NAME_ALREADY_IN_USE;

                //adding new account and updating player's data
                if (!addNewAccount(name, password, player.getUniqueId())) return Outcome.DB_ERROR;
                PlayerData data = player.getPlayerData();
                data.name = name;

                scheduler.scheduleTask(() -> {
                    player.updatePlayerNameTag();
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    public static boolean addNewAccount(String name, String password, UUID playerId) throws SQLException {
        String pwHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        if (DatabaseManager.executeUpdate(
                """
                INSERT INTO accounts (name, password_hash, exp, coins)
                VALUES (?, ?, ?, ?)
                """,
                name, pwHash, 0, 0
                
            ) < 1) return false;

        if (DatabaseManager.executeUpdate(
                """
                INSERT INTO players (uuid, name)
                VALUES (?, ?)
                """,
                playerId.toString(), name
                
            ) < 1) return false;

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
                if (player.getPlayerData().name != null) return Outcome.PLAYER_ALREADY_LOGGED_IN;

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

                //password check
                String pwHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                if (!accountsResults.getString("password_hash").equals(pwHash)) return Outcome.WRONG_PASSWORD;

                //player's data update
                PlayerData data = player.getPlayerData();
                data.name = name;
                data.coins = accountsResults.getInt("coins");
                data.setExp(accountsResults.getInt("exp"));

                scheduler.scheduleTask(() -> {
                    player.updatePlayerNameTag();
                });

                player.resetAsync();
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            player.resetAsync();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> savePlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerData data = dataMap.get(playerId);

                if (DatabaseManager.executeUpdate(
                    """
                    UPDATE accounts
                    SET exp = ?, coins = ?
                    WHERE name = ?
                    """,
                    data.getExp(), data.coins, data.name

                ) < 1) return Outcome.DB_ERROR;

                return Outcome.OK;

            } catch (SQLException e) {
                return Outcome.DB_ERROR;
            }
        });
    }

    public static CompletableFuture<Outcome> savePlayerData(UUID playerId, String field, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (DatabaseManager.executeUpdate(
                    """
                    UPDATE accounts
                    SET ? = ?
                    WHERE name = ?
                    """,
                    field, value, dataMap.get(playerId).name

                ) < 1) return Outcome.DB_ERROR;

                return Outcome.OK;

            } catch (SQLException e) {
                return Outcome.DB_ERROR;
            }
        });
    }

    public static void onMatchEnd(UUID winnerId) {
        onMatchEnd(winnerId, 0, false);
    }

    public static void onMatchEnd(UUID winnerId, int kills) {
        onMatchEnd(winnerId, kills, true);
    }

    public static void onMatchEnd(UUID winnerId, int kills, boolean pvp) {
        
    }

    public static AuthMenu getAuth() { return auth; }

}
