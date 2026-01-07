package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.form.element.custom.ElementDropdown;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;

import java.util.ArrayList;
import java.util.List;

public class VotingMenu {
    
    private final MurderMysteryGame game;
    
    public VotingMenu(MurderMysteryGame game) {
        this.game = game;
    }
    
    public void openVotingMenu(Player player) {
        MMConfig config = game.getConfig();
        VotingSystem voting = game.getVotingSystem();
        
        CustomForm menu = new CustomForm("Game Poll");
        
        //map dropdown
        List<String> availableMaps = voting.getAvailableMaps();
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
        
        ElementDropdown mapDropdown = new ElementDropdown(
            TextFormat.colorize("Vote for map:"),
            mapOptions,
            mapDefaultIndex
        );
        menu.addElement(mapDropdown);
        
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
        
        ElementDropdown timeDropdown = new ElementDropdown(
            TextFormat.colorize("Vote for time:"),
            timeOptions,
            timeDefaultIndex
        );
        menu.addElement(timeDropdown);
        
        menu.send(player);
    }
    
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
            voting.removePlayerVotes(player);
        }
        
        int timeIndex = window.response().getDropdownResponse(1).elementId();
        if (timeIndex > 0) {
            String selectedTime = config.getAvailableTimes().get(timeIndex - 1);
            voting.voteTime(player, selectedTime);
            
            player.sendMessage(TextFormat.colorize(
                config.getMessage("time-vote").replace("{time}", selectedTime)
            ));
        }
        
        // reopen menu
        /*
        game.getPlugin().getServer().getScheduler().scheduleDelayedTask(
            game.getPlugin(),
            () -> openVotingMenu(player),
            10
        );*/
    }
}