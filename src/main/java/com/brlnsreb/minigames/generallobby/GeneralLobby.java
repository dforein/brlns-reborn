package com.brlnsreb.minigames.generallobby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.lobby.ui.MainLobbyBossBar;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerUtils;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class GeneralLobby extends Lobby {

    public static GeneralLobby instance;
    private final MainLobbyBossBar bossBar;
    private final MainLobbyItemManager items;
    private final HashMap<NPCEntity, String> npcMap = new HashMap<>();

    public GeneralLobby() {
        super();
        instance = this;

        this.bossBar = new MainLobbyBossBar(this.messages.getString("name"));
        this.items = new MainLobbyItemManager(config);

        this.bossBar.startBossBarUpdates(this.level);
        this.spawnAllNpcs();
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        giveLobbyItems(player);
    }

    public static void giveLobbyItems(CustomPlayer player) {
        PlayerUtils.clearInventory(player);

        
    }

    private void spawnAllNpcs() {
        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String configPath = "lobby.npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            npcMap.put(spawnNpc(
                configPath,
                (Player player) -> { minigame.onLobbyJoin(player); },
                true, minigame
            ), gameNameTag);
        }
    }

    public void reloadConfig() {
        super.reloadConfig(true);

        for (Map.Entry<NPCEntity, String> npc : npcMap.entrySet()) {
            reloadNpcConfigData(
                npc.getKey(), 
                "lobby.npc." + npc.getValue(),
                true, true
            );
        }

        bossBar.reloadConfig(messages.getString("name"));
    }

    public static GeneralLobby getInstance() { return instance; }
    public Config getNewConfig() { 
        return new Config(MinigameCore.getInstance().getDataFolder() + "general-lobby/config.yml", Config.YAML); 
    }
    public Config getNewMessages() {
        return new Config(MinigameCore.getInstance().getDataFolder() + "general-lobby/messages.yml", Config.YAML);
    }
    public String getConfigPath() { return ""; }
    
}
