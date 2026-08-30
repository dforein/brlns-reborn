package org.brlnsreb.mainhub;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.items.MainLobbyItemManager;
import org.brlnsreb.mainhub.messages.MainLobbyMessages;
import org.brlnsreb.mainhub.ui.MainLobbyBossBar;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.utils.Config;

public class MainHub extends Lobby {

    public static final String displayNameTagP = "§l§dHUB§r";
    public static final String displayNameTagY = "§l§eHUB§r";
    public static MainHub instance;

    private final MainLobbyBossBar bossBar;
    private static MainLobbyItemManager items;
    private final MainLobbyMessages lobbyMessages;

    private NPCEntity randomGameNpc;
    private final HashMap<MinigameType, NPCEntity> mgtNpcMap = new HashMap<>();
    private final HologramEntity frontalHolo;

    public MainHub() {
        super();
        instance = this;

        this.bossBar = new MainLobbyBossBar(ChatMsgs.BROKENLENS);
        items = new MainLobbyItemManager(config);
        this.lobbyMessages = new MainLobbyMessages(messages);

        this.bossBar.startBossBarUpdates(map.level);
        this.lobbyMessages.startMessagesRotation(config.getInt(configPath() + "messages-period"));
        this.spawnAllNpcs();

        this.frontalHolo = createHologram("frontal", true);
    }


    //server join

    public void onServerJoin(CustomPlayer player) {
        PlayerUtils.onlinePlayers++;

        PlayerUtils.lobbyTeleport(player, map.spawn);

        onServerJoinMessages(player);

        player.setLobby(this);
        PlayerUtils.setLobbyState(player, null, onJoinState());

        onJoinUi(player);
        onJoinItems(player);
    } 

    protected void onServerJoinMessages(CustomPlayer player) {
        player.sendMessage(         //send disclaimer
            ChatMsgs.BROKENLENS_PFX + YamlUtil.getStr(configPath() + "disclaimer", messages)
        );

        MainLobbyUtils.friendAlertsNotify(player, null, null, false);
    }


    //join

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }

    protected void onJoinMessages(CustomPlayer player) {
        MainLobbyUtils.friendAlertsNotify(player, null, ChatMsgs.BROKENLENS);
    }

    protected void onJoinUi(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        items.giveMainHubItems(player);
    }


    //npcs

    private void spawnAllNpcs() {
        //random minigame npc
        randomGameNpc = spawnNpc(
            "random-game",
            player -> {
                int random = ThreadLocalRandom.current().nextInt(MinigameManager.getMinigames().size());
                MinigameManager.getMinigames().get(random).onLobbyJoin(player);
            }
        );

        //minigame npcs
        for (MinigameType mgt : MinigameType.values()) {
            Minigame npcMinigame = MinigameManager.getMinigame(mgt.nameTag);
            if (npcMinigame == null) {
                BrlnsReb.instance.getLogger().error("§cNo such minigame nametag (from config): " + mgt.nameTag);
                continue;
            }

            NPCEntity npc = spawnNpc(
                mgt.nameTag,
                player -> npcMinigame.onLobbyJoin(player),
                false
            );

            mgtNpcMap.put(mgt, npc);

            BrlnsReb.getScheduler().scheduleRepeatingTask(BrlnsReb.instance, 
                () -> updateNpcSubtitle(npc, npcMinigame), 
                ThreadLocalRandom.current().nextInt(90, 100)
            );
        }
    }

    private void updateNpcSubtitle(NPCEntity npc, Minigame npcMinigame) {
        String subtitle = YamlUtil.getStr(configPath() + "npcs." + npcMinigame.mgt.nameTag + ".text2", config).formatted(
            npcMinigame.getPlayerCount()
        );
        
        npc.updateSubtitle(subtitle);
    }


    //config

    public void onConfigReload() {
        super.onConfigReload();

        reloadNpcConfigData(randomGameNpc, "random-game", false);

        for (Map.Entry<MinigameType, NPCEntity> npcEntry : mgtNpcMap.entrySet()) {
            reloadNpcConfigData(npcEntry.getValue(), npcEntry.getKey().nameTag, false);
            updateNpcSubtitle(npcEntry.getValue(), MinigameManager.getMinigame(npcEntry.getKey()));
        }

        reloadHologramConfigData(frontalHolo, "frontal", true);

        bossBar.onConfigReload(ChatMsgs.BROKENLENS);
        lobbyMessages.onConfigReload();
    }

    public Config getConfig() { 
        return Configs.getConfig("main_hub/config.yml");
    }
    public Config getMessages() {
        return Configs.getConfig("main_hub/messages.yml");
    }
    public String requireConfigPath() { return ""; }
    
}
