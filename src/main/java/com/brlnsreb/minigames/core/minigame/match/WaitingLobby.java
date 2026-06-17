package com.brlnsreb.minigames.core.minigame.match;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.Minigame;

import cn.nukkit.Player;

public abstract class WaitingLobby extends Lobby {

    protected final NPCEntity leaveNpc;

    public WaitingLobby(Minigame minigame) {
        super(minigame);

        this.leaveNpc = spawnNpc(
            "waiting-lobby.npc.", 
            (Player player) -> { minigame.onLobbyJoin(player); }
        );
    }

    public String getConfigPath() { return "waiting-lobby."; }
    
}