package org.brlnsreb.generallobby.ui;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import cn.nukkit.Player;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.utils.Config;

public class GamesMenu extends MenuAbstract {
    
    private static Config config;

    public static void init(Config lobbyConfig) {
        config = lobbyConfig;
    }

    public static void openMenu(Player player) {
        checkCooldown(player);

        SimpleForm menu = new SimpleForm(YamlUtil.getStr("items.games.name", config));

        menu.addButton(YamlUtil.getStr("items.games.hub-button-text", config));

        for (Minigame mg : MinigameManager.getMinigames()) {
            menu.addButton(
                YamlUtil.getStr("name", mg.getMessages()) + " (" + mg.getPlayerCount() + ")"
            );  //TODO: add images?
        }

        menu.putMeta("type", "games");
        menu.send(player);
    }

    public static void handleResponse(Player player, int buttonId) {
        if (buttonId == 0) {
            GeneralLobby.getInstance().onJoin(player);
        } else {
            MinigameManager.getMinigames().get(buttonId - 1).onLobbyJoin(player);
        }
    }

}
