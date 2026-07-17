package org.brlnsreb.core.minigame.match.waitinglobby.items;

import java.util.Collection;
import java.util.List;

import org.brlnsreb.core.Configs;
import org.brlnsreb.utils.ItemManager;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.nbt.tag.ListTag;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public abstract class WaitingLobbyItemManager extends ItemManager {

    public WaitingLobbyItemManager(Config config) {
        super(config);
    }

    public abstract void giveItemsWaitingPlayers(Player player);
    public abstract void giveItemsCountdown(Player player);
    public abstract void giveItemsCountdownShortened(Player player);

    public void giveItemsWaitingPlayers(Collection<? extends Player> players) {
        for (Player p : players) {
            giveItemsWaitingPlayers(p);
        }
    }

    public void giveItemsCountdown(Collection<? extends Player> players) {
        for (Player p : players) {
            giveItemsCountdown(p);
        }
    }

    public void giveItemsCountdownShortened(Collection<? extends Player> players) {
        for (Player p : players) {
            giveItemsCountdownShortened(p);
        }
    }
    
    public static void giveGamePoll(Player player) {
        Config globalConfig = Configs.getGlobalConfig();
        giveItem(
            player, 
            7, 
            Item.NETHER_STAR, 
            YamlUtil.getStr("match.waiting-lobby.items.game-poll.name", globalConfig)
        );
    }

    protected Item createRulesBook() {
        String path = "waiting-lobby.items.rules-book.";

        String name = YamlUtil.getStr(path + "name", config);
        String title = YamlUtil.getStr(path + "title", config);
        String author = YamlUtil.getStr(path + "author", config);
        List<String> content = config.getStringList(path + "content");
        
        if (content.isEmpty()) {
            content.add("No rules!");
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
        
        for (String pageContent : content) {
            CompoundTag pageTag = new CompoundTag();
            pageTag.putString("photoname", "");
            pageTag.putString("text", TextFormat.colorize(pageContent));
            pagesList.add(pageTag);
        }
        
        tag.putList("pages", pagesList);
        book.setNbt(tag);
        book.setCustomName(name);
        
        return book;
    }
}
