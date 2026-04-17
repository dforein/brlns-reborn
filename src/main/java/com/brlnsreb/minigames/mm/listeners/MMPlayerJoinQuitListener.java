package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerJoinEvent;

import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.core.minigame.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.systems.QuitTracker;

public class MMPlayerJoinQuitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerJoinQuitListener(MurderMysteryGame game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        handlePlayerLeave(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        handlePlayerLeave(event.getPlayer());
    }

    private void handlePlayerLeave(Player player) {
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);

        if (gp != null) {
            if (game.getState() == GameState.IN_GAME || game.getState() == GameState.PREGAME_COUNTDOWN) {
                game.getQuitTracker().markAsQuitted(player.getName());
                
                if (gp.getRole() == MMRole.SHERIFF) {
                    game.getDeath().dropSheriffHoe(player.getLocation());
                }
                
                game.leavePlayer(player);

                if (gp.isAlive()) {
                    game.checkWinCondition();
                }
            } else {
                game.leavePlayer(player);
            }
        }
    }

    @EventHandler
    public void onPreJoin(PlayerLoginEvent event) {
        Level lobby = game.getPlugin().getServer().getLevelByName(game.getConfig().getLobbyWorld());
        Vector3 pos = game.getConfig().getLobbySpawn();

        int cx = pos.getFloorX() >> 4;
        int cz = pos.getFloorZ() >> 4;
        
        if (!lobby.isChunkLoaded(cx, cz)) {
            lobby.loadChunk(cx, cz);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player =  event.getPlayer();
        
        game.refreshPlayerState(player, true);
        game.returnToLobby(player);

        player.getFoodData().setEnabled(false);

        QuitTracker quitTracker = game.getQuitTracker();

        if (quitTracker.hasQuitted(player.getName())) {
            quitTracker.removePlayer(player.getName());
        }
    }
}