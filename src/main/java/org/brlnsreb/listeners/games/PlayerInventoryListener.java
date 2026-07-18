package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.inventory.InventoryPickupItemEvent;
import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerInventoryListener implements Listener {
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch (player.state) {
            case WAITING_LOBBY, PLAYING -> {
                if (!player.matchCurrent.onItemDrop(player, event)) event.setCancelled();
            }
            default -> event.setCancelled();
        }
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent event) {
        Entity entity = (Entity) event.getInventory().getHolder();

        if (event.getInventory().getHolder() instanceof CustomPlayer) {
            CustomPlayer player = (CustomPlayer) entity;
            switch (player.state) {
                case PLAYING -> { 
                    if (!player.matchCurrent.getGame().onItemPickup(player, event.getItem())) event.setCancelled();
                }
                default -> event.setCancelled();
            }
        } else {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch (player.state) {
            case WAITING_LOBBY, PLAYING -> {
                if (!player.matchCurrent.onItemHeld(player, event)) event.setCancelled();
            }
            default -> {}
        }
    }

}
