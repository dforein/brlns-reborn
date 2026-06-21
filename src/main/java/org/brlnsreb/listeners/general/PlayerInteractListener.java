package org.brlnsreb.listeners.general;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockButton;
import cn.nukkit.block.BlockCake;
import cn.nukkit.block.BlockCandleCake;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockFarmland;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockLever;
import cn.nukkit.block.BlockPressurePlateBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlazeRod;
import cn.nukkit.item.ItemCompass;
import cn.nukkit.item.ItemGoldenHoe;
import cn.nukkit.item.ItemIronSword;
import cn.nukkit.item.ItemNetherStar;
import cn.nukkit.item.ItemYellowDye;
import cn.nukkit.level.Sound;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.utils.TextFormat;

import org.brlnsreb.MinigameCore;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.mm.MurderMysteryGame;
import org.brlnsreb.mm.config.MMConfig;
import org.brlnsreb.mm.roles.GamePlayer;
import org.brlnsreb.mm.roles.MMRole;
import org.brlnsreb.mm.systems.ItemManager;
import org.brlnsreb.mm.ui.BossBarSystem;

import java.util.*;

public class PlayerInteractListener implements Listener {

    //TODO: interact listener
    
    private final MurderMysteryGame game;
    private static final HashSet<String> INTERACT_BLOCKS = new HashSet<>(Arrays.asList(
            Block.CHEST, Block.TRAPPED_CHEST, Block.ENDER_CHEST, Block.COPPER_CHEST,
            Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER,
            Block.HOPPER, 
            Block.BARREL, 
            Block.BEACON, 
            Block.BREWING_STAND,
            Block.ANVIL, Block.CHIPPED_ANVIL, Block.DAMAGED_ANVIL,
            Block.CARTOGRAPHY_TABLE, 
            Block.CRAFTING_TABLE, 
            Block.CRAFTER,
            Block.DISPENSER, Block.DROPPER, 
            Block.ENCHANTING_TABLE,
            Block.GRINDSTONE, 
            Block.LECTERN,
            Block.LOOM,
            Block.WALL_SIGN, Block.STANDING_SIGN,
            Block.SMITHING_TABLE,
            Block.STONECUTTER,
            Block.DRAGON_EGG
        )
    );
    
    public PlayerInteractListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onItemFrameInteract(ItemFrameUseEvent event) {

        

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getEntity() instanceof EntityArmorStand) {
            
        }

        
        
        if (handleItemInteraction(player, item, gp)) event.setCancelled();

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Block block = event.getBlock();
    
        if (block != null) {
            if (INTERACT_BLOCKS.contains(block.getId()) 
                || block instanceof BlockCake               //TODO: remove later (cake)
                || block instanceof BlockCandleCake) {      //TODO: remove later (cake)

                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.PHYSICAL) {
                if (block instanceof BlockFarmland || block instanceof BlockPressurePlateBase) {

                    event.setCancelled(true);
                    return;
                }
            }
        }

        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        if (gp == null) return;

        if (block != null) {
            if (gp.getRole() == MMRole.SPECTATOR) {
                if (block instanceof BlockDoor
                    || block instanceof BlockFenceGate
                    || block instanceof BlockButton
                    || block instanceof BlockLever) {
                    
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Item item = event.getItem();
        if (item == null) return;

        if ((item instanceof ItemIronSword || item instanceof ItemGoldenHoe) 
            && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        
        if (handleItemInteraction(player, item, gp)) event.setCancelled();

    }

    private boolean handleItemInteraction(Player player, Item item, GamePlayer gp) {
        if (game.getState() == GameStateType.WAITING_LOBBY || game.getState() == GameStateType.LOBBY_COUNTDOWN) {
            if (game.getPlayers().contains(player)) {
                //game poll
                if (item instanceof ItemNetherStar) {
                    String customName = item.getCustomName();
                    if (customName != null && customName.contains("Game Poll")) {
                        game.getVotingMenu().openVotingMenu(player);
                        return true;
                    }
                }
            }

            return false;
        }
        
        if (item instanceof ItemCompass && gp.getRole() == MMRole.SPECTATOR) {
            game.getSpectatorMenu().openTeleportMenu(player);
            return true;
        }

        if (!gp.isAlive()) return false;
        MMConfig config = game.getConfig();

        if (item instanceof ItemGoldenHoe) {
            handleSheriffShoot(player, gp, config);
            return true;
        }

        else if (item instanceof ItemIronSword) {
            handleMurdererThrow(player, gp, config);
            return true;
        }
        
        else if (item instanceof ItemBlazeRod) {
            handleFlash(player, gp, config);
            return true;
        }
        
        else if (item instanceof ItemYellowDye) {
            handleBecomeSheriff(player, gp, config);
            return true;
        }

        return false;
    }

}