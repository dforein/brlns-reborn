package org.brlnsreb.minigames.mm.match.waitinglobby;

import java.util.List;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.waitinglobby.items.MMWaitingLobbyItemManager;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.VotingMapTimeMenu;
import org.brlnsreb.utils.voting.VotingSystem;

import cn.nukkit.item.Item;

public class MMWaitingLobby extends WaitingLobby {

    private VotingMapTimeMenu votingMenu;
    
    public MMWaitingLobby(MinigameMatch match) {
        super(match);

        this.timeVoting = new VotingSystem<>();
    }

    protected void requireVotingMenu() {
        this.votingMenu = new VotingMapTimeMenu(this);
    }

    protected WaitingLobbyItemManager requireItemManager() {
        return new MMWaitingLobbyItemManager(config);
    }

    @Override
    protected void prepareVoting() {
        super.prepareVoting();
        
        if (timeVoting.getAvailableOptions().isEmpty()) {
            timeVoting.setAvailableOptions(List.of(TimeOfDay.values()));
        }
    }

    @Override
    protected void finalizeVoting() {
        super.finalizeVoting();
        
        selectedTime = timeVoting.getMostVoted();
        if (selectedTime == null) {
            selectedTime = TimeOfDay.DAY;
        }
    }

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            case Item.NETHER_STAR -> votingMenu.openMenu(player);
        }
    }

}
