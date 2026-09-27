package org.brlnsreb.core.player.data.database;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.database.DBResults;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.scheduler.ServerScheduler;

public class FriendsManager {

    private static ServerScheduler scheduler;

    public static void init() {
        scheduler = BrlnsReb.getScheduler();
    }

    //friends init

    public static void loadFriendDataSync(CustomPlayer player, String accountName) throws SQLException {
        PlayerData data = player.data;

        synchronized (data.getFriendLock()) {
            //friends
            populateDataSetFromDB(
                data.getOfflineFriends(),
                accountName,
                "SELECT friend_name FROM friends WHERE player_name = ?",
                "friend_name"  
            );

            //received friend requests
            populateDataSetFromDB(
                data.getReceivedFriendRequests(),
                accountName,
                "SELECT sender_name FROM friend_requests WHERE receiver_name = ?",
                "sender_name"
            );

            //sent friend requests
            populateDataSetFromDB(
                data.getSentFriendRequests(),
                accountName,
                "SELECT receiver_name FROM friend_requests WHERE sender_name = ?",
                "receiver_name"
            );
        }

        addOnlineFriend(data, accountName);
    }

    private static void populateDataSetFromDB(Set<String> dataSet, String accountName, String sql, String field) throws SQLException {
        DBResults queryResults = DatabaseManager.executeSelect(sql, accountName);
        if (queryResults.isEmpty()) return;

        dataSet.clear();

        for (int i = 0; i < queryResults.results.size(); i++) {
            String value = queryResults.getString(i, field);
            dataSet.add(value);
        }
    }


    //online - offline

    private static void addOnlineFriend(PlayerData data, String accountName) {
        //player login: add new online friend to all friends in data passed
        String friendJoined = null;
        if (data.getFriendNotify()) {
            friendJoined = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-server-join", 
                Configs.getGlobalMessages()
            ).formatted(accountName);
        }

        for (String name : data.getOfflineFriendsCopy()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name);
            if (friendData == null) continue;
            data.addOnlineFriend(name);
            friendData.addOnlineFriend(accountName);

            CustomPlayer friend = PlayerUtils.getLoggedPlayer(name);
            if (friendJoined != null) friend.sendMessage(friendJoined);
        }
    }

    public static void removeOnlineFriend(PlayerData data) {
        //player logout: remove online friend to all friends in data passed
        String friendLeft = null;
        if (data.getFriendNotify()) {
            friendLeft = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-server-left", 
                Configs.getGlobalMessages()
            ).formatted(data.name);
        }

        for (String name : data.getOnlineFriendsCopy()) {
            PlayerData friendData = PlayerDataManager.getPlayerData(name);
            if (friendData == null) continue;
            friendData.removeOnlineFriend(data.name);

            CustomPlayer friend = PlayerUtils.getLoggedPlayer(name);
            if (friendLeft != null) friend.sendMessage(friendLeft);
        }
    }


    //friend requests

    public static CompletableFuture<Outcome> sendRequest(String senderName, String receiverName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (senderName.equalsIgnoreCase(receiverName)) return Outcome.CANNOT_FRIEND_SELF;
                if (areFriends(senderName, receiverName)) return Outcome.ALREADY_FRIENDS;

                //check if receiver exists
                DBResults accountsResults = DatabaseManager.executeSelect(
                    "SELECT * FROM accounts WHERE name = ?", 
                    receiverName
                );
                if (accountsResults.isEmpty()) return Outcome.NAME_NOT_FOUND;
                String receiverNameCorrect = accountsResults.getString("name");

                //check if sender is online, in such case use the sender's data for next checks
                PlayerData senderData = PlayerDataManager.getPlayerData(senderName);

                if (senderData != null) {
                    //(1) check if the request was already sent
                    if (senderData.hasSentRequestTo(receiverName)) return Outcome.REQUEST_ALREADY_SENT;

                    //(2) check if the other player sent as well a request in the past, in such case accept directly
                    if (senderData.hasReceivedRequestFrom(receiverName)) {
                        acceptRequestSync(senderName, receiverName);    //the receiver is a past sender, so i put the receiver as the sender arg
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
                        acceptRequestSync(senderName, receiverName);
                        return Outcome.ADDED_FRIEND;
                    }
                }

                //check if requests are enabled for the receiver
                PlayerData receiverData = PlayerDataManager.getPlayerData(receiverName);
                if (receiverData != null && !receiverData.getFriendRequestsFlag()) return Outcome.REQUESTS_DISABLED;
                
                //checks passed, add new friend request
                DatabaseManager.executeUpdate(
                    "INSERT INTO friend_requests (sender_name, receiver_name) VALUES (?, ?)",
                    senderName, receiverNameCorrect
                );

                updateIfOnline(senderName, sdata -> sdata.sendFriendRequest(receiverNameCorrect));
                updateIfOnline(receiverName, rdata -> rdata.receiveFriendRequest(senderName));

                return Outcome.OK;

            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            return PlayerDataManager.onDBError(e);
        });
    }

    public static void sendRequestMessages(Outcome outcome, CustomPlayer sender, String receiverName) {
        sender.sendMessage(
            switch (outcome) {
                case OK -> ChatMsgs.SUCCESS_PFX + "Friend request sent to §e" + receiverName;
                //ps: here ADDED_FRIEND isn't OK, it's an extraordinary outcome; while in /friend accept it's normal so it's OK
                case ADDED_FRIEND -> ChatMsgs.SUCCESS_PFX + "§e" + receiverName + "§a added to your friend list";
                case NAME_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Does not exist such a player named " + receiverName;
                case CANNOT_FRIEND_SELF -> ChatMsgs.ERROR_PFX + "You cannot send a request to yourself!";
                case ALREADY_FRIENDS -> ChatMsgs.ERROR_PFX + receiverName + " is already your friend!";
                case REQUEST_ALREADY_SENT -> ChatMsgs.ERROR_PFX + "You have already sent a request to " + receiverName;
                case REQUESTS_DISABLED -> ChatMsgs.ERROR_PFX + "Sorry, requests are not enabled for " + receiverName;
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend_add_error";
            }
        );

        CustomPlayer receiver = PlayerUtils.getLoggedPlayer(receiverName);
        if (receiver != null) {
            if (outcome == Outcome.OK) {
                receiver.sendMessage(ChatMsgs.INFO_PFX + "§3" + sender.data.name + "§a wants to be your friend! \n§e/friend accept/deny " + sender.data.name);
            } else if (outcome == Outcome.ADDED_FRIEND) {
                receiver.sendMessage(ChatMsgs.INFO_PFX + "§e" + sender.data.name + "§a added to your friend list");
            }
        }
    }

    private static Outcome acceptRequestSync(String receiverName, String senderName) throws SQLException {
        //check if request is present
        PlayerData data = PlayerDataManager.getPlayerData(receiverName);
        String senderNameCorrect;
        if (data != null) {
            senderNameCorrect = data.getOriginalRequestSenderName(senderName);
            if (senderNameCorrect == null) return Outcome.REQUEST_NOT_FOUND;
        } else {
            DBResults request = DatabaseManager.executeSelect(
                "SELECT sender_name FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                senderName, receiverName
            );
            if (request.isEmpty()) return Outcome.REQUEST_NOT_FOUND;
            senderNameCorrect = request.getString("sender_name");
        }
        
        //accept friend request
        DatabaseManager.executeTransaction(conn -> {
            DatabaseManager.executeUpdate(conn,
                "DELETE FROM friend_requests WHERE sender_name = ? AND receiver_name = ?",
                senderNameCorrect, receiverName);
            DatabaseManager.executeUpdate(conn,
                "INSERT INTO friends (player_name, friend_name) VALUES (?, ?)",
                senderNameCorrect, receiverName);
            DatabaseManager.executeUpdate(conn,
                "INSERT INTO friends (player_name, friend_name) VALUES (?, ?)",
                receiverName, senderNameCorrect);
        });

        updateIfOnline(senderNameCorrect, sdata -> sdata.addFriend(receiverName, true));
        updateIfOnline(receiverName, rdata -> rdata.addFriend(senderNameCorrect, true));

        return Outcome.OK;
    }

    public static CompletableFuture<Outcome> acceptRequest(String receiverName, String senderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return acceptRequestSync(receiverName, senderName);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            return PlayerDataManager.onDBError(e);
        });
    }

    public static void sendAcceptRequestMessages(Outcome outcome, CustomPlayer requestReceiver, String requestSenderName) {
        requestReceiver.sendMessage(
            switch (outcome) {
                case OK -> ChatMsgs.SUCCESS_PFX + "§e" + requestSenderName + "§a added to your friend list";
                case REQUEST_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Request not found from " + requestSenderName;
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend_accept_error";
            }
        );

        if (outcome != Outcome.OK) return;
        CustomPlayer requestSender = PlayerUtils.getLoggedPlayer(requestSenderName);
        if (requestSender == null) return;

        requestSender.sendMessage(ChatMsgs.INFO_PFX + "§e" + requestReceiver.data.name + "§a added to your friend list");
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
            return PlayerDataManager.onDBError(e);
        });
    }

    public static void sendDenyRequestMessages(Outcome outcome, CustomPlayer requestReceiver, String requestSenderName) {
        requestReceiver.sendMessage(
            switch (outcome) {
                case OK -> ChatMsgs.SUCCESS_PFX + "Denied friend request from §e" + requestSenderName;
                case REQUEST_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Request not found from " + requestSenderName;
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend_deny_error";
            }
        );
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
            return PlayerDataManager.onDBError(e);
        });
    }

    public static void sendRemoveFriendMessages(Outcome outcome, CustomPlayer player, String friendName) {
        player.sendMessage(
            switch (outcome) {
                case OK -> ChatMsgs.SUCCESS_PFX + "§e" + friendName + "§a removed from your friend list";
                case NOT_FRIENDS -> ChatMsgs.ERROR_PFX + friendName + " not found in your friend list!";
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend_remove_error";
            }
        );
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
            return PlayerDataManager.onDBError(e);
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