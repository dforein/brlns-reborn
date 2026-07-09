package org.brlnsreb.core.player.data.database;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.database.DBResults;

public class PlayerDataManager {
    
    private static final ConcurrentHashMap<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> nameIdMap = new ConcurrentHashMap<>();

    public static void init() {
        AccountsManager.init();
    }

    public static CompletableFuture<Outcome> onServerJoin(CustomPlayer player) {
        if (!DatabaseManager.isEnabled()) return CompletableFuture.completedFuture(Outcome.DB_ERROR);
        player.canRunAsync();

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

                //the player is not new, search and load the data
                String accountName = playersResults.getString("name");

                AccountsManager.loadAccountDataSync(player, accountName);
                FriendsManager.loadFriendDataSync(player, accountName);
                nameIdMap.put(accountName, playerId);
                
                //update the last login timestamp
                DatabaseManager.executeUpdate(
                    "UPDATE players SET last_login = CURRENT_TIMESTAMP WHERE uuid = ?",
                    playerId.toString()
                );

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.kick("Database error onServerJoin: report this error to the dev team, if you can.");
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

                //creating new account and updating player's data
                Outcome outcome = AccountsManager.createNewAccount(player, name, password);
                if (outcome != Outcome.OK) return outcome;

                nameIdMap.put(name, player.getUniqueId());

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
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
                if (nameIdMap.get(name) != null) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //try to load account
                Outcome outcome = AccountsManager.checkAndLoadAccountDataSync(player, name, password);
                if (outcome != Outcome.OK) return outcome;
                FriendsManager.loadFriendDataSync(player, name);

                nameIdMap.put(name, player.getUniqueId());

                //remove old player-account association, if it exists
                DBResults playerResults = DatabaseManager.executeSelect(
                    "SELECT * FROM players WHERE name = ?", 
                    name
                );
                if (!playerResults.isEmpty()) {
                    if (!playerResults.getString("uuid").equals(player.getUniqueId().toString())) {
                        DatabaseManager.executeUpdate(
                            "UPDATE players SET uuid = ? WHERE name = ?", 
                            player.getUniqueId().toString(), name
                        );
                    }
                }

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
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

                //execute player logout
                AccountsManager.playerLogoutSync(player);
                nameIdMap.remove(player.getPlayerData().name);

                
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
    }

    public static void savePlayerDataSync(UUID uuid) {
        AccountsManager.savePlayerDataSync(getPlayerData(uuid));
    }


    public static PlayerData getPlayerData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public static PlayerData getPlayerData(String name) {
        UUID uuid = nameIdMap.get(name);
        return uuid == null ? null : dataMap.get(uuid);
    }

    public static UUID getPlayerId(String name) {
        return nameIdMap.get(name);
    }

}