package com.brlnsreb.minigames.generallobby;

import java.util.List;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class GeneralLobby extends Lobby {

    public GeneralLobby(Config config, Config messages) {
        super(config, messages);
        spawnAllNPCs();
    }

    private void spawnAllNPCs() {
        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String path = "npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            spawnNpc(
                path,
                (Player player) -> { minigame.onLobbyJoin(player); },
                //subtitle? check yt
            );
        }
    }

    public boolean onJoin(Player player) {

    }
    
}
