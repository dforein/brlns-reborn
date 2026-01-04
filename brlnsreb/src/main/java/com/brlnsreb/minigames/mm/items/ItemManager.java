package com.brlnsreb.minigames.mm.items;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.TextFormat;

public class ItemManager {

    public static void giveLobbyItems(Player player, String bookName) {
        giveLobbyItems(player, bookName, "", true);
    }

    public static void giveLobbyItems(Player player, String bookName, String netherStarName) {
        giveLobbyItems(player, bookName, netherStarName, false);
    }

    public static void giveLobbyItems(Player player, String bookName, String netherStarName, Boolean onlyRulesBook) {
        clearInventory(player);
        
        Item book = Item.get(Item.BOOK);
        book.setCustomName(TextFormat.colorize(bookName));
        player.getInventory().setItem(1, book);
        
        if (!onlyRulesBook) {
            Item netherStar = Item.get(Item.NETHER_STAR);
            netherStar.setCustomName(TextFormat.colorize(netherStarName));
            player.getInventory().setItem(7, netherStar);
        }

        player.getInventory().sendContents(player);
    }
    
    public static void giveMurdererItems(Player player, String swordName, String blazeRodName) {
        clearInventory(player);

        player.getInventory().setHeldItemIndex(1);
        
        Item sword = Item.get(Item.IRON_SWORD);
        sword.addEnchantment(Enchantment.get(Enchantment.ID_DAMAGE_ALL).setLevel(5));
        sword.setCustomName(TextFormat.colorize(swordName));
        
        Item blazeRod = Item.get(Item.BLAZE_ROD);
        blazeRod.setCustomName(TextFormat.colorize(blazeRodName));
        
        player.getInventory().setItem(0, sword);
        player.getInventory().setItem(2, blazeRod);
    }
    
    public static void giveSheriffItems(Player player, String hoeName) {
        clearInventory(player);
        
        player.getInventory().setHeldItemIndex(0);

        Item hoe = Item.get(Item.GOLDEN_HOE);
        hoe.setCustomName(TextFormat.colorize(hoeName));
        
        player.getInventory().setItem(1, hoe);
    }

    public static void giveSpectatorItems(Player player, String netherStarName) {
        Item netherStar = Item.get(Item.NETHER_STAR);
        netherStar.setCustomName(TextFormat.colorize(netherStarName));

        if (!player.getInventory().contains(netherStar)) return;

        clearInventory(player);
        
        player.getInventory().setItem(4, netherStar);
        player.getInventory().setHeldItemIndex(0);

        player.getInventory().sendContents(player);
    }
    
    public static void clearInventory(Player player) {
        player.getInventory().clearAll();
        player.getCursorInventory().clearAll();
    }

    public static void giveYellowDye(Player player, String dyeName) {
        Item dye = Item.get(Item.DYE, 11, 1);
        dye.setCustomName(TextFormat.colorize(dyeName));
        
        if (player.getInventory().contains(dye) 
            || player.getInventory().contains(Item.get(Item.GOLDEN_HOE))) {
            return;
        }

        player.getInventory().setItem(2, dye);
    }
}