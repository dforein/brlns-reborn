package org.brlnsreb.generallobby.ui;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import org.powernukkitx.Player;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.utils.Config;

public class GamesMenu extends MenuAbstract {

    public static void openMenu(Player player) {
        checkCooldown(player);

        Config config = ConfigManager.getGlobalConfig();

        SimpleForm menu = new SimpleForm(YamlUtil.getStr("items.games.name", config));

        menu.addButton(YamlUtil.getStr("items.games.hub-button-text", config));

        for (Minigame mg : MinigameManager.getMinigames()) {
            menu.addButton(
                YamlUtil.getStr("name", mg.getMessages()) + " §r§7(" + mg.getPlayerCount() + ")"
            );  //TODO: add images?
        }

        menu.putMeta("type", "games");
        menu.send(player);
    }

    public static void handleResponse(CustomPlayer player, int buttonId) {
        if (buttonId == 0) {
            GeneralLobby.getInstance().onJoin(player);
        } else {
            MinigameManager.getMinigames().get(buttonId - 1).onLobbyJoin(player);
        }
    }

}
