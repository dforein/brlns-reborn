package com.brlnsreb.minigames.core.minigame.lobby;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.Minigame;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final String nameTag;
    protected final NPCEntity joinNpc;
    protected final String joinNpcSubtitle;
    
    public MinigameLobby(Config config, Minigame minigame) {
        super(config);
        this.nameTag = minigame.getNameTag();
        this.joinNpc = spawnJoinNpc(minigame);
        this.joinNpcSubtitle = config.getString("npc.subtitle");

    }

    public void onReplaceMainPendingMatch(int matchNumber) {
        joinNpc.updateSubTitle(
            joinNpcSubtitle.formatted(this.nameTag, matchNumber)
        );
    }

    private NPCEntity spawnJoinNpc(Minigame minigame) {
        return spawnNpc(
            "npc",
            (Player player) -> { minigame.onMatchJoin(player); }
        );
    }

}
