package org.brlnsreb.core.player.data.database;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.brlnsreb.core.Configs;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.database.DBResults;
import org.powernukkitx.Server;
import org.powernukkitx.scheduler.ServerScheduler;

public class FriendsManager {

    private static ServerScheduler scheduler;

    public static void init() {
        scheduler = Server.getInstance().getScheduler();
    }

    //friends init

    public static void loadFriendDataSync(CustomPlayer player, String accountName) throws SQLException {
        PlayerData data = player.data;

        //friends
        populateDataMapFromDB(
            data.getOfflineFriends(),
            accountName,
            "SELECT friend_name FROM friends WHERE player_name = ?",
            "friend_name"  
        );

        //received friend requests
        populateDataMapFromDB(
            data.getReceivedFriendRequests(),
            accountName,
            "SELECT sender_name FROM friend_requests WHERE receiver_name = ?",
            "sender_name"
        );

        //sent friend requests
        populateDataMapFromDB(
            data.getSentFriendRequests(),
            accountName,
            "SELECT receiver_name FROM friend_requests WHERE sender_name = ?",
            "receiver_name"
        );

        addOnlineFriend(data, accountName);
    }

    private static void populateDataMapFromDB(Map<String, String> dataMap, String accountName, String sql, String field) throws SQLException {
        DBResults queryResults = DatabaseManager.executeSelect(sql, accountName);
        if (queryResults.isEmpty()) return;

        scheduler.scheduleTask(() -> {
            dataMap.clear();

            for (int i = 0; i < queryResults.results.size(); i++) {
                String value = queryResults.getString(i, field);
                dataMap.put(value.toLowerCase(), value);
            }
        });
    }


    //online - offline

    private static void addOnlineFriend(PlayerData data, String accountName) {
        //player login
        String friendJoined = null;
        if (data.getFriendNotify()) {
            friendJoined = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-server-join", 
                Configs.getGlobalMessages()
            ).formatted(accountName);
        }

        for (Entry<String, String> name : data.getOfflineFriendsEntriesCopy()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name.getKey());
            if (friendData == null) continue;
            data.addOnlineFriend(name.getKey(), name.getValue());
            friendData.addOnlineFriend(accountName);

            CustomPlayer friend = PlayerUtils.getPlayer(name.getValue());
            if (friendJoined != null) friend.sendMessage(friendJoined);
        }
    }

    public static void removeOnlineFriend(PlayerData data) {
        //player logout
        String friendLeft = null;
        if (data.getFriendNotify()) {
            friendLeft = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-server-left", 
                Configs.getGlobalMessages()
            ).formatted(data.name);
        }

        for (Entry<String, String> name : data.getOnlineFriends().entrySet()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name.getKey());
            if (friendData == null) continue;
            friendData.removeOnlineFriend(data.name);

            CustomPlayer friend = PlayerUtils.getPlayer(name.getValue());
            if (friendLeft != null) friend.sendMessage(friendLeft);
        }
    }


    //friend requests

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
                    if (data.hasSentRequestTo(receiverName)) return Outcome.REQUEST_ALREADY_SENT;

                    //(2) check if the other player sent as well a request in the past, in such case accept directly
                    if (data.hasReceivedRequestFrom(receiverName)) {
                        acceptRequestSync(receiverName, senderName);    //the receiver is a past sender, so i put the receiver as the sender arg
                        return Outcome.ADDED_FRIEND;
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
                        acceptRequestSync(receiverName, senderName);
                        return Outcome.ADDED_FRIEND;
                    }
                }

                //check if requests are enabled for the receiver
                PlayerData receiverData = PlayerDataManager.getPlayerData(receiverName);
                if (receiverData != null && !receiverData.getFriendRequestsFlag()) return Outcome.REQUESTS_DISABLED;
                
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
        PlayerData data = PlayerDataManager.getPlayerData(receiverName);
        if (data != null) {
            if (!data.hasReceivedRequestFrom(senderName)) return Outcome.REQUEST_NOT_FOUND;
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

        updateIfOnline(senderName, sdata -> sdata.addFriend(receiverName, true));
        updateIfOnline(receiverName, rdata -> rdata.addFriend(senderName, true));

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

    public static CompletableFuture<Outcome> denyRequest(String receiverName, String senderName) {
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


    //removing friend

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


    //save friends settings in DB

    public static CompletableFuture<Outcome> saveFriendsSettings(CustomPlayer player) {
        if (!player.canRunAsync()) {
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            Outcome outcome = saveFriendsSettingsSync(player.data);
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
                data.getFriendAlerts(), data.getFriendNotify(), 
                data.name
            );

            return Outcome.OK;
        } catch (SQLException e) {
            e.printStackTrace();
            return Outcome.DB_ERROR;
        }
    }


    //utils

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
        if (data != null) scheduler.scheduleTask(() -> action.accept(data));
    }
}