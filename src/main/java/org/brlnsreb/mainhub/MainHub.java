package org.brlnsreb.mainhub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.mainhub.items.MainLobbyItemManager;
import org.brlnsreb.mainhub.ui.MainLobbyBossBar;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.utils.Config;

public class MainHub extends Lobby {

    public static final String displayNameTag = "§l§dHUB§r";
    public static MainHub instance;

    public static int onlinePlayers = 0;

    private final MainLobbyBossBar bossBar;
    private static MainLobbyItemManager items;

    private final HashMap<MinigameType, NPCEntity> mgtNpcMap = new HashMap<>();

    public MainHub() {
        super();
        instance = this;

        this.bossBar = new MainLobbyBossBar(ChatMsgs.BROKENLENS);
        items = new MainLobbyItemManager(config);

        this.bossBar.startBossBarUpdates(this.level);
        this.spawnAllNpcs();
    }

    public void onServerJoin(CustomPlayer player) {
        onlinePlayers++;

        PlayerUtils.lobbyTeleport(player, spawnLoc);

        onServerJoinMessages(player);

        PlayerUtils.setLobbyState(player, null, onJoinState());

        onJoinUi(player);
        onJoinItems(player);
    } 

    protected void onServerJoinMessages(CustomPlayer player) {
        friendAlertsNotify(player, null, null, false);
    }


    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }

    protected void onJoinMessages(CustomPlayer player) {
        friendAlertsNotify(player, null, ChatMsgs.BROKENLENS);
    }

    protected void onJoinUi(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        items.giveMainHubItems(player);
    }

    public static void friendAlertsNotify(CustomPlayer player, Minigame minigame, String minigameName) {
        friendAlertsNotify(player, minigame, minigameName, false);
    }

    public static void friendAlertsNotify(CustomPlayer player, Minigame minigame, String minigameName, boolean serverJoin) {
        PlayerData data = player.data;
        if (!data.isLogged()) return;

        //get the boolean value of alerts (get alerts of friends) and notify (notify friends) 
        boolean alerts = data.getFriendAlerts();
        boolean notify = data.getFriendNotify() && player.minigameCurrent != minigame;
        if (!alerts && !notify) return;
        
        //build the message to send to friends, if notify is enabled
        String notifyMessage = null;
        if (notify && !serverJoin) {
            notifyMessage = ChatMsgs.INFO_PFX + YamlUtil.getStr(
                "lobby.friend-minigame-join", 
                Configs.getGlobalMessages()
            ).formatted(data.name, minigameName);
        }


        List<String> friends = data.getOnlineFriendsKeysCopy();
        
        List<String> hubFriends = new ArrayList<>();
        Map<Minigame, List<String>> minigameGroups = new LinkedHashMap<>();
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
                    hubFriends.add(friend.data.name);
                } else {
                    minigameGroups.computeIfAbsent(friend.minigameCurrent, k -> new ArrayList<>())
                        .add(friend.data.name);
                }
            }
        }

        //send the alerts message to the player, building the message based on what minigame the friends are in
        if (alerts && friendsCount > 0) {
            player.sendMessage(buildAlertsMessage(hubFriends, minigameGroups, friendsCount));
        }
    }

    private static String buildAlertsMessage(List<String> hubFriends, Map<Minigame, List<String>> minigameGroups, int alertsCount) {
        StringBuilder alertsBuilder = new StringBuilder();

        if (!hubFriends.isEmpty()) {
            alertsBuilder.append(" ")
                        .append(MainHub.displayNameTag)
                        .append("§7: §3")
                        .append(String.join("§7, §3", hubFriends));
        }

        for (Map.Entry<Minigame, List<String>> entry : minigameGroups.entrySet()) {
            alertsBuilder.append(" ")
                        .append(entry.getKey().mgt.displayNameTag)
                        .append("§7: §3")
                        .append(String.join("§7, §3", entry.getValue()));
        }

        return ChatMsgs.INFO_PFX + "§d" + alertsCount + " §afriend(s) online:" + alertsBuilder.toString();
    }


    private void spawnAllNpcs() {
        //TODO: random minigame
        for (String mgNameTag : config.getStringList("npc.list")) {
            String configPath = configPath() + "npc." + mgNameTag;
            Minigame npcMinigame = MinigameManager.getMinigame(mgNameTag);
            if (npcMinigame == null) {
                BrlnsReb.instance.getLogger().error("§cNo such minigame nametag (from config): " + mgNameTag);
                continue;
            }

            NPCEntity npc = spawnNpc(
                configPath,
                player -> npcMinigame.onLobbyJoin(player),
                false
            );

            mgtNpcMap.put(MinigameType.fromNameTag(mgNameTag), npc);

            BrlnsReb.getScheduler().scheduleRepeatingTask(BrlnsReb.instance, 
                () -> updateNpcSubtitle(npc, npcMinigame), 
                ThreadLocalRandom.current().nextInt(90, 100)
            );
        }
    }

    private void updateNpcSubtitle(NPCEntity npc, Minigame npcMinigame) {
        String subtitle = YamlUtil.getStr(configPath() + "npc." + npcMinigame.mgt.nameTag + ".text2", config).formatted(
            npcMinigame.getPlayerCount()
        );
        
        npc.updateSubtitle(subtitle);
    }

    public void onConfigReload() {
        super.onConfigReload();

        for (Map.Entry<MinigameType, NPCEntity> npcEntry : mgtNpcMap.entrySet()) {
            reloadNpcConfigData(
                npcEntry.getValue(), 
                configPath() + "npc." + npcEntry.getKey().nameTag,
                false
            );

            updateNpcSubtitle(npcEntry.getValue(), MinigameManager.getMinigame(npcEntry.getKey()));
        }

        bossBar.onConfigReload(ChatMsgs.BROKENLENS);
    }

    public Config getConfig() { 
        return Configs.getConfig("main_hub/config.yml");
    }
    public Config getMessages() {
        return Configs.getConfig("main_hub/messages.yml");
    }
    public String requireConfigPath() { return ""; }
    
}
