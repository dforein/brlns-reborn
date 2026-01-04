package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;

import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerQuitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerQuitListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);

        if (gp != null) {
            if (game.getState() == GameState.IN_GAME || game.getState() == GameState.PREGAME_COUNTDOWN) {
                game.getQuitTracker().markAsQuitted(player.getName());
                
                if (gp.getRole() == MMRole.SHERIFF) {
                    game.getDeath().dropSheriffHoe(player.getLocation());
                }
                
                game.leavePlayer(player);
                game.checkWinCondition();
            } else if (game.getState() == GameState.COUNTDOWN) {
                game.leavePlayer(player);
            }
        }
    }
}