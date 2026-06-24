package org.brlnsreb.minigames.mm.systems;
import org.brlnsreb.BrlnsReb;

import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.TextFormat;

public class ItemManager {

    public static void giveLobbyItems(Player player, String bookName, BrlnsReb plugin) {
        giveLobbyItems(player, bookName, "", true, plugin);
    }

    public static void giveLobbyItems(Player player, String bookName, String netherStarName, BrlnsReb plugin) {
        giveLobbyItems(player, bookName, netherStarName, false, plugin);
    }

    public static void giveLobbyItems(Player player, String bookName, String netherStarName, Boolean onlyRulesBook, BrlnsReb plugin) {
        clearInventory(player);
        
        Item book = createRulesBook(plugin);
        player.getInventory().setItem(1, book);
        
        if (!onlyRulesBook) {
            Item netherStar = Item.get(Item.NETHER_STAR);
            netherStar.setCustomName(TextFormat.colorize(netherStarName));
            player.getInventory().setItem(7, netherStar);
        }

        player.getInventory().sendContents(player);
    }

    private static Item createRulesBook(BrlnsReb plugin) {
        String title = plugin.getConfig().getString("lobby.rules-book.title", "Murder Mystery Rules");
        String author = plugin.getConfig().getString("lobby.rules-book.author", "Server");
        List<String> pages = plugin.getConfig().getStringList("lobby.rules-book.pages");
        
        if (pages.isEmpty()) {
            pages.add("§0No rules configured!");
        }
        
        Item book = Item.get(Item.WRITTEN_BOOK);
        
        CompoundTag tag = book.getNbt();
        if (tag == null) {
            tag = new CompoundTag();
        }
        
        tag.putString("title", TextFormat.colorize(title));
        tag.putString("author", TextFormat.colorize(author));
        tag.putInt("generation", 0);
        tag.putString("xuid", "");
        
        ListTag<CompoundTag> pagesList = new ListTag<>();
        
        for (String pageContent : pages) {
            CompoundTag pageTag = new CompoundTag();
            pageTag.putString("photoname", "");
            pageTag.putString("text", TextFormat.colorize(pageContent));
            pagesList.add(pageTag);
        }
        
        tag.putList("pages", pagesList);
        book.setNbt(tag);
        book.setCustomName(TextFormat.colorize("&o&l&fRules &7- Hold / Right Click"));
        
        return book;
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

    public static void giveSpectatorItems(Player player, String compassName) {
        Item compass = Item.get(Item.COMPASS);
        compass.setCustomName(TextFormat.colorize(compassName));

        if (player.getInventory().contains(compass)) return;

        clearInventory(player);
        
        player.getInventory().setItem(4, compass);
        player.getInventory().setHeldItemIndex(0);

        player.getInventory().sendContents(player);
    }
    
    public static void clearInventory(Player player) {
        if (!player.isOnline()) return;
        
        player.getInventory().clearAll();
        player.getCursorInventory().clearAll();

        player.getInventory().sendContents(player);
        player.getCursorInventory().sendContents(player);
    }

    public static void giveYellowDye(Player player, String dyeName) {
        Item dye = Item.get(Item.YELLOW_DYE);
        dye.setCustomName(TextFormat.colorize(dyeName));
        
        if (player.getInventory().contains(dye) 
            || player.getInventory().contains(Item.get(Item.GOLDEN_HOE))) {
            return;
        }

        player.getInventory().setItem(2, dye);
    }
}