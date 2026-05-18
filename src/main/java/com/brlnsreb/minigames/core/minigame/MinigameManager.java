package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.core.auth.AuthMenu;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerDataManager;
import com.brlnsreb.minigames.mm.MurderMystery;

import cn.nukkit.Player;

public class MinigameManager {

    private static MinigameManager instance;

    public MinigameManager() {
        instance = this;
    }
    
    private final Minigame[] minigames = {
        new MurderMystery(MinigameType.MURDER_MYSTERY)
    };

    public boolean onJoin(Player player, MinigameType minigame) {
        CustomPlayer p = (CustomPlayer) player;

        if (p.getPlayerData().name == null) {
            PlayerDataManager.getAuth().openMenu(p);
            return false;
        }

        return this.getMinigame(minigame).onLobbyJoin(p);
    }

    public void forceStop() {
        for (Minigame mg : minigames) {
            for (MinigameMatch match : mg.getMatches()) {
                match.getGame().forceStop();
            }
        }
    }

    public Minigame getMinigame(MinigameType minigame) {
        for (Minigame mg : minigames) {
            if (mg.getId() == minigame.getId()) return mg;
        }
        return null;
    }

    public static MinigameManager getInstance() { return instance; }

}
