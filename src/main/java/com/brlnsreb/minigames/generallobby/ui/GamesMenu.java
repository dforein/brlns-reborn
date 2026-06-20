package com.brlnsreb.minigames.generallobby.ui;

import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.generallobby.GeneralLobby;
import com.brlnsreb.minigames.utils.YamlUtil;
import com.brlnsreb.minigames.utils.abstraction.MenuAbstract;

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
