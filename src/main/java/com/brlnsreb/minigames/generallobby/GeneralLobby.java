package com.brlnsreb.minigames.generallobby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerUtils;
import com.brlnsreb.minigames.utils.LobbyBossBar;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class GeneralLobby extends Lobby {

    public static GeneralLobby instance;
    private final LobbyBossBar bossBar;
    private final HashMap<NPCEntity, String> npcMap = new HashMap<>();

    public GeneralLobby(MinigameCore plugin, Config config, Config messages) {
        super(config, messages);
        instance = this;

        this.bossBar = new LobbyBossBar(
            plugin, 
            this.messages.getString("name"),
            new Config(plugin.getDataFolder() + "global/messages.yml", Config.YAML)     //for global lobby-bossbar messages (not just general lobby, all lobbies)
        );

        this.bossBar.startBossBarUpdates(this.level);
        this.spawnAllNpcs();
    }

    public boolean onJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;

        PlayerUtils.changeWorld(p, spawnPos);
        PlayerUtils.setLobbyState(p);
        
        bossBar.updateLobbyBossBar(p);
        //TODO: items

        return true;
    }

    private void spawnAllNpcs() {
        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String path = "lobby.npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            npcMap.put(spawnNpc(
                path,
                (Player player) -> { minigame.onLobbyJoin(player); },
                true, minigame
            ), gameNameTag);
        }
    }

    @Override
    public void reloadConfig(Config config, Config messages) {
        super.reloadConfig(config, messages);

        for (Map.Entry<NPCEntity, String> npc : npcMap.entrySet()) {
            reloadNpcConfigData(
                npc.getKey(), 
                "lobby.npc." + npc.getValue(),
                true, true
            );
        }
    }

    public static GeneralLobby getInstance() { return instance; }
    
}
