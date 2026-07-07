package org.brlnsreb.minigames.mm.match.waitinglobby.items;

import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class MMWaitingLobbyItemManager extends WaitingLobbyItemManager {
    
    public MMWaitingLobbyItemManager(Config config) {
        super(config);
    }

    public void giveItemsWaitingPlayers(Player player) {
        giveRulesBook(player);
    }

    public void giveItemsCountdown(Player player) {
        giveRulesBook(player);
        giveGamePoll(player);
    }

    public void giveItemsCountdownShortened(Player player) {
        giveRulesBook(player);
    }
    

    public void giveRulesBook(Player player) {
        giveItem(player, 1, createRulesBook());
    }

}
