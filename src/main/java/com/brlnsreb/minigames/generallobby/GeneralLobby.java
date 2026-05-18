package com.brlnsreb.minigames.generallobby;

import java.util.List;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.minigame.MinigameType;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class GeneralLobby extends Lobby {

    public GeneralLobby(Config config) {
        super(config);
        spawnAllNPCs();
    }

    private void spawnAllNPCs() {
        MinigameManager minigameManager = MinigameManager.getInstance();

        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String path = "npc." + gameNameTag;
            MinigameType minigame = MinigameType.fromNameTag(gameNameTag);

            spawnNpc(
                path,
                (Player player) -> { minigameManager.onJoin(player, minigame); }
            );
        }
    }

    public boolean onJoin(Player player) {

    }
    
}
