package org.brlnsreb.generallobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.generallobby.items.MainLobbyItemManager;
import org.brlnsreb.generallobby.ui.MainLobbyBossBar;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.Server;
import org.powernukkitx.utils.Config;

public class GeneralLobby extends Lobby {

    public static final String displayNameTag = "§l§dHUB§r";
    
    public static GeneralLobby instance;

    private final MainLobbyBossBar bossBar;
    private static MainLobbyItemManager items;

    private final HashMap<NPCEntity, String> npcNameTagMap = new HashMap<>();

    public GeneralLobby() {
        super();
        instance = this;

        this.bossBar = new MainLobbyBossBar(messages.getString("name"));
        items = new MainLobbyItemManager(config);

        this.bossBar.startBossBarUpdates(this.level);
        this.spawnAllNpcs();
    }

    public void onServerJoin(CustomPlayer player) {
        PlayerUtils.changeWorld(player, spawnPos, true);

        onServerJoinMessages(player);

        PlayerUtils.setLobbyState(player, onJoinState());

        onJoinBossBar(player);
        onJoinItems(player);
    } 

    protected void onServerJoinMessages(CustomPlayer player) {
        friendAlertsNotify(player, null, "");
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }

    protected void onJoinMessages(CustomPlayer player) {
        friendAlertsNotify(player, null, ChatMsgs.brokenlens);
    }

    public static void friendAlertsNotify(CustomPlayer player, Minigame minigame, String notifyMsgMinigame) {
        PlayerData data = player.getPlayerData();
        if (!data.isLogged()) return;

        boolean notify = data.getFriendNotify() && !player.currentMinigame.equals(minigame);
        boolean alerts = data.getFriendAlerts();

        String notifyMessage = notify 
            ? ChatMsgs.infoPfx
                + YamlUtil.getStr("lobby.friend-joined", ConfigManager.getGlobalMessages())
                    .formatted(data.name, notifyMsgMinigame)
            : null;

        Map<Minigame, List<String>> minigameGroups = new LinkedHashMap<>();
        List<String> hubFriends = new ArrayList<>();
        int alertsCount = 0;

        List<String> friends = data.getOnlineFriendsKeysCopy();

        for (String friendName : friends) {
            CustomPlayer friend = data.getFriend(friendName);
            if (friend == null) continue;

            if (notify) {
                friend.sendMessage(notifyMessage);
            }

            if (alerts) {
                alertsCount++;
                if (friend.currentMinigame == null) {
                    hubFriends.add(friend.getPlayerData().name);
                } else {
                    minigameGroups.computeIfAbsent(friend.currentMinigame, k -> new ArrayList<>())
                        .add(friend.getPlayerData().name);
                }
            }
        }

        if (alerts && alertsCount > 0) {
            StringBuilder alertsBuilder = new StringBuilder();

            if (!hubFriends.isEmpty()) {
                alertsBuilder.append(" ")
                            .append(GeneralLobby.displayNameTag)
                            .append("§7: §3")
                            .append(String.join("§7, §3", hubFriends));
            }

            for (Map.Entry<Minigame, List<String>> entry : minigameGroups.entrySet()) {
                alertsBuilder.append(" ")
                            .append(entry.getKey().mgt.displayNameTag)
                            .append("§7: §3")
                            .append(String.join("§7, §3", entry.getValue()));
            }

            String alertsMessage = ChatMsgs.infoPfx + "§d" + alertsCount + " §afriend(s) online:" + alertsBuilder.toString();
            player.sendMessage(alertsMessage);
        }
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        items.giveGeneralLobbyItems(player);
    }

    private void spawnAllNpcs() {
        for (String gameNameTag : config.getStringList("npc.list")) {
            String configPath = "lobby.npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            NPCEntity npc = spawnNpc(
                configPath,
                (CustomPlayer player) -> { minigame.onLobbyJoin(player); },
                false
            );

            npcNameTagMap.put(npc, gameNameTag);

            Server.getInstance().getScheduler().scheduleRepeatingTask(BrlnsReb.getInstance(), 
                () -> {
                    updateNpcSubtitle(npc);
                }, 100
            );
        }
    }

    private void updateNpcSubtitle(NPCEntity npc) {
        String subtitle = YamlUtil.getStr(configPath() + "npc.text2", config).formatted(
            minigame.mgt.nameTag,
            minigame.getMainPendingMatch().getNumber(),
            minigame.getMainPendingMatch().getPlayers().size()
        );
        
        npc.updateSubtitle(subtitle);
    }

    public void onConfigReload() {
        super.onConfigReload();

        for (Map.Entry<NPCEntity, String> npcEntry : npcNameTagMap.entrySet()) {
            reloadNpcConfigData(
                npcEntry.getKey(), 
                configPath() + "npc." + npcEntry.getValue(),
                false
            );
            updateNpcSubtitle(npcEntry.getKey());
        }

        bossBar.onConfigReload(messages.getString("name"));
    }

    public static GeneralLobby getInstance() { return instance; }
    public Config getConfig() { 
        return ConfigManager.getConfig("general-lobby/config.yml");
    }
    public Config getMessages() {
        return ConfigManager.getConfig("general-lobby/messages.yml");
    }
    public String requireConfigPath() { return ""; }
    
}
