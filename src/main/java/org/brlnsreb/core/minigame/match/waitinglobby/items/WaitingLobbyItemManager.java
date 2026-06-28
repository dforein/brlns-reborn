package org.brlnsreb.core.minigame.match.waitinglobby.items;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.utils.ItemManager;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

public abstract class WaitingLobbyItemManager extends ItemManager {

    public WaitingLobbyItemManager(Config config) {
        super(config);
    }

    public abstract void giveItemsWaitingPlayers(Player player);
    public abstract void giveItemsCountdown(Player player);
    public abstract void giveItemsCountdownShortened(Player player);
    
    public static void giveGamePoll(Player player) {
        Config globalConfig = ConfigManager.getConfig("global/config.yml");
        giveItem(
            player, 
            7, 
            Item.NETHER_STAR, 
            YamlUtil.getStr("match.waiting-lobby.items.game-poll.name", globalConfig)
        );
    }
}
