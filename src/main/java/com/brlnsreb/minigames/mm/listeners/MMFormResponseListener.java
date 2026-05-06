package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.form.response.SimpleResponse;

import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMFormResponseListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMFormResponseListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();

        if (event.getWindow() instanceof CustomForm) {
            CustomForm window = (CustomForm) event.getWindow();
            
            if (window.title().contains("Game Poll")) {
                if (game.getState() == GameState.WAITING_LOBBY || game.getState() == GameState.LOBBY_COUNTDOWN) {
                    game.getVotingMenu().handleVoteResponse(player, window);
                }
                return;
            }
        }
        
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        if (gp == null || gp.getRole() != MMRole.SPECTATOR) return;
        
        if (event.getResponse() instanceof SimpleResponse) {
            SimpleResponse response = (SimpleResponse) event.getResponse();
            
            int buttonId = response.buttonId();
            if (buttonId >= 0) {
                game.getSpectatorMenu().handleResponse(player, buttonId);
            }
        }
    }
}