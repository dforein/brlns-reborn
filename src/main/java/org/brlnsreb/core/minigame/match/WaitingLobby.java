package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

public abstract class WaitingLobby extends Lobby {

    protected final NPCEntity leaveNpc;

    public WaitingLobby(Minigame minigame) {
        super(minigame);

        this.leaveNpc = spawnNpc(
            "waiting-lobby.npc.", 
            (Player player) -> { minigame.onLobbyJoin(player); }
        );
    }

    public void onItemUse(CustomPlayer player, Item item) {

    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    public String getConfigPath() { return "waiting-lobby."; }
    
}