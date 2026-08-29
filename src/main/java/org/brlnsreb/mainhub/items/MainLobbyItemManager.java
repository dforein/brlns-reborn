package org.brlnsreb.mainhub.items;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.ui.GamesMenu;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.items.ItemManager;
import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.utils.Config;

public class MainLobbyItemManager extends ItemManager {

    public static MainLobbyItemManager instance;
    
    public MainLobbyItemManager(Config config) {
        super(Configs.getGlobalConfig());
        instance = this;
    }

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            case Item.ORANGE_DYE -> GamesMenu.openMenu(player);
            //case Item.PURPLE_DYE -> ;
            //case Item.BLAZE_ROD -> ;
            case Item.SLIME_BALL -> player.minigameCurrent.onMatchJoin(player);
        }
    }

    public void giveMainHubItems(Player player) {
        PlayerUtils.clearInventory(player);

        giveGames(player);
        giveMenu(player);
        giveMagicStaff(player);
    }

    public void giveMinigameLobbyItems(Player player) {
        giveMainHubItems(player);
        giveJoinGame(player);
    }

    private void giveGames(Player player) {
        giveItem(player, 1, Item.ORANGE_DYE, getStr("lobby.items.games.name"));
    }

    private void giveMenu(Player player) {
        giveItem(player, 2, Item.PURPLE_DYE, getStr("lobby.items.menu.name"));
    }

    private void giveMagicStaff(Player player) {
        giveItem(player, 3, Item.BLAZE_ROD, getStr("lobby.items.magic-staff.name"));
    }

    private void giveJoinGame(Player player) {
        giveItem(player, 7, Item.SLIME_BALL, getStr("lobby.items.join-game.name"));
    }

}
