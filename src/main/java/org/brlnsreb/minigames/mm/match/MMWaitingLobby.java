package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.TimeOfDay;
import org.brlnsreb.utils.VotingSystem;

import cn.nukkit.item.Item;

public class MMWaitingLobby extends WaitingLobby {
    
    public MMWaitingLobby(MinigameMatch match) {
        super(match);

        this.timeVoting = new VotingSystem<>();
    }

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            case Item.NETHER_STAR -> 
        }
    }

}
