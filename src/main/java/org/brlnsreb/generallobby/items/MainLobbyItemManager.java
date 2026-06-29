package org.brlnsreb.generallobby.items;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.generallobby.ui.GamesMenu;
import org.brlnsreb.utils.ItemManager;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

public class MainLobbyItemManager extends ItemManager {

    private static MainLobbyItemManager instance;
    
    public MainLobbyItemManager(Config config) {
        super(config);
        instance = this;

        GamesMenu.init(config);
    }

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getDisplayName()) {
            case value:
                
                break;
        
            default:
                break;
        }
    }

    public void giveLobbyItems(Player player) {
        PlayerUtils.clearInventory(player);

        giveGames(player);
        giveMenu(player);
        giveMagicStaff(player);
        giveJoinGame(player);
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

    public static MainLobbyItemManager getInstance() { return instance; }

}
