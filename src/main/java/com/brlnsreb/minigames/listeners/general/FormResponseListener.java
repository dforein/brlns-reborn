package com.brlnsreb.minigames.listeners.general;

import com.brlnsreb.minigames.core.auth.AuthSystem;
import com.brlnsreb.minigames.core.player.CustomPlayer;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.CustomForm;

public class FormResponseListener implements Listener {

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (event.getWindow() instanceof CustomForm) {
            CustomForm window = (CustomForm) event.getWindow();
            

            switch (player.state) {
                case LOBBY:
                    String authTitle = AuthSystem.getConfig().getString("auth.menu.title");

                    if (window.title().contains(authTitle)) {
                        AuthSystem.handleResponse(player, window);
                        return;
                    }

                    break;
                
                case WAITING_LOBBY:
                    if (window.title().contains("Game Poll")) {

                        return;
                    }
                default:
                    break;
            }
        }
    }

}
