package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.MinigameCore;

import java.util.List;

public class BookManager {
    
    public static void openRulesBook(Player player, MinigameCore plugin) {
        String title = plugin.getConfig().getString("lobby.rules-book.title", "MurderMystery Rules");
        String author = plugin.getConfig().getString("lobby.rules-book.author", "Server");
        List<String> pages = plugin.getConfig().getStringList("lobby.rules-book.pages");
        
        if (pages.isEmpty()) {
            player.sendMessage(TextFormat.RED + "Rules book is not configured!");
            return;
        }
        
        Item book = Item.get(Item.WRITTEN_BOOK);
        
        CompoundTag tag = book.getNamedTag();
        if (tag == null) {
            tag = new CompoundTag();
        }
        
        tag.putString("title", TextFormat.colorize(title));
        tag.putString("author", TextFormat.colorize(author));
        tag.putInt("generation", 0);

        ListTag<StringTag> pagesList = new ListTag<>(8);

        for (String pageContent : pages) {
            StringTag page = new StringTag();
            page.data = TextFormat.colorize(pageContent);
            pagesList.add(page);
        }

        tag.putList("pages", pagesList);
        book.setNamedTag(tag);
        
        player.getInventory().setItemInHand(book);
        player.getInventory().sendContents(player);
    }
}