package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.item.Item;

import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerInventoryListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerInventoryListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        
        if (gp == null || gp.getRole() != MMRole.SPECTATOR) {
            return;
        }
        
        int newSlot = event.getSlot();
        Item itemInNewSlot = player.getInventory().getItem(newSlot);
        
        if (itemInNewSlot.getId().equals(Item.COMPASS)) {
            event.setCancelled(true);
            
            int previousSlot = event.getSlot() - 1;
            if (previousSlot < 0) {
                previousSlot = 8;
            }
            
            player.getInventory().setHeldItemIndex(previousSlot);
            
            game.getSpectatorMenu().openTeleportMenu(player);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}