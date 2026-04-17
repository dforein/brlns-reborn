package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemGoldIngot;
import cn.nukkit.item.ItemGoldenHoe;
import cn.nukkit.utils.TextFormat;

import com.brlnsreb.minigames.core.minigame.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.items.ItemManager;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.ui.BossBarSystem;

public class MMPlayerPickupListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerPickupListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (game.getState() != GameState.IN_GAME && game.getState() != GameState.ENDING) return;

        EntityItem itemEntity = event.getItem();

        if (itemEntity == null || itemEntity.isClosed() || !itemEntity.isValid()) {
            event.setCancelled(true);
            return;     // Lag-caused double pickup -> ignore
        }

        if (!(event.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) event.getInventory().getHolder();

        Item item = itemEntity.getItem();

        // Golds
        if (item instanceof ItemGoldIngot && itemEntity.namedTag.contains("mm_gold")) {
            event.setCancelled(true);
            
            handleGoldPickup(player, itemEntity);
            return;
        }

        // Sheriff Hoe
        if (item instanceof ItemGoldenHoe && itemEntity.namedTag.contains("mm_sheriff_hoe")) {
            event.setCancelled(true);
            
            handleSheriffHoePickup(player, itemEntity);
            return;
        }
    }
    
    private void handleGoldPickup(Player player, EntityItem itemEntity) {

        if (game.getState() != GameState.IN_GAME) return;

        GamePlayer gp = game.getRoleManager().getGamePlayer(player);

        if (gp == null || !gp.isAlive()) return;
        if (gp.getRole() != MMRole.INNOCENT) return;
        
        itemEntity.close();

        MMConfig config = game.getConfig();

        gp.addGold(1);
        gp.addExp(config.getExpPerGold());

        String message = game.getConfig().getMessage("gold-collected");
        player.sendMessage(TextFormat.colorize(message));
        
        if (gp.canBecomeSheriff(config.getGoldForGun()) 
            && game.getRoleManager().isSheriffDead()) {

            ItemManager.giveYellowDye(player, config.getDyeName());
        }

    }
    
    private void handleSheriffHoePickup(Player player, EntityItem itemEntity) {

        if (game.getState() != GameState.IN_GAME) return;
        
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);

        if (gp == null || !gp.isAlive()) return;
        if (gp.getRole() != MMRole.INNOCENT) return;
        
        itemEntity.close();

        if (!game.getRoleManager().isSheriffDead()) return;

        game.getRoleManager().setSheriff(gp);
        if (game.getRoleManager().getSheriff() != gp) return;

        player.getInventory().setHeldItemIndex(0);
        ItemManager.giveSheriffItems(player, game.getConfig().getSheriffHoeName());
        
        for (GamePlayer otherGp : game.getRoleManager().getAllPlayers()) {
            if (otherGp.getRole() == MMRole.INNOCENT && otherGp.isAlive()) {
                Player p = otherGp.getPlayer();
                clearItem(p, Item.YELLOW_DYE);
            }
        }

        BossBarSystem bossBar = game.getBossBar();
        bossBar.hide(player);
        bossBar.showExp(player, game.getRoleManager().getGamePlayer(player).getExpEarned());
        
        MMConfig config = game.getConfig();
        for (Player p : game.getPlayers()) {
            p.sendMessage(TextFormat.colorize(config.getMessage("new-sheriff-chosen")));
        }
        
    }

    private void clearItem(Player player, String itemId) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId().equals(itemId)) {
                player.getInventory().clear(i);
                break;
            }
        }
    }

}