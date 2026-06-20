package com.brlnsreb.minigames.generallobby.items;

import com.brlnsreb.minigames.utils.abstraction.ItemManagerAbstract;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

public class MainLobbyItemManager extends ItemManagerAbstract {
    
    public MainLobbyItemManager(Config config) {
        super(config);
    }

    public void giveGames(Player player) {
        giveItem(player, 1, Item.ORANGE_DYE, getStr("items.games.name"));
    }

    public void giveMenu(Player player) {
        //TODO: purple or green? it changes
        giveItem(player, 2, Item.PURPLE_DYE, getStr("items.menu.name"));
    }

    public void giveMagicStaff(Player player) {
        giveItem(player, 3, Item.BLAZE_ROD, getStr("items.magic-staff.name"));
    }

    public void giveJoinGame(Player player) {
        giveItem(player, 7, Item.SLIME_BALL, getStr("items.join-game.name"));
    }

}
