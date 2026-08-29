package org.brlnsreb.minigames.mm.match.waitinglobby;

import java.util.List;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.waitinglobby.items.MMWaitingLobbyItemManager;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.voting.VotingMapTimeMenu;
import org.brlnsreb.utils.voting.VotingSystem;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;

public class MMWaitingLobby extends WaitingLobby {

    private VotingMapTimeMenu votingMenu;
    
    public MMWaitingLobby(Match match) {
        super(match);
    }

    protected void initVotingSystems() {
        this.mapVoting = new VotingSystem<>();
        this.timeVoting = new VotingSystem<>();
    }

    protected void requireVotingMenu() {
        this.votingMenu = new VotingMapTimeMenu(this);
    }

    protected WaitingLobbyItemManager requireItemManager() {
        return new MMWaitingLobbyItemManager(config);
    }

    @Override
    public void onLeave(CustomPlayer player) {
        timeVoting.removePlayerVote(player);
        super.onLeave(player);
    }

    @Override
    protected void prepareVoting() {
        super.prepareVoting();
        
        if (timeVoting.getAvailableOptions() == null) {
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

    public boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event) {
        return true;
    }

}
