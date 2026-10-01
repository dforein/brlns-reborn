package org.brlnsreb.utils.voting;

import org.powernukkitx.Player;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.utils.Config;

import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;

public class VotingMapTimeForm extends VotingForm {

    private VotingSystem<String> mapVoting;
    private Config mapSettings;

    private VotingSystem<TimeOfDay> timeVoting;

    public VotingMapTimeForm(WaitingLobby waitingLobby) {
        super(waitingLobby);
        this.mapVoting = waitingLobby.getMapVoting();
        this.mapSettings = waitingLobby.getMapSettings();

        this.timeVoting = waitingLobby.getTimeVoting();

    }

    private String getMapDisplayName(String mapId) {
        return YamlUtil.getStr("maps." + mapId + ".name", mapSettings);
    }
    private String getTimeDisplayName(TimeOfDay time) { return time.displayName; }

    protected void addElements(CustomForm form, Player player) {
        addVotingDropdown(form, player, 
            mapVoting, 
            this::getMapDisplayName,
            "Vote for map:"
        );

        addVotingDropdown(form, player, 
            timeVoting,
            this::getTimeDisplayName, 
            "Vote for time:"
        );
    }
    
    protected int handleResponse(Player player, CustomResponse response) {
        int dropdownIndex = 0;

        handleVotingResponse(player, response, dropdownIndex++, 
            mapVoting, this::getMapDisplayName, 
            YamlUtil.getStr("match.waiting-lobby.voting.map-vote", Configs.getGlobalMessages())
        );

        handleVotingResponse(player, response, dropdownIndex++, 
            timeVoting, this::getTimeDisplayName, 
            YamlUtil.getStr("match.waiting-lobby.voting.time-vote", Configs.getGlobalMessages())
        );

        return dropdownIndex;
    }

}