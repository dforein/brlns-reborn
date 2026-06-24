package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.PlayerStateType;

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

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    public String getConfigPath() { return "waiting-lobby."; }
    
}