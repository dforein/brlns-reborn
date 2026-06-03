package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;

import cn.nukkit.Player;

public abstract class MinigameLobby extends Lobby {

    protected final Minigame minigame;
    protected final NPCEntity joinNpc;
    protected final String joinNpcSubtitle;
    
    public MinigameLobby(Minigame minigame) {
        super(minigame.getConfig(), minigame.getMessages());

        this.minigame = minigame;
        this.joinNpc = spawnJoinNpc(minigame);
        this.joinNpcSubtitle = config.getString("npc.subtitle");
    }

    public void onReplaceMainPendingMatch(int matchNumber) {
        joinNpc.updateSubtitle(
            joinNpcSubtitle.formatted(minigame.getNameTag(), matchNumber)
        );
    }

    private NPCEntity spawnJoinNpc(Minigame minigame) {
        return spawnNpc(
            "lobby.npc",
            (Player player) -> { minigame.onMatchJoin(player); },
            true, minigame
        );
    }

    public void reloadConfig() {
        super.reloadConfig(minigame.getConfig(), minigame.getMessages());
        reloadNpcConfigData(joinNpc, "lobby.npc", true, true);
    }

}
