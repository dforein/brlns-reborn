package org.brlnsreb.core.minigame.game.items;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.utils.ItemManager;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

public class SpectatorItemManager extends ItemManager {
    
    public SpectatorItemManager() {
        super(ConfigManager.getConfig("global/config.yml"));
    }

    public void giveTeleporter(Player player) {
        giveItem(
            player, 
            2, 
            Item.COMPASS,
            YamlUtil.getStr("match.game.spectator.items.teleporter.name", config)
        );
    }

    public void giveActions(Player player) {
        giveItem(
            player, 
            6, 
            Item.CLOCK,
            YamlUtil.getStr("match.game.spectator.items....name", config)
        );
    }

}
