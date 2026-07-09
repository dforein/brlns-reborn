package org.brlnsreb.core.player.data.database;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.database.DBResults;

public class FriendsManager {

    public static void loadFriendDataSync(CustomPlayer player, String accountName) throws SQLException {
        PlayerData data = player.getPlayerData();

        //friends
        populateDataSetFromDB(
            data.getFriends(),
            accountName,
            "SELECT friend_name FROM friends WHERE player_name = ?",
            "friend_name"  
        );

        //received friend requests
        populateDataSetFromDB(
            data.getReceivedFriendRequests(),
            accountName,
            "SELECT sender_name FROM friend_requests WHERE receiver_name = ?",
            "sender_name");

        //sent friend requests
        populateDataSetFromDB(
            data.getSentFriendRequests(),
            accountName,
            "SELECT receiver_name FROM friend_requests WHERE sender_name = ?",
            "receiver_name"
        );

        addOnlineFriend(data, accountName);
    }

    private static void populateDataSetFromDB(Set<String> dataSet, String accountName, String sql, String field) throws SQLException {
        DBResults queryResults = DatabaseManager.executeSelect(sql, accountName);
        if (queryResults.isEmpty()) return;

        dataSet.clear();
        for (int i = 0; i < queryResults.results.size(); i++) {
            dataSet.add(queryResults.getString(i, field));
        }
    }

    private static void addOnlineFriend(PlayerData data, String accountName) {
        for (String name : data.getFriends()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name);
            if (friendData == null) continue;
            friendData.addOnlineFriend(accountName);
        }
    }

    public static void removeOnlineFriend(PlayerData data) {
        for (String name : data.getOnlineFriends()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name);
            if (friendData == null) continue;
            friendData.removeOnlineFriend(data.name);
        }
    }


    public static CompletableFuture<Outcome> sendRequest(String senderName, String receiverName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (senderName.equals(receiverName)) return Outcome.CANNOT_FRIEND_SELF;
                if (areFriends(senderName, receiverName)) return Outcome.ALREADY_FRIENDS;

                //check if player is online, in such case use the player's data for next checks
                PlayerData data;
                data = PlayerDataManager.getPlayerData(senderName);

                if (data != null) {
                    //(1) check if the request was already sent
                    if (data.getSentFriendRequests().contains(receiverName)) return Outcome.REQUEST_ALREADY_SENT;

                    //(2) check if the other player sent as well a request in the past, in such case accept directly
                    if (data.getReceivedFriendRequests().contains(receiverName)) {
                        return acceptRequestSync(receiverName, senderName);
                    }
                } else {
                    //the player is not online, use DB queries
                    //(1)
                    DBResults existingRequest = DatabaseManager.executeSelect(
                        "SELECT * FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                        senderName, receiverName
                    );
                    if (!existingRequest.isEmpty()) return Outcome.REQUEST_ALREADY_SENT;

                    //(2)
                    DBResults reverseRequest = DatabaseManager.executeSelect(
                        "SELECT * FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                        receiverName, senderName
                    );
                    if (!reverseRequest.isEmpty()) {
                        return acceptRequestSync(receiverName, senderName);
                    }
                }
                
                //checks passed, add new friend request
                DatabaseManager.executeUpdate(
                    "INSERT INTO friend_requests (sender_name, receiver_name) VALUES (?, ?)",
                    senderName, receiverName
                );

                updateIfOnline(senderName, sdata -> sdata.sendFriendRequest(receiverName));
                updateIfOnline(receiverName, rdata -> rdata.receiveFriendRequest(senderName));

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
    }

    private static Outcome acceptRequestSync(String senderName, String receiverName) throws SQLException {
        //check if request is present
        PlayerData data = PlayerDataManager.getPlayerData(senderName);
        if (data != null) {
            if (!data.getReceivedFriendRequests().contains(senderName)) return Outcome.REQUEST_NOT_FOUND;
        } else {
            DBResults request = DatabaseManager.executeSelect(
                "SELECT * FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                senderName, receiverName
            );
            if (request.isEmpty()) return Outcome.REQUEST_NOT_FOUND;
        }
        
        //accept friend request
        DatabaseManager.executeTransaction(conn -> {
            DatabaseManager.executeUpdate(conn,
                "DELETE FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                senderName, receiverName);
            DatabaseManager.executeUpdate(conn,
                "INSERT INTO friends (player_name, friend_name) VALUES (?, ?)",
                senderName, receiverName);
            DatabaseManager.executeUpdate(conn,
                "INSERT INTO friends (player_name, friend_name) VALUES (?, ?)",
                receiverName, senderName);
        });

        updateIfOnline(senderName, sdata -> sdata.addFriend(receiverName));
        updateIfOnline(receiverName, rdata -> rdata.addFriend(senderName));

        return Outcome.OK;
    }

    public static CompletableFuture<Outcome> acceptRequest(String receiverName, String senderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return acceptRequestSync(senderName, receiverName);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> declineRequest(String receiverName, String senderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int rows = DatabaseManager.executeUpdate(
                    "DELETE FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                    senderName, receiverName
                );
                if (rows == 0) return Outcome.REQUEST_NOT_FOUND;

                updateIfOnline(senderName, data -> data.removeSentFriendRequest(receiverName));
                updateIfOnline(receiverName, data -> data.removeReceivedFriendRequest(senderName));

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> removeFriend(String playerName, String friendName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!areFriends(playerName, friendName)) return Outcome.NOT_FRIENDS;

                DatabaseManager.executeTransaction(conn -> {
                    DatabaseManager.executeUpdate(conn,
                        "DELETE FROM friends WHERE player_name = ? AND friend_name = ?",
                        playerName, friendName);
                    DatabaseManager.executeUpdate(conn,
                        "DELETE FROM friends WHERE player_name = ? AND friend_name = ?",
                        friendName, playerName);
                });

                updateIfOnline(playerName, data -> data.removeFriend(friendName));
                updateIfOnline(friendName, data -> data.removeFriend(playerName));

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        });
    }

    public static CompletableFuture<Outcome> saveFriendsSettings(CustomPlayer player) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            Outcome outcome = saveFriendsSettingsSync(player.getPlayerData());
            player.resetAsync();
            return outcome;
        });
    }

    public static Outcome saveFriendsSettingsSync(PlayerData data) {
        try {
            if (!data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_OUT;

            DatabaseManager.executeUpdate(
                """
                UPDATE accounts
                SET friend_alerts = ?, friend_notify = ?
                WHERE name = ?
                """,
                data.getFriendsAlerts(), data.getFriendsNotify(), 
                data.name
            );

            return Outcome.OK;
        } catch (SQLException e) {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        }
    }

    private static boolean areFriends(String playerName, String friendName) throws SQLException {
        PlayerData data;

        //online check
        data = PlayerDataManager.getPlayerData(playerName);
        if (data != null) return data.isFriendWith(friendName);
        data = PlayerDataManager.getPlayerData(friendName);
        if (data != null) return data.isFriendWith(playerName);

        //db check
        DBResults queryResults = DatabaseManager.executeSelect(
            "SELECT * FROM friends WHERE player_name = ? AND friend_name = ?",
            playerName, friendName
        );
        return !queryResults.isEmpty();
    }

    private static void updateIfOnline(String name, Consumer<PlayerData> action) {
        PlayerData data = PlayerDataManager.getPlayerData(name);
        if (data != null) action.accept(data);
    }
}