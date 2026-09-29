package org.brlnsreb.mainhub.ui;

import java.util.List;

import org.brlnsreb.commands.FriendCommand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.window.ModalForm;
import org.powernukkitx.form.window.SimpleForm;

public class MenuForm extends FormAbstract {

    private static final String PATH = "lobby.items.menu.";
    private static final int FRIENDS_PER_PAGE = 50;

    public static void openForm(CustomPlayer player) {
        if (!checkCooldown(player)) return;

        SimpleForm form = new SimpleForm(getStr("title"));
        form.addButton("Manage Friends", p -> {
            if (!player.data.isLogged()) {
                player.sendMessage(ChatMsgs.ERROR_PFX + "You are not logged in!");
                return;
            }
            manageFriends(player);
        });
        
        form.send(player);
    }

    private static void manageFriends(CustomPlayer player) {
        SimpleForm form = new SimpleForm("Manage Friends");
        form.addButton("Add New Friend", p -> addNewFriend(player));
        //form.addButton("Search Friends", p -> searchFriends(player));
        form.addButton("Friend List", p -> friendList(player, 1));
        form.addButton("Friend Invites", p -> friendInvites(player));
        form.addButton("Home", p -> openForm(player));
        
        form.send(player);
    }

    private static void addNewFriend(CustomPlayer player) {
        CustomForm form = new CustomForm("Add New Friend");
        form.addInput("Insert the name of the player you want to add:");

        form.send(player);
        form.onSubmit((p, response) -> {
            String senderName = (player).data.name;
            String receiverName = response.getInputResponse(0);
            FriendsManager.sendRequest(senderName, receiverName).thenAccept(outcome -> {
                FriendsManager.sendRequestMessages(outcome, player, receiverName);
            });
        });
    }

    private static void friendList(CustomPlayer player, int page) {
        SimpleForm form = new SimpleForm("Friend List");
        form.content("Friend List / Page "+page+"\nFriend Slots: "+player.data.getFriendCount());
        
        int slots = 0;
        int startIndex = (page - 1) * FRIENDS_PER_PAGE;

        if (player.data.getOnlineFriendCount() >= startIndex + 1) {
            List<String> onlineFriends = player.data.getOnlineFriendsCopy();
            onlineFriends.sort(String.CASE_INSENSITIVE_ORDER);

            for (int i = startIndex; i < onlineFriends.size() && slots < FRIENDS_PER_PAGE; i++, slots++) {
                String currName = onlineFriends.get(i);
                CustomPlayer curr = PlayerUtils.getLoggedPlayer(currName);
                if (curr == null) {
                    slots--;
                    continue;
                }

                form.addButton(
                    currName + " §8- §aON §8(" 
                        + (curr.minigameCurrent != null 
                            ? curr.minigameCurrent.mgt.nameTag.toUpperCase()
                            : "HUB"
                        )
                        + ")",
                    p -> friendActions(player, currName, true)
                );
            }
        }

        if (slots < FRIENDS_PER_PAGE) {
            List<String> offlineFriends = player.data.getOfflineFriendsCopy();
            offlineFriends.sort(String.CASE_INSENSITIVE_ORDER);

            int start = startIndex + slots - player.data.getOnlineFriendCount();

            for (int i = start >= 0 ? start : 0; i < offlineFriends.size() && slots < FRIENDS_PER_PAGE; i++, slots++) {
                String currName = offlineFriends.get(i);
                form.addButton(
                    currName + " §8- §cOFF",
                    p -> friendActions(player, currName, false)
                );
            }
        }

        if (player.data.getFriendCount() - 1 < startIndex + FRIENDS_PER_PAGE) {
            form.addButton("First Page", p -> friendList(player, 1));
        } else {
            form.addButton("Next Page", p -> friendList(player, page + 1));
        }

        form.addButton("Home", p -> manageFriends(player));

        form.send(player);
    }

    private static void friendActions(CustomPlayer player, String friendName, boolean isOnline) {
        SimpleForm form = new SimpleForm("Friend Actions ("+friendName+")");

        //form.addButton("Invite To Party", p -> implement());
        //form.addButton("Invite To Crew", p -> implement());

        if (isOnline) {
            form.addButton("Join / Spectate", p -> {
                FriendCommand.joinSpectateFriend(player, friendName);
            });
        }

        form.addButton("Remove", p -> {
            FriendsManager.removeFriend(player.data.name, friendName).thenAccept(outcome -> {
                FriendsManager.sendRemoveFriendMessages(outcome, player, friendName);
            });
        });

        //remove and ignore?

        form.addButton("Go Back", p -> friendList(player, 1));
        form.addButton("Home", p -> manageFriends(player));

        form.send(player);
    }

    private static void friendInvites(CustomPlayer player) {
        SimpleForm form = new SimpleForm("Friend Invites");
        List<String> receivedFriendRequests = player.data.getReceivedFriendRequestsCopy();

        form.content(receivedFriendRequests.size() == 0 
            ? "No pending friend requests!"
            : "Friend Invites: "+receivedFriendRequests.size()
        );

        for (String requestSender : receivedFriendRequests) {
            form.addButton(requestSender, p -> inviteAction(player, requestSender));
        }

        form.addButton("Home", p -> manageFriends(player));

        form.send(player);
    }

    private static void inviteAction(CustomPlayer player, String requestSenderName) {
        ModalForm form = new ModalForm(
            "Invite Action ("+requestSenderName+")", 
            "Do you want to accept or deny the friend request sent by "+requestSenderName+"?"
                + "\nTo cancel close the window."
        );

        form.yes("Accept", p -> {
            FriendsManager.acceptRequest(player.data.name, requestSenderName).thenAccept(outcome -> {
                FriendsManager.sendAcceptRequestMessages(outcome, player, requestSenderName);
            });
        });

        form.no("Deny", p -> {
            FriendsManager.denyRequest(player.data.name, requestSenderName).thenAccept(outcome -> {
                FriendsManager.sendDenyRequestMessages(outcome, player, requestSenderName);
            });
        });

        form.send(player);
    }

    private static String getStr(String path) {
        return YamlUtil.getStr(PATH + path, Configs.getGlobalConfig());
    }
    
}
