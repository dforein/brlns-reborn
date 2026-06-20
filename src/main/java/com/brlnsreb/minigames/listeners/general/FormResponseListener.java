package com.brlnsreb.minigames.listeners.general;

import com.brlnsreb.minigames.core.auth.AuthSystem;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.generallobby.ui.GamesMenu;
import com.brlnsreb.minigames.utils.abstraction.MenuAbstract;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.CustomResponse;
import cn.nukkit.form.response.SimpleResponse;

public class FormResponseListener implements Listener {

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        MenuAbstract.removeForm(event.getFormID());
        if (event.getResponse() == null) return;

        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch ((String) event.getWindow().getMeta("type")) {

            case "auth" -> AuthSystem.handleResponse(
                player, 
                (CustomResponse) event.getResponse()
            );

            case "games" -> GamesMenu.handleResponse(
                player, 
                ((SimpleResponse) event.getResponse()).buttonId()
            );
            
        }
    }

}
