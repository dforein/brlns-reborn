package org.brlnsreb.core.minigame.match.game.items;

import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.items.ItemManager;
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
