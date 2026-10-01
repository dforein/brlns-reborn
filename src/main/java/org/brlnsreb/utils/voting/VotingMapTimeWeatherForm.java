package org.brlnsreb.utils.voting;

import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.Player;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;

public class VotingMapTimeWeatherForm extends VotingMapTimeForm {
    
    private VotingSystem<Weather> weatherVoting;

    public VotingMapTimeWeatherForm(WaitingLobby waitingLobby) {
        super(waitingLobby);
        this.weatherVoting = waitingLobby.getWeatherVoting();
    }

    private String getWeatherDisplayName(Weather weather) { return weather.displayName; }

    @Override
    protected void addElements(CustomForm form, Player player) {
        super.addElements(form, player);

        addVotingDropdown(form, player, 
            weatherVoting,
            this::getWeatherDisplayName, 
            "Vote for weather:"
        );
    }

    @Override
    protected int handleResponse(Player player, CustomResponse response) {
        int dropdownIndex = super.handleResponse(player, response);

        handleVotingResponse(player, response, dropdownIndex++, 
            weatherVoting, this::getWeatherDisplayName, 
            YamlUtil.getStr("match.waiting-lobby.voting.weather-vote", Configs.getGlobalMessages())
        );

        return dropdownIndex;
    }

}
