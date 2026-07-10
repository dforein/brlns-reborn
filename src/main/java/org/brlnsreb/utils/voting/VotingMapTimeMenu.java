package org.brlnsreb.utils.voting;

import org.powernukkitx.Player;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import java.util.ArrayList;
import java.util.List;

public class VotingMapTimeMenu extends MenuAbstract {

    private VotingSystem<String> mapVoting;
    private VotingSystem<TimeOfDay> timeVoting;
    private Config config;
    private Messages msgUtil;

    public VotingMapTimeMenu(WaitingLobby waitingLobby) {
        this.mapVoting = waitingLobby.getMapVoting();
        this.timeVoting = waitingLobby.getTimeVoting();
        this.config = waitingLobby.getConfig();
        this.msgUtil = waitingLobby.getMsgUtil();
    }
    
    public void openMenu(Player player) {
        checkCooldown(player);
        
        CustomForm menu = new CustomForm("Game Poll");
        
        //map dropdown
        List<String> availableMaps = mapVoting.getAvailableOptions();
        List<String> mapOptions = new ArrayList<>();

        mapOptions.add("None");                       //"None" option
        for (String map : availableMaps) {              //all randomly selected maps options
            int votes = mapVoting.getVoteCount(map);
            String mapDisplayName = YamlUtil.getStr("map-settings.maps." + map + ".name", config);
            mapOptions.add(mapDisplayName + " (" + votes + ")");
        }
        
        String pastMapVote = mapVoting.getPlayerVote(player);
        int mapDefaultIndex;
        if (pastMapVote != null) {
            mapDefaultIndex = availableMaps.indexOf(pastMapVote) + 1;
        } else {
            mapDefaultIndex = 0;
        }
        
        menu.addDropdown(
            TextFormat.colorize("Vote for map:"),
            mapOptions,
            mapDefaultIndex
        );
        
        //time dropdown
        List<TimeOfDay> availableTimes = timeVoting.getAvailableOptions();
        List<String> timeOptions = new ArrayList<>();

        timeOptions.add("None");
        for (TimeOfDay time : availableTimes) {
            int votes = timeVoting.getVoteCount(time);
            timeOptions.add(time.displayName + " (" + votes + ")");
        }
        
        TimeOfDay pastTimeVote = timeVoting.getPlayerVote(player);
        int timeDefaultIndex;
        if (pastTimeVote != null) {
            timeDefaultIndex = availableTimes.indexOf(pastTimeVote) + 1;
        } else {
            timeDefaultIndex = 0;
        }
        
        menu.addDropdown(
            TextFormat.colorize("Vote for time:"),
            timeOptions,
            timeDefaultIndex
        );
        
        menu.send(player);
    }
    
    public void handleVoteResponse(Player player, CustomForm window) {
        if (window.response() == null) return;

        String[] placeholder = new String[1];
        
        int mapIndex = window.response().getDropdownResponse(0).elementId();
        if (mapIndex > 0) {         //if it's zero, the choice was "None"
            String selectedMap = mapVoting.getAvailableOptions().get(mapIndex - 1);
            mapVoting.vote(player, selectedMap);
            
            placeholder[0] = YamlUtil.getStr("map-settings.maps." + selectedMap + ".name", config);
            msgUtil.sendPresetMessagePrefix("map-vote", player, placeholder);
        } else {
            mapVoting.removePlayerVote(player);     //in case he voted before
        }
        
        int timeIndex = window.response().getDropdownResponse(1).elementId();
        if (timeIndex > 0) {
            TimeOfDay selectedTime = timeVoting.getAvailableOptions().get(timeIndex - 1);
            timeVoting.vote(player, selectedTime);
            
            placeholder[0] = selectedTime.displayName;
            msgUtil.sendPresetMessagePrefix("time-vote", player, placeholder);
        } else {
            timeVoting.removePlayerVote(player);
        }
    }

}