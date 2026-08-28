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
    
    private static final ConcurrentHashMap<UUID, PlayerData> uuid2DataMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> name2UuidMap = new ConcurrentHashMap<>();     //*lowercase* name -to-> uuid, for case insensitive searches

    public static void init() {
        AccountsManager.init();
        FriendsManager.init();
    }

    public static CompletableFuture<Outcome> onServerJoin(CustomPlayer player) {
        UUID uuid = player.getUniqueId();

        //put empty PlayerData shell in the player
        PlayerData data = new PlayerData();
        uuid2DataMap.put(uuid, data);
        player.data = data;

        if (!DatabaseManager.isEnabled()) return CompletableFuture.completedFuture(Outcome.DB_ERROR);
        
        player.canRunAsync();
        return CompletableFuture.supplyAsync(() -> {
            try {
                //check whether the player's uuid is already linked to an account
                DBResults playersResults = DatabaseManager.executeSelect(
                    "SELECT * FROM players WHERE uuid = ?", 
                    uuid.toString()
                );
                if (playersResults.isEmpty()) return Outcome.OK;

                //the player is not new, search and load the data
                String accountName = playersResults.getString("name");

                AccountsManager.loadAccountDataSync(player, accountName);
                FriendsManager.loadFriendDataSync(player, accountName);
                
                //update the last login timestamp
                DatabaseManager.executeUpdate(
                    "UPDATE players SET last_login = CURRENT_TIMESTAMP WHERE uuid = ?",
                    uuid.toString()
                );

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            player.kick("Database error onServerJoin: report this error to the dev team, if you can.");
            return onDBError(e);
        });
    }

    public static void onServerLeave(CustomPlayer player) {
        uuid2DataMap.remove(player.getUniqueId());
        if (player.data.isLogged()) {
            name2UuidMap.remove(player.data.name.toLowerCase());
        }
    }

    public static void onLogin(String name, UUID uuid) {
        name2UuidMap.put(name.toLowerCase(), uuid);
    }

    public static CompletableFuture<Outcome> registerNewPlayer(CustomPlayer player, String name, String password) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (player.data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //creating new account and updating player's data
                Outcome outcome = AccountsManager.createNewAccount(player, name, password);
                if (outcome != Outcome.OK) return outcome;

                onLogin(name, player.getUniqueId());

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            return onDBError(e);
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
                if (player.data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //check whether another player is already logged into the account
                if (name2UuidMap.putIfAbsent(name.toLowerCase(), player.getUniqueId()) != null) return Outcome.PLAYER_ALREADY_LOGGED_IN;

                //try to load account
                Outcome outcome = AccountsManager.checkAndLoadAccountDataSync(player, name, password);
                if (outcome != Outcome.OK) {
                    name2UuidMap.remove(name.toLowerCase(), player.getUniqueId());
                    return outcome;
                }
                FriendsManager.loadFriendDataSync(player, name);

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
            return onDBError(e);
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
                if (!player.data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_OUT;

                //delete the player-account association
                DatabaseManager.executeUpdate(
                    "DELETE FROM players WHERE uuid = ?", 
                    player.getUniqueId().toString()
                );

                //execute player logout
                AccountsManager.playerLogoutSync(player);
                name2UuidMap.remove(player.data.name.toLowerCase());

                
                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            } finally {
                player.resetAsync();
            }
        }).exceptionally(e -> {
            return onDBError(e);
        });
    }

    public static void savePlayerDataSync(UUID uuid) {
        AccountsManager.savePlayerDataSync(getPlayerData(uuid));
    }


    public static PlayerData getPlayerData(UUID uuid) {
        return uuid2DataMap.get(uuid);
    }

    public static PlayerData getPlayerData(String name) {
        UUID uuid = getPlayerId(name);
        return uuid == null ? null : uuid2DataMap.get(uuid);
    }

    public static UUID getPlayerId(String name) {
        return name2UuidMap.get(name.toLowerCase());
    }


    public static Outcome onDBError(SQLException e) {
        if (DatabaseManager.isEnabled()) e.printStackTrace();
        return Outcome.DB_ERROR;
    }

    public static Outcome onDBError(Throwable e) {
        if (DatabaseManager.isEnabled()) e.printStackTrace();
        return Outcome.DB_ERROR;
    }

}