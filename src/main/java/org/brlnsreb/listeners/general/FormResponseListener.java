package org.brlnsreb.listeners.general;

import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.generallobby.ui.GamesMenu;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerFormRespondedEvent;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.response.SimpleResponse;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
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
