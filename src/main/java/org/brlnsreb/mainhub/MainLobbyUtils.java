package org.brlnsreb.mainhub;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;

public class MainLobbyUtils {

    //friends: alerts and notify
    
    public static void friendAlertsNotify(CustomPlayer player, Minigame minigame, String minigameName) {
        friendAlertsNotify(player, minigame, minigameName, false);
    }

    public static void friendAlertsNotify(CustomPlayer player, Minigame minigame, String minigameName, boolean serverJoin) {
        PlayerData data = player.data;
        if (!data.isLogged()) return;

        //get the boolean value of alerts (get alerts of friends) and notify (notify friends) 
        boolean alerts = data.getFriendAlerts();
        boolean notify = !serverJoin && data.getFriendNotify() && player.minigameCurrent != minigame;
        if (!alerts && !notify) return;
        
        //build the message to send to friends, if notify is enabled
        String notifyMessage = null;
        if (notify) {
            notifyMessage = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-minigame-join", 
                Configs.getGlobalMessages()
            ).formatted(data.name, minigameName);
        }


        List<String> friends = data.getOnlineFriendsKeysCopy();
        
        List<String> friendsInHub = new ArrayList<>();
        Map<Minigame, List<String>> friendsInMinigames = new LinkedHashMap<>();
        int friendsCount = 0;

        for (String friendName : friends) {
            CustomPlayer friend = data.getFriend(friendName);
            if (friend == null) continue;

            //send the notify message to all friends
            if (notify) friend.sendMessage(notifyMessage);

            // + categorize all the friends based on what minigame they are in (else they get put in hubFriends)
            if (alerts) {
                friendsCount++;
                if (friend.minigameCurrent == null) {
                    friendsInHub.add(friend.data.name);
                } else {
                    friendsInMinigames.computeIfAbsent(friend.minigameCurrent, k -> new ArrayList<>())
                        .add(friend.data.name);
                }
            }
        }

        //send the alerts message to the player, building the message based on what minigame the friends are in
        if (alerts && friendsCount > 0) {
            player.sendMessage(buildAlertsMessage(friendsInHub, friendsInMinigames, friendsCount));
        }
    }

    private static String buildAlertsMessage(List<String> hubFriends, Map<Minigame, List<String>> friendsInMinigames, int alertsCount) {
        StringBuilder alertsBuilder = new StringBuilder();

        if (!hubFriends.isEmpty()) {
            alertsBuilder.append(" ")
                        .append(MainHub.displayNameTagP)
                        .append("§7: §3")
                        .append(String.join("§7, §3", hubFriends));
        }

        for (Map.Entry<Minigame, List<String>> entry : friendsInMinigames.entrySet()) {
            alertsBuilder.append(" ")
                        .append(entry.getKey().mgt.displayNameTagP)
                        .append("§7: §3")
                        .append(String.join("§7, §3", entry.getValue()));
        }

        return ChatMsgs.INFO_PFX + "§d" + alertsCount + " §afriend(s) online:" + alertsBuilder.toString();
    }

}
