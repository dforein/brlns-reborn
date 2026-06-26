package org.brlnsreb.listeners.general;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockButton;
import cn.nukkit.block.BlockCake;
import cn.nukkit.block.BlockCandleCake;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockFarmland;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockLever;
import cn.nukkit.block.BlockPressurePlateBase;
import cn.nukkit.block.BlockTrapdoor;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.item.Item;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.event.player.PlayerBucketFillEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.plugin.annotation.EventListener;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.generallobby.items.MainLobbyItemManager;

@EventListener
public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        checkItemInteraction(player, event.getAction(), event.getItem());
        
        switch (player.interactMode) {
            case FULL:
                return;
            
            case LIMITED, ONLY_PLAYER_BLOCKS:
                Block block = event.getBlock();
                if (block != null) {
                    if (block instanceof BlockDoor
                        || block instanceof BlockFenceGate
                        || block instanceof BlockTrapdoor
                        || block instanceof BlockButton
                        || block instanceof BlockLever
                    ) return;

                    if (player.state != PlayerStateType.LOBBY
                        && (block instanceof BlockCake || block instanceof BlockCandleCake)
                    ) return;

                    if (event.getAction() == Action.PHYSICAL) {
                        if (block instanceof BlockFarmland 
                            || block instanceof BlockPressurePlateBase) {
                            event.setCancelled();
                        }
                    } else {
                        event.setCancelled();
                    }
                }
                return;

            case NOTHING:
                event.setCancelled();
                return;
        }
    }

    private void checkItemInteraction(CustomPlayer player, Action action, Item item) {
        if (item == null) return;
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        if (player.state == PlayerStateType.LOBBY) {
            MainLobbyItemManager.getInstance().onItemUse(player, item);
        } else {
            if (player.getMatch() != null) {
                player.getMatch().onItemUse(player, item);
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player.interactMode != InteractMode.FULL) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onInteractItemFrame(ItemFrameUseEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player != null && 
            player.interactMode != InteractMode.FULL
        ) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onInteractBucketFill(PlayerBucketFillEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player.interactMode != InteractMode.FULL) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onInteractBucketEmpty(PlayerBucketEmptyEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player.interactMode != InteractMode.FULL) {
            event.setCancelled();
        }
    }

}