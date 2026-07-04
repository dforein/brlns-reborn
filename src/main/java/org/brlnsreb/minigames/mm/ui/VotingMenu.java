package org.brlnsreb.minigames.mm.ui;

import cn.nukkit.Player;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.config.MMConfig;
import org.brlnsreb.utils.TimeOfDay;
import org.brlnsreb.utils.VotingSystem;
import org.brlnsreb.utils.Weather;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import java.util.ArrayList;
import java.util.List;

public class VotingMenu extends MenuAbstract {

    private WaitingLobby waitingLobby;
    private VotingSystem<String> mapVoting;
    private VotingSystem<TimeOfDay> timeVoting;

    public VotingMenu(WaitingLobby waitingLobby) {
        this.waitingLobby = waitingLobby;
        this.mapVoting = waitingLobby.getMapVoting();
        this.timeVoting = waitingLobby.getTimeVoting();
    }
    
    //TODO: REWRITE
    public void openMenu(Player player) {
        checkCooldown(player);

        Config config = waitingLobby.getConfig();
        
        CustomForm menu = new CustomForm("Game Poll");
        
        //map dropdown
        List<String> availableMaps = mapVoting.getAvailableOptions();
        List<String> mapOptions = new ArrayList<>();
        mapOptions.add("None");
        
        for (String map : availableMaps) {
            int votes = voting.getMapVoteCount(map);
            String mapDisplayName = config.getMapDisplayName(map);
            mapOptions.add(mapDisplayName + " (" + votes + ")");
        }
        
        String currentMapVote = voting.getPlayerMapVote(player);
        int mapDefaultIndex = 0;
        if (!currentMapVote.equals("None")) {
            mapDefaultIndex = availableMaps.indexOf(currentMapVote) + 1;
        }
        
        menu.addDropdown(
            TextFormat.colorize("Vote for map:"),
            mapOptions,
            mapDefaultIndex
        );
        
        //time dropdown
        List<String> times = config.getAvailableTimes();
        List<String> timeOptions = new ArrayList<>();
        timeOptions.add("None");
        
        for (String time : times) {
            int votes = voting.getTimeVoteCount(time);
            timeOptions.add(time + " (" + votes + ")");
        }
        
        String currentTimeVote = voting.getPlayerTimeVote(player);
        int timeDefaultIndex = 0;
        if (!currentTimeVote.equals("None")) {
            timeDefaultIndex = times.indexOf(currentTimeVote) + 1;
        }
        
        menu.addDropdown(
            TextFormat.colorize("Vote for time:"),
            timeOptions,
            timeDefaultIndex
        );
        
        menu.send(player);
    }
    
    //TODO: REWRITE
    public void handleVoteResponse(Player player, CustomForm window) {
        if (window.response() == null) return;
        
        MMConfig config = game.getConfig();
        VotingSystem voting = game.getVotingSystem();
        
        int mapIndex = window.response().getDropdownResponse(0).elementId();
        if (mapIndex > 0) {
            String selectedMap = voting.getAvailableMaps().get(mapIndex - 1);
            voting.voteMap(player, selectedMap);
            
            String displayName = config.getMapDisplayName(selectedMap);
            player.sendMessage(TextFormat.colorize(
                config.getMessage("map-vote").replace("{mapName}", displayName)
            ));
        } else {
            voting.removeMapVote(player);
        }
        
        int timeIndex = window.response().getDropdownResponse(1).elementId();
        if (timeIndex > 0) {
            String selectedTime = config.getAvailableTimes().get(timeIndex - 1);
            voting.voteTime(player, selectedTime);
            
            player.sendMessage(TextFormat.colorize(
                config.getMessage("time-vote").replace("{time}", selectedTime)
            ));
        } else {
            voting.removeTimeVote(player);
        }
    }
}