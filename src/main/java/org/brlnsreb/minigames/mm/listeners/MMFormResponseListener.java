package org.brlnsreb.minigames.mm.listeners;

import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerFormRespondedEvent;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.response.SimpleResponse;

import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.roles.GamePlayer;
import org.brlnsreb.minigames.mm.roles.MMRole;

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
                if (game.getState() == GameStateType.WAITING_LOBBY || game.getState() == GameStateType.LOBBY_COUNTDOWN) {
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