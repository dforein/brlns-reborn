package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerJoinEvent;

import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.systems.QuitTracker;

public class MMPlayerJoinQuitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerJoinQuitListener(MurderMysteryGame game) {
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
            } else {
                game.leavePlayer(player);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player =  event.getPlayer();
        QuitTracker quitTracker = game.getQuitTracker();

        if (quitTracker.hasQuitted(player.getName()) || game.getState() == GameState.LOBBY) {
            game.refreshPlayerState(player, true);
            game.returnToLobby(player);

            if (quitTracker.hasQuitted(player.getName())) {
                quitTracker.removePlayer(player.getName());
            }
        }
    }
}