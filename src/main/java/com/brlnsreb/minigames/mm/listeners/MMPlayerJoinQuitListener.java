package com.brlnsreb.minigames.mm.listeners;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.event.player.PlayerKickEvent;
import org.powernukkitx.event.player.PlayerLoginEvent;
import org.powernukkitx.event.player.PlayerJoinEvent;

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
                game.getQuitTracker().markAsQuitted(player.getUniqueId());
                
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

        if (player.getLevel().getId() != Server.getInstance().getDefaultLevel().getId()
            || game.getConfig().getLobbySpawn().distanceSquared(player) > 16) {
            
            game.returnToLobby(player);
        }

        QuitTracker quitTracker = game.getQuitTracker();

        if (quitTracker.hasQuitted(player.getUniqueId())) {
            quitTracker.removePlayer(player.getUniqueId());
        }
    }
}