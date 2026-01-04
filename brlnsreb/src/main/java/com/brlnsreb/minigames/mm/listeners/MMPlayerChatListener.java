package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerChatListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerChatListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(PlayerChatEvent event) {
        if (game.getState() != GameState.IN_GAME) return;
        
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        
        if (gp == null) return;

        if (gp.getRole() == MMRole.MURDERER || 
            gp.getRole() == MMRole.SHERIFF || 
            gp.getRole() == MMRole.SPECTATOR) {
            //event.setCancelled(true);
            //player.sendMessage(TextFormat.colorize(game.getConfig().getMessage("no-chat")));  //TODO: remove commenting
        }
    }
}