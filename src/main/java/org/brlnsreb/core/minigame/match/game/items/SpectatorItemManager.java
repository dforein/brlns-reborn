package org.brlnsreb.core.minigame.match.game.items;

import org.brlnsreb.core.Configs;
import org.brlnsreb.utils.ItemManager;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;

public class SpectatorItemManager extends ItemManager {
    
    public SpectatorItemManager() {
        super(Configs.getGlobalConfig());
    }

    public void giveTeleporter(Player player) {
        giveItem(
            player, 
            2, 
            Item.COMPASS,
            YamlUtil.getStr("match.game.items.spectator.teleporter.name", config)
        );
    }

    public void giveActions(Player player) {
        giveItem(
            player, 
            6, 
            Item.CLOCK,
            YamlUtil.getStr("match.game.items.spectator.actions.name", config)
        );
    }

}
