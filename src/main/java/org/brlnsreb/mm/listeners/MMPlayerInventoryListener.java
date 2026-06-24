package org.brlnsreb.mm.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDropItemEvent;

import org.brlnsreb.mm.MurderMysteryGame;
import org.brlnsreb.mm.roles.GamePlayer;
import org.brlnsreb.mm.roles.MMRole;

public class MMPlayerInventoryListener implements Listener {

    private final MurderMysteryGame game;
    
    public MMPlayerInventoryListener(MurderMysteryGame game) {
        this.game = game;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
    
    //UNUSED: replaced with old mechanics due to new features
    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        
        if (gp == null || gp.getRole() != MMRole.SPECTATOR) {
            return;
        }
        
        int newSlot = event.getSlot();
        Item itemInNewSlot = player.getInventory().getItem(newSlot);
        
        if (itemInNewSlot instanceof ItemCompass) {
            event.setCancelled(true);
            
            int previousSlot = event.getSlot() - 1;
            if (previousSlot < 0) {
                previousSlot = 8;
            }
            
            player.getInventory().setHeldItemIndex(previousSlot);
            
            game.getSpectatorMenu().openTeleportMenu(player);
        }
    }*/
}