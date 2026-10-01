package org.brlnsreb.utils.voting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.brlnsreb.utils.messages.Messages;
import org.powernukkitx.Player;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;

public abstract class VotingForm extends FormAbstract {

    protected Messages msgUtil;

    public VotingForm(WaitingLobby waitingLobby) {
        this.msgUtil = waitingLobby.getMsgUtil();
    }

    public void openForm(Player player) {
        if (!checkCooldown(player)) return;
        
        CustomForm form = new CustomForm("Game Poll");
        addElements(form, player);
        
        form.send(player);
        form.onSubmit((p, response) -> handleResponse(player, response));
    }

    protected abstract void addElements(CustomForm form, Player player);

    protected <T> void addVotingDropdown(
        CustomForm form, Player player, 
        VotingSystem<T> voting, Function<T, String> getDisplayName, String dropdownText
    ) {
        List<T> availableOptions = voting.getAvailableOptions();
        List<String> options = new ArrayList<>();

        options.add("None");                       //"None" option
        for (T option : availableOptions) {              //all randomly selected maps options
            int votes = voting.getVoteCount(option);
            options.add(getDisplayName.apply(option) + " (" + votes + ")");
        }
        
        T pastVote = voting.getPlayerVote(player);
        int defaultIndex;
        if (pastVote != null) {
            defaultIndex = availableOptions.indexOf(pastVote) + 1;
        } else {
            defaultIndex = 0;
        }
        
        form.addDropdown(
            dropdownText,
            options,
            defaultIndex
        );
    }

    protected abstract int handleResponse(Player player, CustomResponse response);  //returns the index of the last element parsed from the response

    protected <T> void handleVotingResponse(
        Player player, CustomResponse response, int dropdownIndex, 
        VotingSystem<T> voting, Function<T, String> getDisplayName, String message
    ) {
        String[] placeholder = new String[1];
        
        int choiceIndex = response.getDropdownResponse(dropdownIndex).elementId();
        if (choiceIndex > 0) {         //if it's zero, the choice was "None"
            T selectedOption = voting.getAvailableOptions().get(choiceIndex - 1);
            voting.vote(player, selectedOption);
            
            placeholder[0] = getDisplayName.apply(selectedOption);
            
            msgUtil.sendMessagePrefix(player, message, placeholder);
        } else {
            voting.removePlayerVote(player);     //in case he voted before
        }
    }
    
}
